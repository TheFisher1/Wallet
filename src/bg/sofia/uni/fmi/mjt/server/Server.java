package bg.sofia.uni.fmi.mjt.server;

import bg.sofia.uni.fmi.mjt.server.commands.CommandExecutor;
import bg.sofia.uni.fmi.mjt.server.logging.LoggingHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Runnable {
    private static final int BUFFER_SIZE = 4096;
    private static final String HOST = "localhost";
    private static final int DEFAULT_PORT = 6666;

    private static final String DEFAULT_LOG_FILE = "log.txt";
    private static final Logger LOGGER = Logger.getLogger("default-logger");
    private static final LoggingHandler LOG = new LoggingHandler(LOGGER);
    private final CommandExecutor commandExecutor;
    private final int port;
    private boolean isServerWorking;

    private ByteBuffer buffer;
    private Selector selector;

    public Server(int port, CommandExecutor commandExecutor) {
        this.port = port;
        this.commandExecutor = commandExecutor;

        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "there was an error: " + e.getMessage() + ". Stacktrace: " +
                Arrays.toString(e.getStackTrace()));
        }

    }

    public Server() {
        this(DEFAULT_PORT, new CommandExecutor(LOG));
        try {
            Handler handler = new FileHandler(DEFAULT_LOG_FILE);
            LOG.addHandler(handler);
            LOG.setLevel(Level.ALL);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }

    public void start() {
        System.out.println("Server was started successfully");
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            selector = Selector.open();

            configureServerSocketChannel(serverSocketChannel, selector);

            this.buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
            isServerWorking = true;

            while (isServerWorking) {
                try {
                    int readyChannels = selector.select();
                    if (readyChannels == 0) {
                        continue;
                    }

                    processClients();

                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, e.getMessage());
                }
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            throw new RuntimeException("there was a problem");
        }
    }

    public void stop() {

        this.isServerWorking = false;
        if (selector.isOpen()) {
            selector.wakeup();
        }

        commandExecutor.shutdown();
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(HOST, this.port));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String getClientInput(SocketChannel clientChannel) throws IOException {
        buffer.clear();

        int readBytes = clientChannel.read(buffer);
        if (readBytes < 0) {
            clientChannel.close();
            return null;
        }

        buffer.flip();

        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);

        return new String(clientInputBytes, StandardCharsets.UTF_8);
    }

    private void writeClientOutput(SocketChannel clientChannel, String output) throws IOException {
        buffer.clear();
        buffer.put(output.getBytes());
        buffer.flip();

        clientChannel.write(buffer);
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();

        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }

    @Override
    public void run() {
        start();
    }

    private void processClients() throws IOException {
        Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();

            if (key.isReadable()) {
                SocketChannel clientChannel = (SocketChannel) key.channel();
                String clientInput = getClientInput(clientChannel);

                if (clientInput == null) {
                    continue;
                }

                String output = commandExecutor.execute(key, clientInput);

                writeClientOutput(clientChannel, output);
            } else if (key.isAcceptable()) {
                accept(selector, key);
            }

            keyIterator.remove();
        }
    }
}