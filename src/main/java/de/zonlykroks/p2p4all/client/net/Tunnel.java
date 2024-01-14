package de.zonlykroks.p2p4all.client.net;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Tunnel {
    private final int sourcePort;
    private final InetSocketAddress target;

    public Tunnel(int sourcePort, String dest) {
        this.sourcePort = sourcePort;

        String[] targetParts = dest.split(":");
        this.target = new InetSocketAddress(targetParts[0], Integer.parseInt(targetParts[1]));
    }

    public void startAsClient() {
        System.out.printf("Starting hole punching: [:%d] --- [%s:%d]\n",
                sourcePort, target.getAddress(), target.getPort());

        Socket server = new Socket();

        try {
            server.bind(new InetSocketAddress(sourcePort));
        } catch (IOException e) {
            System.out.println("failed to bind to socket, might be in use");
        }

        for (int i = 0; i < 10; i++) {
            try {
                server.connect(target, 2000);
                System.out.println("connection established successfully! Connect to localhost now");

                ServerSocket local = new ServerSocket(sourcePort + 1);
                Socket client = local.accept();

                Thread c2s = new Forwarder(client.getOutputStream(), server.getInputStream());
                Thread s2c = new Forwarder(server.getOutputStream(), client.getInputStream());

                c2s.start();
                s2c.start();

                local.close();

            } catch (IOException e) {
                System.out.println("failed connection attempt " + (i + 1) + "/10");
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            }
        }

        try {
            server.close();
        } catch (IOException e) {
            System.out.println("failed to close server socket. oh well");
        }
    }

    public void startAsServer() {
        System.out.printf("Starting hole punching: [:%d] --- [%s:%d]\n",
                sourcePort, target.getAddress(), target.getPort());

        try (Socket s = new Socket()) {
            s.setReuseAddress(true);
            s.bind(new InetSocketAddress(sourcePort));
            s.connect(target, 500);
        } catch (IOException e) {
            System.out.println("punch packet sent (failed successfully), err: " + e.getMessage());
        }

        try (ServerSocket sock = new ServerSocket(sourcePort)) {
            Socket client = sock.accept();
            Socket server = new Socket("localhost", 25565);

            Thread c2s = new Forwarder(client.getOutputStream(), server.getInputStream());
            Thread s2c = new Forwarder(server.getOutputStream(), client.getInputStream());

            c2s.start();
            s2c.start();

        } catch (IOException e) {

        }
    }

    private static class Forwarder extends Thread {

        private final InputStream is;
        private final OutputStream os;

        public Forwarder(OutputStream os, InputStream is) {
            this.is = is;
            this.os = os;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[512];
            try {
                while (true) {
                    int bytesRead = is.read(buffer);
                    if (bytesRead == -1) break;
                    os.write(buffer, 0, bytesRead);
                    os.flush();
                }
            } catch (IOException e) {
                System.out.println("Forwarding failed: " + e.getMessage());
            }
        }
    }
}
