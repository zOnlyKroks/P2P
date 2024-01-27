package de.zonlykroks.p2p.util;

import de.zonlykroks.p2p.api.GoleAPIEvents;

import de.zonlykroks.p2p.client.P2PClient;
import de.zonlykroks.p2p.config.P2PYACLConfig;

import java.io.File;
import java.net.*;
import java.util.concurrent.CompletableFuture;

public class GoleStarter {

    private final String password;
    private final boolean areWeTheServer;

    private final String targetIp;

    public GoleStarter(String targetIP,String password, boolean areWeTheServer){
        this.password = password;
        this.areWeTheServer = areWeTheServer;
        this.targetIp = targetIP;
    }

    public void start(){
        GoleAPIEvents.IP_STATE_CHANGE.invoker().ipStateChange(targetIp, null , ConnectionProgress.PENDING);
        new Thread(() -> {
            try {
                int gamePort = areWeTheServer ? P2PYACLConfig.get().localServerPort : P2PYACLConfig.get().localClientGamePort;

                if(!checkIP()) return;

                int port = Integer.parseInt(password);
                int port1 = areWeTheServer ? port + 1 : port;
                int port2 = areWeTheServer ? port : port + 1;

                GoleProcess process = GoleExecutor.execute(new File(P2PYACLConfig.HANDLER.instance().golePath), targetIp, port1, port2, areWeTheServer, gamePort);
                CompletableFuture<Void> future = process.associatedCompletableFuture();

                P2PClient.currentlyRunningTunnels.put(targetIp, process);

                if(!handleWait(future)) return;

                finishConnection(gamePort);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void finishConnection(int gamePort) {
        if(P2PClient.ipToStateMap.get(targetIp) == ConnectionProgress.SUCCESS) {
            if (!areWeTheServer) {
                System.out.println("Connection established!\n\nWaiting for you to join @ 127.0.0.1:" + gamePort);
                P2PClient.SERVER_CONNECT_ADDRESS = "127.0.0.1:" + gamePort;
            } else {
                System.out.println("Connection established!\nWaiting for the player to join");
            }
        }
    }

    private boolean checkIP() {
        try {
            InetAddress.getByName(targetIp);
        } catch (Exception ex) {
            System.out.println("Couldn't parse IP address \"" + targetIp + "\"");
            P2PClient.ipToStateMap.put(targetIp,ConnectionProgress.FAILED);
            return false;
        }
        return true;
    }

    private boolean handleWait(CompletableFuture<Void> future) {
        long wait = System.currentTimeMillis() + (P2PYACLConfig.get().connectTimeoutInSeconds * 1000L);
        while (!future.isDone() && System.currentTimeMillis() <= wait) {
            //Do nothing
        }

        if (!future.isDone()) {
            future.cancel(true);
            System.out.println("Failed to connect after 2 minutes and 30 seconds");
            P2PClient.ipToStateMap.put(targetIp,ConnectionProgress.FAILED);
            GoleAPIEvents.IP_STATE_CHANGE.invoker().ipStateChange(targetIp, ConnectionProgress.PENDING , ConnectionProgress.FAILED);
            return false;
        }
        return true;
    }
}
