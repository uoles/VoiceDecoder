package ru.uoles.ex;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import ru.uoles.ex.entity.Command;
import ru.uoles.ex.entity.Partial;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class Main {

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static final String RESULT_LIGHT_ON = "ВКЛЮЧИЛ СВЕТ";
    private static final String RESULT_LIGHT_OFF = "ВЫКЛЮЧИЛ СВЕТ";

    private static Set<Command> journal = new HashSet<>();
    private static String lastCommand;

    public static void main(String[] args) {
        LibVosk.setLogLevel(LogLevel.DEBUG);

        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 60000, 16, 2, 4, 44100, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        TargetDataLine microphone = null;
        try (Model model = new Model("vosk-model-small-ru-0.22");
             Recognizer recognizer = new Recognizer(model, 120000)) {
            try {
                microphone = (TargetDataLine) AudioSystem.getLine(info);
                microphone.open(format);
                microphone.start();

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int numBytesRead;
                int CHUNK_SIZE = 1024;
                int bytesRead = 0;
                byte[] b = new byte[4096];

                while (bytesRead <= 100000000) {
                    numBytesRead = microphone.read(b, 0, CHUNK_SIZE);
                    bytesRead += numBytesRead;

                    out.write(b, 0, numBytesRead);

                    Partial partial;
                    if (recognizer.acceptWaveForm(b, numBytesRead)) {
                        partial = objectMapper.readValue(recognizer.getResult(), Partial.class);
                    } else {
                        partial = objectMapper.readValue(recognizer.getPartialResult(), Partial.class);
                    }

                    if (StringUtils.isNotBlank(partial.getPartial())) {
                        checkCommand(partial.getPartial());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (Objects.nonNull(microphone)) {
                microphone.close();
            }
        }
    }

    private static void checkCommand(final String command) {
        if (!command.equals(lastCommand)) {
            List<String> words = Arrays.asList(command.trim().split("\\s+"));

            if (words.contains("окей") && words.contains("джарвис")) {
                if (words.contains("включи") && words.contains("свет")) {
                    addCommandToJournal(command, RESULT_LIGHT_ON);
                }

                if (words.contains("выключи") && words.contains("свет")) {
                    addCommandToJournal(command, RESULT_LIGHT_OFF);
                }
            }
        }
        lastCommand = command;
    }

    private static void addCommandToJournal(final String command, final String result) {
        journal.add(new Command(command, result));

        System.out.println("----------------");
        journal.stream()
                .sorted(Comparator.comparing(Command::getDataTime, Comparator.reverseOrder()))
                .forEach(o -> System.out.println(
                        String.join(", ", o.getDataTime(), o.getPartial(), o.getResult())
                ));
    }
}