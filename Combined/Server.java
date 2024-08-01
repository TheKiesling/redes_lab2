import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

public class Server {

    public static double errorProbability = 0.01;

    public static String textToBinary(String text) {
        StringBuilder binary = new StringBuilder();
        for (char c : text.toCharArray()) {
            binary.append(String.format("%8s", Integer.toBinaryString(c)).replaceAll(" ", "0"));
        }
        return binary.toString();
    }

    public static String applyNoise(String encodedMessage, double errorProbability) {
        Random rand = new Random();
        char[] noisyMessage = encodedMessage.toCharArray();
        boolean error = false;
        for (int i = 0; i < noisyMessage.length; i++) {
            if (rand.nextDouble() < errorProbability) {
                noisyMessage[i] = (noisyMessage[i] == '0') ? '1' : '0';
                error = true;
            }
        }
        if (error) {
            System.out.println("MENSAJE CON UN ERROR");
        } else {
            System.out.println("MENSAJE SIN ERRORES");
        }
        return new String(noisyMessage);
    }

    public static int[] decompose(int position, int r) {
        int[] components = new int[r];
        int current = 0;

        for (int i = r - 1; i >= 0; i--) {
            int power = (int) Math.pow(2, i);
            if (position - power >= 0) {
                components[current] = power;
                position -= power;
            }
            current++;
        }

        return components;
    }

    public static char getParity(int p, int r, String message) {
        int counter = 0;
        int length = message.length();

        for (int i = 1; i <= length; i++) {
            if ((i & (i - 1)) == 0) {
                continue;
            }
            if (Arrays.stream(decompose(i, r)).anyMatch(j -> j == p)) {
                if (message.charAt(i - 1) == '1') {
                    counter++;
                }
            }
        }

        return (counter % 2 == 0) ? '0' : '1';
    }

    public static String hammingEncoding(String message) {
        int m = message.length();
        int r = 0;

        while (Math.pow(2, r) < m + r + 1) {
            r++;
        }

        char[] encoded = new char[m + r];
        int messagePos = 0;

        for (int i = 0; i < encoded.length; i++) {
            if ((i + 1 & i) == 0) {
                encoded[i] = '0';
            } else {
                encoded[i] = message.charAt(messagePos);
                messagePos++;
            }
        }

        for (int i = 0; i < encoded.length; i++) {
            if ((i + 1 & i) == 0) {
                encoded[i] = getParity(i + 1, r, new String(encoded));
            }
        }

        return new String(encoded);
    }

    public static int fletcher16(byte[] data) {
        int sum1 = 0xFF;
        int sum2 = 0xFF;
        for (byte b : data) {
            sum1 = (sum1 + (b & 0xFF)) % 255;
            sum2 = (sum2 + sum1) % 255;
        }
        return (sum2 << 8) | sum1;
    }

    public static String fletcherEncoding(String message) {
        int padding = 8 - (message.length() % 8);
        if (padding < 8) {
            message += "0".repeat(padding);
        }

        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        int checksum = fletcher16(messageBytes);
        return message + String.format("%04x", checksum);
    }

    public static void main(String[] args) {
        int port = 12345;

        try (ServerSocket serverSocket = new ServerSocket(port);
             Socket clientSocket = serverSocket.accept();
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String message;
            Random rand = new Random();
            while ((message = in.readLine()) != null) {
                System.out.println("Mensaje recibido: " + message);
                String binaryMessage = textToBinary(message);

                String encodedMessage;
                String finalMessage;
                if (rand.nextBoolean()) {
                    encodedMessage = hammingEncoding(binaryMessage);
                    finalMessage = 'H' + applyNoise(encodedMessage, errorProbability);
                    System.out.println("SE USA Hamming");
                } else {
                    encodedMessage = fletcherEncoding(binaryMessage);
                    finalMessage = 'F' + applyNoise(encodedMessage, errorProbability);
                    System.out.println("SE USA Fletcher");
                }

                System.out.println("Mensaje codificado: " + finalMessage);
                out.println(finalMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
