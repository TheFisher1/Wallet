package bg.sofia.uni.fmi.mjt.server;

import java.util.Scanner;

public class ServerHandler {

    public static void main(String[] args) {

        Thread serverThread = null;
        Server server = null;

        var scanner = new Scanner(System.in);
        System.out.println("Server started");
        printCommands();

        while (true) {
            System.out.print("=> ");
            String cmd = scanner.nextLine();

            switch (cmd) {
                case "start" -> {
                    if (server != null) {
                        System.out.println("Server already started. ");
                        break;
                    }

                    server = new Server();
                    serverThread = new Thread(server);
                    serverThread.setDaemon(true);

                    System.out.println("Starting server.");
                    serverThread.start();
                }

                case "stop" -> {
                    if (server == null) {
                        System.out.println("Server not started yet.");
                        break;
                    }

                    System.out.println("Stopping server...");
                    server.stop();
                    serverThread.interrupt();

                    try {

                        serverThread.join();
                    } catch (InterruptedException e) {
                        System.err.println(e.getMessage());
                    }
                    server = null;
                }

                case "exit" -> {
                    if (server != null) {
                        System.out.println("Server still running. Cannot exit.");
                        break;
                    }
                    System.out.println("exiting");
                    return;
                }

                default -> {
                    System.out.println("Invalid command.");
                    printCommands();
                }
            }
        }
    }

    private static void printCommands() {
        System.out.println("""
                              Available commands: "
                              start : starts the server,"
                              stop  : stops the server,"
                              exit  : exits the server console;
                              """);

    }
}
