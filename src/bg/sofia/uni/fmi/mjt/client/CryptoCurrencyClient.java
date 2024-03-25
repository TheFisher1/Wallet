package bg.sofia.uni.fmi.mjt.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class CryptoCurrencyClient {
    private static final int SERVER_PORT = 6666;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 4096;

    public static void main(String[] args) {

        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            System.out.println("Connected to the server.");

            while (true) {
                String message = getMessageFromUser(scanner);

                if ("quit".equals(message)) {
                    break;
                }

                sendMessageToServer(socketChannel, message);
                String reply = receiveMessageFromServer(socketChannel);
                System.out.println("The server replied <" + reply + ">");
            }

        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the network communication", e);
        }
    }

    private static String getMessageFromUser(Scanner scanner) {
        System.out.print("Enter message: ");
        return scanner.nextLine();
    }

    private static void sendMessageToServer(SocketChannel socketChannel, String message) throws IOException {
        if (message.equals("")) {
            message = " ";
        }

        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        socketChannel.write(buffer);
    }

    private static String receiveMessageFromServer(SocketChannel socketChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        socketChannel.read(buffer);
        buffer.flip();
        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);
        return new String(byteArray, "UTF-8");
    }
}
