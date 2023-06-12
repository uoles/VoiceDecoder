package ru.uoles.proj;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import ru.uoles.proj.entity.Partial;
import ru.uoles.proj.rtu.ComPort;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static ru.uoles.proj.rtu.CRC16.getCrc16;
import static ru.uoles.proj.rtu.ComPort.bytesToHexString;
import static ru.uoles.proj.rtu.ComPort.convertIntegerToByteArray;
import static ru.uoles.proj.utils.Constants.DICTIONARY;

@SpringBootApplication
public class Application implements CommandLineRunner {

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static String lastCommand;
    private static ComPort sowa = new ComPort();

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        LibVosk.setLogLevel(LogLevel.DEBUG);

        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 60000, 16, 2, 4, 44100, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        TargetDataLine microphone = null;
        try (Model model = new Model("/home/forester/sowa/vosk-model-small-ru-0.22");
             Recognizer recognizer = new Recognizer(model, 120000)) {
            try {
                microphone = (TargetDataLine) AudioSystem.getLine(info);
                microphone.open(format);
                microphone.start();
                initSowaConnect("COM2");

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int numBytesRead;
                int CHUNK_SIZE = 1024;
                byte[] b = new byte[4096];

                while (true) {
                    numBytesRead = microphone.read(b, 0, CHUNK_SIZE);

                    Partial partial;
                    if (recognizer.acceptWaveForm(b, numBytesRead)) {
                        partial = objectMapper.readValue(recognizer.getResult(), Partial.class);
                    } else {
                        partial = objectMapper.readValue(recognizer.getPartialResult(), Partial.class);
                    }

                    checkCommand(partial.getPartial());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeSowaConnect();
            if (Objects.nonNull(microphone)) {
                microphone.close();
            }
        }
    }

    private static void checkCommand(final String command) {
        if (!Objects.isNull(command) && !command.equals(lastCommand)) {
            List<String> words = Arrays.asList(command.trim().split("\\s+"));

            boolean isBadWord = DICTIONARY.stream().anyMatch(words::contains);

            if (isBadWord) {
                sendMessageToSowa(100);
                System.out.println("------ " + command + " ВВЕРХ");

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (!isBadWord) {
                sendMessageToSowa(0);
                System.out.println("------ " + command + " ВНИЗ");
            }
        }
        lastCommand = command;
    }

    private static void initSowaConnect(final String portName) {
        sowa.connectPort(portName);
    }

    private static void closeSowaConnect() {
        sowa.closePort();
    }

    private static void sendMessageToSowa(int value) {
        sowa.sendData(createPacket(value));
    }

    public static byte[] createPacket(int value) {
        byte[] data = new byte[] {
                (byte) (01 & 0xff),
                (byte) (06 & 0xff),
                (byte) (00 & 0xff),
                (byte) (00 & 0xff),
                (byte) (00 & 0xff),
                (byte) (value & 0xff)
        };

        int crc = getCrc16(data, 6, 0xffff);
        System.out.println(crc);

        byte[] data_crc = convertIntegerToByteArray(crc, 2,false);
        System.out.println(bytesToHexString(data_crc));

        return new byte[] {
                data[0],
                data[1],
                data[2],
                data[3],
                data[4],
                data[5],
                data_crc[1],
                data_crc[0]
        };
    }
}