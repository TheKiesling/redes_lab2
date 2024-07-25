import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.Scanner;

public class JavaServer {

    public static int fletcher16(byte[] data) {
        int sum1 = 0xFF;
        int sum2 = 0xFF;
        for (byte b : data) {
            sum1 = (sum1 + (b & 0xFF)) % 255;
            sum2 = (sum2 + sum1) % 255;
        }
        return (sum2 << 8) | sum1;
    }

    public static String validateBinary() {
        Scanner scan = new Scanner(System.in);
        String binaryNumber;

        while (true) {
            System.out.print("Ingrese el mensaje binario a enviar: ");
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

    public static void main(String[] args) {
        int port = 12345;

        try (ServerSocket serverSocket = new ServerSocket(port);
             Socket clientSocket = serverSocket.accept();
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String binaryNumber = validateBinary();

            // Padding para asegurar longitud múltiplo de 8
            int padding = 8 - (binaryNumber.length() % 8);
            if (padding < 8) {
                binaryNumber += "0".repeat(padding);
            }

            byte[] messageBytes = binaryNumber.getBytes(StandardCharsets.UTF_8);
            int checksum = fletcher16(messageBytes);
            String messageEncoded = binaryNumber + String.format("%04x", checksum);
            System.out.println("Mensaje con checksum: " + messageEncoded);

            // Introducción de un error en una posición aleatoria
            Random rand = new Random();
            int randPos = rand.nextInt(messageEncoded.length() - 4); // Evita el checksum
            char errorBit = (messageEncoded.charAt(randPos) == '0') ? '1' : '0';
            messageEncoded = messageEncoded.substring(0, randPos) + errorBit + messageEncoded.substring(randPos+1);

            System.out.println("Se ha agregado un error en la posición: " + (randPos + 1));
            System.out.println("Mensaje con error: " + messageEncoded);

            out.println(messageEncoded);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
