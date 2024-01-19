package de.zonlykroks.p2p4all.util;

import de.zonlykroks.p2p4all.api.GoleAPIEvents;

import de.zonlykroks.p2p4all.client.P2P4AllClient;
import de.zonlykroks.p2p4all.config.P2PYACLConfig;

import java.io.File;
import java.net.InetAddress;
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

                try {
                    InetAddress.getByName(targetIp);
                } catch (Exception ex) {
                    System.out.println("Couldn't parse IP address \"" + targetIp + "\"");
                    P2P4AllClient.ipToStateMap.put(targetIp,ConnectionProgress.FAILED);
                    return;
                }

                int port = Integer.parseInt(password);
                int port1 = areWeTheServer ? port + 1 : port;
                int port2 = areWeTheServer ? port : port + 1;

                GoleProcess process = GoleExecutor.execute(new File(P2PYACLConfig.HANDLER.instance().golePath), targetIp, port1, port2, areWeTheServer, gamePort);
                CompletableFuture<Void> future = process.associatedCompletableFuture();

                P2P4AllClient.currentlyRunningTunnels.put(targetIp, process);

                long wait = System.currentTimeMillis() + (150000);
                while (!future.isDone() && System.currentTimeMillis() <= wait) {
                    //Do nothing
                }

                if (!future.isDone()) {
                    future.cancel(true);
                    System.out.println("Failed to connect after 2 minutes and 30 seconds");
                    P2P4AllClient.ipToStateMap.put(targetIp,ConnectionProgress.FAILED);
                    GoleAPIEvents.IP_STATE_CHANGE.invoker().ipStateChange(targetIp, ConnectionProgress.PENDING , ConnectionProgress.FAILED);
                    return;
                }

                if(P2P4AllClient.ipToStateMap.get(targetIp) == ConnectionProgress.SUCCESS) {
                    if (!areWeTheServer) {
                        System.out.println("Connection established!\n\nWaiting for you to join @ 127.0.0.1:" + gamePort);
                        P2P4AllClient.SERVER_CONNECT_ADDRESS = "127.0.0.1:" + gamePort;
                    } else {
                        System.out.println("Connection established!\nWaiting for the player to join");
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
