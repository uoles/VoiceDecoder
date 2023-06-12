package ru.uoles.proj.rtu;

import com.fazecast.jSerialComm.SerialPort;

import java.nio.ByteBuffer;
import java.util.Objects;

public class ComPort implements AutoCloseable {

    public SerialPort comPort = null;
    public byte[] receivingPack = new byte[8];

    public void connectPort(final String port) {
        for (SerialPort serialPort : SerialPort.getCommPorts()) {
            String portName = serialPort.getDescriptivePortName();
            System.out.println("Find: " + serialPort.getSystemPortName() + ": " + portName);

            if (portName.contains(port)) {
                try {
                    comPort = serialPort;
                    comPort.setComPortParameters(9600, 8, 1, 0);
                    comPort.openPort();

                    System.out.println("Connected to: " + portName);

                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    comPort.closePort();
                }
            }
        }

      //  (new Thread(new SerialReader())).start();
    }

    public void closePort() {
        if (Objects.nonNull(comPort)) {
            comPort.closePort();
        }
    }

    public void sendData(int command, int bytesLength) {
        byte[] buffer = ByteBuffer.allocate(bytesLength).putInt(command).array();
        comPort.writeBytes(buffer, bytesLength, 0);
        System.out.println("Sending: " + bytesToHexString(buffer));
    }

    public void sendData(byte[] buffer) {
        comPort.writeBytes(buffer, buffer.length, 0);
        System.out.println("Sending: " + bytesToHexString(buffer));
    }

    public static String bytesToHexString(final byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    public static byte[] convertIntegerToByteArray(int value, int bytes, boolean revers) {
        if (bytes == 1 && !revers) {
            return new byte[] { (byte) value };
        } else if (bytes == 1 && revers) {
            return new byte[] { (byte) (value >>> 24) };
        } else if (bytes == 2 && !revers) {
            return new byte[] { (byte) (value >>> 8), (byte) value };
        } else if (bytes == 2 && revers) {
            return new byte[] { (byte) (value >>> 24),
                    (byte) (value >>> 16) };
        } else if (bytes == 3 && !revers) {
            return new byte[] { (byte) (value >>> 16),
                    (byte) (value >>> 8), (byte) value };
        } else if (bytes == 3 && revers) {
            return new byte[] { (byte) (value >>> 24),
                    (byte) (value >>> 16), (byte) (value >>> 8) };
        } else {//www.j a  v a2 s  .c o m
            return new byte[] { (byte) (value >>> 24),
                    (byte) (value >>> 16), (byte) (value >>> 8),
                    (byte) value };
        }
    }

    @Override
    public void close() {
        closePort();
    }

    public class SerialReader implements Runnable {
        public void run() {
            readData();
        }
    }

    public void readData() {
        String bufferD = "0000000000000000";
        String newBufferD = "";

        while (true) {
            comPort.readBytes(receivingPack, 8, 0);

            newBufferD = bytesToHexString(receivingPack);
            if (!bufferD.equals(newBufferD)) {
                System.out.println("Receive: " + newBufferD);
                bufferD = newBufferD;
            }

            try {
                Thread.sleep(200);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}