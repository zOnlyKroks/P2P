package de.zonlykroks.p2p4all.net;

import de.zonlykroks.p2p4all.client.P2P4AllClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Forwarder extends Thread {

    private final InputStream is;
    private final OutputStream os;
    public boolean isShuttingDown;

    public Forwarder(OutputStream os, InputStream is) {
        this.is = is;
        this.os = os;
        this.isShuttingDown = false;
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
            if (!isShuttingDown) {
                P2P4AllClient.LOGGER.debug("Forwarding failed: " + e.getMessage());
            }
        }
    }
}