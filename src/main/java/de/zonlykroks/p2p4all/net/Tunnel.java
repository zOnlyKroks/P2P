package de.zonlykroks.p2p4all.net;

import de.zonlykroks.p2p4all.client.P2P4AllClient;
import de.zonlykroks.p2p4all.config.P2PYACLConfig;
import de.zonlykroks.p2p4all.util.ConnectionProgress;
import org.slf4j.event.Level;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Tunnel {
    private int sourcePort;
    private InetSocketAddress target;
    private boolean isServer;
    private Socket remote;
    private Socket local = new Socket();

    private Forwarder in;
    private Forwarder out;

    public void init(boolean isServer) throws SocketException {

        this.isServer = isServer;

        this.remote = new Socket();
        this.remote.setReuseAddress(true);

        for (int i = 0; i < 20; i++) {
            try {
                this.remote.bind(new InetSocketAddress(40000 + i));
            } catch (Exception ignored) {}
        }

        if (!this.remote.isBound()) {
            throw new SocketException("failed to find local port to bind to");
        } else {
            this.sourcePort = this.remote.getLocalPort();
            try { this.remote.close(); } catch (IOException e) {
                System.err.println("could not unbind unconnected port. this should not happen.");
            }
        }
    }

    public int getLocalPort() {
        return sourcePort;
    }

    public void setTarget(String targetIp, int targetPort) {
        this.target = new InetSocketAddress(targetIp, targetPort);
        P2P4AllClient.ipToStateMap.put(target.getHostString(), ConnectionProgress.PENDING);
    }

    private void log(Level level, String msg, Object... items) {
        P2P4AllClient.LOGGER.atLevel(level).log(
                target == null  ? msg : "[" + target.getHostString() + "] " + msg,
                items
        );
    }

    public void connect() {
        for (int i = 0; i < 20; i++) {
            log(Level.DEBUG, "punch attempt {}/{}", i+1, 20);
            P2P4AllClient.ipToStateMap.put(target.getHostString(), ConnectionProgress.PUNCHING);

            try {
                this.remote = new Socket();
                this.remote.setReuseAddress(true);
                this.remote.bind(new InetSocketAddress(sourcePort));
                this.remote.connect(target, isServer ? 2200 : 1800);
                log(Level.INFO, "connection tunnel established as client!");
                break;
            } catch (IOException e) {
                if (!e.getMessage().contains("timed out")) log(Level.WARN, "IOEx punch clientside bind: " + e.getMessage());
                try {
                    this.remote.close();
                    this.remote = null;
                } catch (IOException ex) {
                    log(Level.DEBUG, "IOEx punch clientside close: " + e.getMessage());
                }
            }

            try (ServerSocket s = new ServerSocket()) {
                s.setReuseAddress(true);
                s.bind(new InetSocketAddress(sourcePort));
                s.setSoTimeout(isServer ? 3000 : 2500);
                this.remote = s.accept();
                log(Level.INFO, "connection tunnel established as server!");
                break;
            } catch (IOException e) {
                if (!e.getMessage().contains("timed out")) log(Level.WARN, "IOEx punch serverside bind: " + e.getMessage());
                try {
                    if (this.remote != null) this.remote.close();
                    this.remote = null;
                } catch (IOException ex) {
                    log(Level.DEBUG, "IOEx punch serverside close: " + ex.getMessage());
                }
            }
        }

        if (this.remote == null) {
            log(Level.ERROR, "failed to setup tunnel, aborting");
            P2P4AllClient.ipToStateMap.put(target.getHostString(), ConnectionProgress.FAILED);
        } else {
            P2P4AllClient.ipToStateMap.put(target.getHostString(), ConnectionProgress.SUCCESS);
        }
    }

    public void createLocalTunnel() {
        if (this.remote == null) {
            log(Level.ERROR, "tried to create local tunnel without existing remote tunnel");
            P2P4AllClient.ipToStateMap.put(target.getHostString(), ConnectionProgress.FAILED);
            return;
        }

        log(Level.DEBUG, "setting up local tunnel");
        if (this.isServer) {
            try {
                local.connect(new InetSocketAddress(P2PYACLConfig.get().internalLanPort), 5000);
            } catch (Exception e) {
                log(Level.ERROR, "failed to connect local tunnel to LAN server: " + e.getMessage());
            }
        } else {
            // or here!
            try {
                ServerSocket localServer = new ServerSocket();
                for (int i = 0; i < 20; i++) {
                    try {
                        localServer.bind(new InetSocketAddress(P2PYACLConfig.get().internalLanPort));
                    } catch (IOException ignored) {}
                }
                if (!localServer.isBound()) {
                    log(Level.ERROR, "failed to find port for local proxy");
                    P2P4AllClient.ipToStateMap.put(target.getHostString(), ConnectionProgress.FAILED);
                    return;
                }

                this.local = localServer.accept();
                localServer.close();
            } catch (IOException e) {
                log(Level.ERROR, "failed to setup local tunnel: " + e.getMessage());
            }
        }

        if (!local.isConnected()) {
            log(Level.ERROR, "failed to setup tunnel, aborting");
            P2P4AllClient.ipToStateMap.put(target.getHostString(), ConnectionProgress.FAILED);
            return;
        }

        try {
            this.in = new Forwarder(this.local.getOutputStream(), this.remote.getInputStream());
            this.out = new Forwarder(this.remote.getOutputStream(), this.local.getInputStream());
            in.start();
            out.start();
        } catch (IOException e) {
            log(Level.ERROR, "stream forwarding failed to start: " + e.getMessage());
        }

        log(Level.INFO, "local tunnel established");
        P2P4AllClient.ipToStateMap.put(target.getHostString(), ConnectionProgress.SUCCESS);
    }

    public void close() {
        try {
            P2P4AllClient.LOGGER.info("shutting down tunnel...");
            if (this.in != null) this.in.isShuttingDown = true;
            if (this.out != null) this.out.isShuttingDown = true;
            if (this.local != null && !this.local.isClosed()) this.local.close();
            if (this.remote != null && !this.remote.isClosed()) this.remote.close();
            P2P4AllClient.LOGGER.info("tunnel shut down successfully");

        } catch (IOException e) {
            P2P4AllClient.LOGGER.warn("did not shut down tunnel sockets correctly, ports might be unusable for a few minutes");
        }
    }
}
