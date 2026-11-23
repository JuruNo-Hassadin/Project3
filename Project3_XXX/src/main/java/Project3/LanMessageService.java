package Project3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class LanMessageService implements AutoCloseable {
    private static final String MESSAGE_TYPE = "CHATGAE_TEXT";

    @FunctionalInterface
    public interface MessageListener {
        void onTextReceived(
                String timestamp,
                String sender,
                String recipient,
                String text
        );
    }

    private final String username;
    private final MessageListener listener;

    private volatile boolean running;
    private ServerSocket serverSocket;
    private Thread listenerThread;

    public LanMessageService(String username, MessageListener listener) {
        this.username = username;
        this.listener = listener;
    }

    public synchronized int start() throws IOException {
        if (running && serverSocket != null) {
            return serverSocket.getLocalPort();
        }

        ServerSocket newServer = new ServerSocket();

        try {
            newServer.setReuseAddress(true);
            newServer.bind(new InetSocketAddress(0));
        } catch (IOException ex) {
            newServer.close();
            throw ex;
        }

        serverSocket = newServer;
        running = true;

        listenerThread = new Thread(
                this::listen,
                "chatgae-tcp-listener"
        );
        listenerThread.setDaemon(true);
        listenerThread.start();

        return serverSocket.getLocalPort();
    }

    private void listen() {
        while (running) {
            try {
                Socket client = serverSocket.accept();

                Thread clientThread = new Thread(
                        () -> receiveMessage(client),
                        "chatgae-client-message"
                );
                clientThread.setDaemon(true);
                clientThread.start();

            } catch (SocketException ex) {
                if (running) {
                    System.err.println(
                            "LAN message listener: " + ex.getMessage()
                    );
                }
            } catch (IOException ex) {
                if (running) {
                    System.err.println(
                            "LAN message listener: " + ex.getMessage()
                    );
                }
            }
        }
    }

    private void receiveMessage(Socket client) {
        try (Socket connection = client) {
            DataInputStream input =
                    new DataInputStream(connection.getInputStream());

            String type = input.readUTF();
            String sender = input.readUTF();
            String recipient = input.readUTF();
            String timestamp = input.readUTF();
            String text = input.readUTF();

            if (!MESSAGE_TYPE.equals(type)) {
                return;
            }

            if (!username.equalsIgnoreCase(recipient)) {
                return;
            }

            listener.onTextReceived(
                    timestamp,
                    sender,
                    recipient,
                    text
            );

        } catch (IOException ex) {
            if (running) {
                System.err.println(
                        "Could not receive message: " + ex.getMessage()
                );
            }
        }
    }

    public void sendText(
            String ipAddress,
            int recipientPort,
            String sender,
            String recipient,
            String timestamp,
            String text
    ) throws IOException {
        if (!running) {
            throw new IOException("LAN message service is not running.");
        }

        if (recipientPort <= 0 || recipientPort > 65535) {
            throw new IOException("The recipient TCP port is invalid.");
        }

        try (Socket connection = new Socket()) {
            connection.connect(
                    new InetSocketAddress(ipAddress, recipientPort),
                    2500
            );

            DataOutputStream output =
                    new DataOutputStream(connection.getOutputStream());

            output.writeUTF(MESSAGE_TYPE);
            output.writeUTF(sender);
            output.writeUTF(recipient);
            output.writeUTF(timestamp);
            output.writeUTF(text);
            output.flush();
        }
    }

    @Override
    public synchronized void close() {
        running = false;

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                System.err.println(
                        "Could not close LAN server: " + ex.getMessage()
                );
            }
            serverSocket = null;
        }

        if (listenerThread != null) {
            listenerThread.interrupt();
            listenerThread = null;
        }
    }
}