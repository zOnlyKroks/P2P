package de.zonlykroks.p2p4all.client.net;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

public class Tunnel {
    private final int sourcePort;
    private final InetSocketAddress target;
    private final boolean isServer;

    public Tunnel(int sourcePort, String dest, boolean isServer) {
        this.sourcePort = sourcePort;
        this.isServer = isServer;

        String[] targetParts = dest.split(":");
        System.out.println(Arrays.toString(targetParts));
        this.target = new InetSocketAddress(targetParts[0], Integer.parseInt(targetParts[1]));
    }

    public void start() throws SocketException {
        System.out.printf("Starting hole punching: [:%d] --- [%s:%d]\n",
                sourcePort, target.getAddress(), target.getPort());

        Socket remote = new Socket();
        InetSocketAddress localAddr = new InetSocketAddress(sourcePort);

        for (int i = 0; i < 20; i++) {
            System.out.printf("punch attempt %d / %d\n", i+1, 20);
            try {
                remote = new Socket();
                remote.setReuseAddress(true);
                remote.bind(localAddr);
                remote.connect(target, isServer ? 2200 : 1800);
                System.out.println("connection established as client!");
                break;
            } catch (IOException e) {
                System.out.println("IOEx punch clientside bind: " + e.getMessage());
                try {
                    remote.close();
                    remote = null;
                } catch (IOException ex) {
                    System.out.println("IOEx punch clientside close: " + e.getMessage());
                }
            }

            try (ServerSocket s = new ServerSocket()) {
                s.setReuseAddress(true);
                s.setSoTimeout(isServer ? 3000 : 2500);
                s.bind(localAddr);
                remote = s.accept();
                System.out.println("connection established as server!");
                break;
            } catch (IOException e) {
                System.out.println("IOEx punch serverside bind: " + e.getMessage());
                try {
                    if (remote != null) remote.close();
                    remote = null;
                } catch (IOException ex) {
                    System.out.println("IOEx punch serverside close: " + ex.getMessage());
                }
            }
        }

        if (remote == null) {
            System.out.println("failed to setup tunnel, aborting");
            throw new SocketException("failed to setup tunnel");
        }

        Socket local = getLocalTunnel();
        if (local == null) {
            System.out.println("failed to setup local tunnel, aborting");
            throw new SocketException("failed to setup local tunnel");
        }

        try {
            Thread out = new Forwarder(local.getOutputStream(), remote.getInputStream());
            Thread in  = new Forwarder(remote.getOutputStream(), local.getInputStream());

            out.start();
            in.start();
        } catch (IOException e) {
            System.out.println("stream forwarding failed: " + e.getMessage());
        }


    }

    private Socket getLocalTunnel() {

        if (this.isServer) {
            // do NOT use try with resources here!
            try {
                return new Socket("localhost", 25565);
            } catch (Exception e) {
                System.out.println("failed to connect local tunnel to LAN server: " + e.getMessage());
                return null;
            }
        } else {
            // or here!
            try {
                ServerSocket localServer = new ServerSocket(sourcePort + 1);
                Socket client = localServer.accept();
                localServer.close();
                return client;
            } catch (IOException e) {
                System.out.println("failed to setup local tunnel: " + e.getMessage());
                return null;
            }
        }
    }

    public void startAsClient() {
        System.out.printf("Starting hole punching: [:%d] --- [%s:%d]\n",
                sourcePort, target.getAddress(), target.getPort());

        Socket server = new Socket();

        try {
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(sourcePort));
        } catch (IOException e) {
            System.out.println("failed to bind to socket, might be in use");
        }

        for (int i = 0; i < 20; i++) {
            try {
                server.connect(target, 2000);
                System.out.println("connection established successfully! Connect to localhost now");

                ServerSocket local = new ServerSocket();
                local.setReuseAddress(true);
                local.bind(new InetSocketAddress(sourcePort + 1));
                Socket client = local.accept();

                Thread c2s = new Forwarder(client.getOutputStream(), server.getInputStream());
                Thread s2c = new Forwarder(server.getOutputStream(), client.getInputStream());

                c2s.start();
                s2c.start();

                local.close();

            } catch (IOException e) {
                System.out.println("failed connection attempt " + (i + 1) + "/20");
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
