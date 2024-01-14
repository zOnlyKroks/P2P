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
    private Socket remote;
    private Socket local = new Socket();

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
        
        InetSocketAddress localAddr = new InetSocketAddress(sourcePort);

        for (int i = 0; i < 20; i++) {
            System.out.printf("punch attempt %d / %d\n", i+1, 20);
            try {
                this.remote = new Socket();
                this.remote.setReuseAddress(true);
                this.remote.bind(localAddr);
                this.remote.connect(target, isServer ? 2200 : 1800);
                System.out.println("connection established as client!");
                break;
            } catch (IOException e) {
                System.out.println("IOEx punch clientside bind: " + e.getMessage());
                try {
                    this.remote.close();
                    this.remote = null;
                } catch (IOException ex) {
                    System.out.println("IOEx punch clientside close: " + e.getMessage());
                }
            }

            try (ServerSocket s = new ServerSocket()) {
                s.setReuseAddress(true);
                s.setSoTimeout(isServer ? 3000 : 2500);
                s.bind(localAddr);
                this.remote = s.accept();
                System.out.println("connection established as server!");
                break;
            } catch (IOException e) {
                System.out.println("IOEx punch serverside bind: " + e.getMessage());
                try {
                    if (this.remote != null) this.remote.close();
                    this.remote = null;
                } catch (IOException ex) {
                    System.out.println("IOEx punch serverside close: " + ex.getMessage());
                }
            }
        }

        if (this.remote == null) {
            System.out.println("failed to setup tunnel, aborting");
            throw new SocketException("failed to setup tunnel");
        }
    }

    public void createLocalTunnel() throws SocketException {
        System.out.println("setting up local tunnel");
        if (this.isServer) {
            try {
                local.connect(new InetSocketAddress(25565));
            } catch (Exception e) {
                System.out.println("failed to connect local tunnel to LAN server: " + e.getMessage());
            }
        } else {
            // or here!
            try {
                ServerSocket localServer = new ServerSocket(sourcePort + 1);
                this.local = localServer.accept();
                localServer.close();
            } catch (IOException e) {
                System.out.println("failed to setup local tunnel: " + e.getMessage());
            }
        }

        if (!local.isConnected()) {
            System.out.println("failed to setup tunnel, aborting");
            throw new SocketException("failed to setup tunnel");
        }

        try {
            Thread out = new Forwarder(local.getOutputStream(), this.remote.getInputStream());
            Thread in  = new Forwarder(this.remote.getOutputStream(), local.getInputStream());

            out.start();
            in.start();
        } catch (IOException e) {
            System.out.println("stream forwarding failed: " + e.getMessage());
        }

        System.out.println("local tunnel established");
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
