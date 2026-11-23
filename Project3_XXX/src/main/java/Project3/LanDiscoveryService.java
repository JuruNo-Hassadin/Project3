/* Group members (English names and student IDs): [FILL IN] */
package Project3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public final class LanDiscoveryService implements AutoCloseable {
    private static final int UDP_PORT = 45871;
    private static final String PREFIX = "CHATGAE_ONLINE|";

    @FunctionalInterface
    public interface PeerListener {
        void onPeerSeen(String username, String ipAddress, int tcpPort);
    }

    private final String username;
    private final int localTcpPort;
    private final PeerListener listener;

    private volatile boolean running;
    private DatagramSocket socket;
    private Thread receiverThread;
    private Thread announcerThread;

    public LanDiscoveryService(
            String username,
            int localTcpPort,
            PeerListener listener
    ) {
        this.username = username.trim();
        this.localTcpPort = localTcpPort;
        this.listener = listener;
    }

    public synchronized void start() throws IOException {
        if (running) return;

        socket = new DatagramSocket(null);
        socket.setReuseAddress(true);
        socket.setBroadcast(true);
        socket.setSoTimeout(1000);
        socket.bind(new InetSocketAddress(UDP_PORT));
        running = true;

        receiverThread = new Thread(
                this::receiveAnnouncements,
                "lan-user-receiver"
        );
        receiverThread.setDaemon(true);
        receiverThread.start();

        announcerThread = new Thread(
                this::announcePresence,
                "lan-user-announcer"
        );
        announcerThread.setDaemon(true);
        announcerThread.start();
    }

    private void announcePresence() {
        InetAddress broadcastAddress;

        try {
            broadcastAddress = InetAddress.getByName("255.255.255.255");
        } catch (IOException ex) {
            return;
        }

        while (running) {
            try {
                String message =
                        PREFIX + username + "|" + localTcpPort;
                byte[] data = message.getBytes(StandardCharsets.UTF_8);

                DatagramPacket packet = new DatagramPacket(
                        data,
                        data.length,
                        broadcastAddress,
                        UDP_PORT
                );

                socket.send(packet);
                Thread.sleep(2000);

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            } catch (SocketException ex) {
                if (!running) return;
            } catch (IOException ex) {
                if (!running) return;
            }
        }
    }

    private void receiveAnnouncements() {
        byte[] buffer = new byte[512];

        while (running) {
            DatagramPacket packet =
                    new DatagramPacket(buffer, buffer.length);

            try {
                socket.receive(packet);

                String message = new String(
                        packet.getData(),
                        packet.getOffset(),
                        packet.getLength(),
                        StandardCharsets.UTF_8
                );

                if (!message.startsWith(PREFIX)) continue;

                String payload = message.substring(PREFIX.length());
                int separator = payload.lastIndexOf('|');

                if (separator <= 0) continue;

                String peerName = payload.substring(0, separator).trim();
                int peerPort;

                try {
                    peerPort = Integer.parseInt(
                            payload.substring(separator + 1).trim()
                    );
                } catch (NumberFormatException ex) {
                    continue;
                }

                if (peerName.isBlank()) continue;
                if (peerName.equalsIgnoreCase(username)) continue;
                if (peerPort <= 0 || peerPort > 65535) continue;

                String ipAddress = packet.getAddress().getHostAddress();

                listener.onPeerSeen(peerName, ipAddress, peerPort);

            } catch (SocketTimeoutException ex) {
                // ตรวจ running ซ้ำหลัง timeout
            } catch (SocketException ex) {
                if (!running) return;
            } catch (IOException | RuntimeException ex) {
                if (!running) return;
            }
        }
    }

    @Override
    public synchronized void close() {
        running = false;

        if (socket != null) {
            socket.close();
            socket = null;
        }

        if (receiverThread != null) receiverThread.interrupt();
        if (announcerThread != null) announcerThread.interrupt();
    }
}