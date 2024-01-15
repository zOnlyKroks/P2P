package de.zonlykroks.p2p4all.net;

import de.zonlykroks.p2p4all.client.P2P4AllClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Tunnel {
    private InetSocketAddress target;
    private boolean isServer;
    private Socket remote;
    private Socket local;

    private Forwarder in;
    private Forwarder out;

    /*public Tunnel(String dest, boolean isServer) {
        this.isServer = isServer;

        String[] targetParts = dest.split(":");
        P2P4AllClient.LOGGER.debug(Arrays.toString(targetParts));
        this.target = new InetSocketAddress(targetParts[0], Integer.parseInt(targetParts[1]));
    }*/

    private Tunnel() {} // use Tunnel.init() below

    public static Tunnel init(boolean isServer, String targetAddress) throws SocketException {
        Tunnel tunnel = new Tunnel();
        tunnel.isServer = isServer;

        String[] target = targetAddress.split(":");
        tunnel.target = new InetSocketAddress(target[0], Integer.parseInt(target[1]));

        tunnel.remote = new Socket();
        tunnel.remote.setReuseAddress(true);

        for (int i = 0; i < 20; i++) {
            try {
                tunnel.remote.bind(new InetSocketAddress(40000 + i));
            } catch (Exception ignored) {}
        }

        if (!tunnel.remote.isBound()) {
            throw new SocketException("failed to find local port to bind to");
        }

        return tunnel;
    }

    public void connect() throws SocketException {
        for (int i = 0; i < 20; i++) {
            P2P4AllClient.LOGGER.debug("punch attempt {}/{}", i+1, 20);
            try {
                this.remote = new Socket();
                this.remote.setReuseAddress(true);
                this.remote.connect(target, isServer ? 2200 : 1800);
                P2P4AllClient.LOGGER.debug("connection established as client!");
                break;
            } catch (IOException e) {
                P2P4AllClient.LOGGER.debug("IOEx punch clientside bind: " + e.getMessage());
                try {
                    this.remote.close();
                    this.remote = null;
                } catch (IOException ex) {
                    P2P4AllClient.LOGGER.debug("IOEx punch clientside close: " + e.getMessage());
                }
            }

            try (ServerSocket s = new ServerSocket()) {
                s.setReuseAddress(true);
                s.setSoTimeout(isServer ? 3000 : 2500);
                this.remote = s.accept();
                P2P4AllClient.LOGGER.debug("connection established as server!");
                break;
            } catch (IOException e) {
                P2P4AllClient.LOGGER.debug("IOEx punch serverside bind: " + e.getMessage());
                try {
                    if (this.remote != null) this.remote.close();
                    this.remote = null;
                } catch (IOException ex) {
                    P2P4AllClient.LOGGER.debug("IOEx punch serverside close: " + ex.getMessage());
                }
            }
        }

        if (this.remote == null) {
            P2P4AllClient.LOGGER.debug("failed to setup tunnel, aborting");
            throw new SocketException("failed to setup tunnel");
        }
    }

    public void createLocalTunnel() throws SocketException {
        P2P4AllClient.LOGGER.debug("setting up local tunnel");
        if (this.isServer) {
            try {
                local.connect(new InetSocketAddress(25565));
            } catch (Exception e) {
                P2P4AllClient.LOGGER.debug("failed to connect local tunnel to LAN server: " + e.getMessage());
            }
        } else {
            // or here!
            try {
                ServerSocket localServer = new ServerSocket();
                for (int i = 0; i < 20; i++) {
                    try {
                        localServer.bind(new InetSocketAddress(remote.getLocalPort() + i));
                    } catch (IOException ignored) {}
                }
                if (!localServer.isBound()) throw new SocketException("failed to find port for local proxy");

                this.local = localServer.accept();
                localServer.close();
            } catch (IOException e) {
                P2P4AllClient.LOGGER.debug("failed to setup local tunnel: " + e.getMessage());
            }
        }

        if (!local.isConnected()) {
            P2P4AllClient.LOGGER.error("failed to setup tunnel, aborting");
            throw new SocketException("failed to setup tunnel");
        }

        try {
            this.in = new Forwarder(this.local.getOutputStream(), this.remote.getInputStream());
            this.out = new Forwarder(this.remote.getOutputStream(), this.local.getInputStream());
            in.start();
            out.start();
        } catch (IOException e) {
            P2P4AllClient.LOGGER.warn("stream forwarding failed to start: " + e.getMessage());
        }

        P2P4AllClient.LOGGER.debug("local tunnel established");
    }

    public void close() {
        try {
            this.in.isShuttingDown = true;
            this.out.isShuttingDown = true;
            if (!this.local.isClosed()) this.local.close();
            if (!this.remote.isClosed()) this.remote.close();

        } catch (IOException e) {
            P2P4AllClient.LOGGER.warn("did not shut down tunnel sockets correctly, ports might be unusable for a few minutes");
        }
    }
}
