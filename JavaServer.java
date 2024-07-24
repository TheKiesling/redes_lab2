import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Arrays;
import java.util.Random;

public class JavaServer {

    public static String validateBinary() {
        Scanner scan = new Scanner(System.in);
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

        scan.close();
        return binaryNumber;
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

    public static void main(String[] args) {
        int port = 12345;

        try (ServerSocket serverSocket = new ServerSocket(port);
                Socket clientSocket = serverSocket.accept();
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String binaryNumber = validateBinary();

            String messageEncoded = hammingEncoding(binaryNumber);

            System.out.println("Mensaje codificado: " + messageEncoded);

            // Agregar un error en posición aleatoria
            Random rand = new Random();
            int randPos = rand.nextInt(messageEncoded.length());

            char errorBit = (messageEncoded.charAt(randPos) == '0') ? '1' : '0';
            messageEncoded = messageEncoded.substring(0, randPos) + errorBit + messageEncoded.substring(randPos+1);

            System.out.println("Se ha agregado un error en la posición: " + (randPos+1));
            System.out.println("Mensaje con error: " + messageEncoded);
            
            // Agregar error adicional en posición aleatoria
            // rand = new Random();
            // randPos = rand.nextInt(messageEncoded.length());

            // errorBit = (messageEncoded.charAt(randPos) == '0') ? '1' : '0';
            // messageEncoded = messageEncoded.substring(0, randPos) + errorBit + messageEncoded.substring(randPos+1);

            // System.out.println("Se ha agregado un error en la posición: " + (randPos+1));
            // System.out.println("Mensaje con error: " + messageEncoded);

            out.println(messageEncoded);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
