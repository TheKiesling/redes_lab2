import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.Arrays;
import java.util.Random;

public class Server {

    public static double errorProbability = 1 / 100;

    public static String applyNoise(String encodedMessage, double errorProbability) {
        boolean error = false;
        Random rand = new Random();
        char[] noisyMessage = encodedMessage.toCharArray();
        for (int i = 0; i < noisyMessage.length; i++) {
            if (rand.nextDouble() < errorProbability) {
                error = true;
                noisyMessage[i] = (noisyMessage[0] == '0') ? '1' : '0';
            }
        }

        if (error)
            System.out.println("Se han añadido errores al mensaje.");

        System.out.println("original: " + encodedMessage);
        System.out.println("nuevo: " + new String(noisyMessage));

        return new String(noisyMessage);
    }

    public static String validateBinary(Scanner scan) {

        String binaryNumber;

        while (true) {
            System.out.print("Ingrese el mensaje a enviar: ");
            binaryNumber = scan.nextLine();

            if (!binaryNumber.matches("[01]+")) {
                System.out.println("Entrada inválida. Por favor, ingrese solo 0s y 1s.");
            } else {
                break;
            }
        }

        return binaryNumber;
    }

    public static int validateInt(Scanner scan, String menu, int max, boolean zero) {

        String option;

        while (true) {
            System.out.print(menu);
            option = scan.nextLine();

            if (!option.matches("[0-9]+"))
                System.out.println("Debe ingresar un número entero.");
            else if (Integer.valueOf(option) > max || (zero && Integer.valueOf(option) == 0))
                System.out.println("Opción no válida.");
            else
                break;
        }

        return Integer.valueOf(option);
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

    public static String choiceAlgorithm(int option, String message) {
        switch (option) {
            case 1:
                return 'H' + applyNoise(hammingEncoding(message), 1.0 / message.length());
            case 2:
                return 'F' + applyNoise(fletcherEncoding(message), 1.0 / message.length());
            default:
                System.out.println("Opción no válida.");
                return null;
        }
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
        String messageEncoded = message + String.format("%04x", checksum);
        System.out.println("Mensaje con checksum: " + messageEncoded);

        return messageEncoded;
    }

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        int port = 12345;
        boolean repeat = true;

        try (ServerSocket serverSocket = new ServerSocket(port);
                Socket clientSocket = serverSocket.accept();
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            while (repeat) {

                String binaryNumber = validateBinary(scan);

                String messageEncoded = null;

                while (messageEncoded == null) {
                    int option = validateInt(scan, """
                            Selecciona el algoritmo a utilizar:
                                1. Hamming.
                                2. Fletcher.
                            """, 2, false);
                    messageEncoded = choiceAlgorithm(option, binaryNumber);
                }

                System.out.println("Mensaje codificado: " + messageEncoded);
                out.println(messageEncoded);

                int option = validateInt(scan, """
                        ¿Desea enviar un nuevo mensaje?
                        1. Sí
                        2. No
                        """, 2, false);

                if (option == 2)
                    repeat = false;

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scan.close();
        }
    }
}
