package de.zonlykroks.p2p4all.util;

import de.zonlykroks.p2p4all.client.P2P4AllClient;
import de.zonlykroks.p2p4all.client.screen.CreateScreen;
import de.zonlykroks.p2p4all.config.P2PYACLConfig;
import de.zonlykroks.p2p4all.event.MinecraftClientShutdownEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.DirectConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
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
        P2P4AllClient.ipToStateMap.put(targetIp,ConnectionProgress.PENDING);
        new Thread(() -> {
            try {
                int gamePort = areWeTheServer ? 25565 : 39332;

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

                CompletableFuture<Void> future = GoleExecutor.execute(new File(P2PYACLConfig.HANDLER.instance().golePath), targetIp, port1, port2, areWeTheServer, gamePort);
                //P2P4AllClient.currentlyRunningTunnels.put(targetIp,future);

                long wait = System.currentTimeMillis() + (150000);
                while (!future.isDone() && System.currentTimeMillis() <= wait) {
                    //Do nothing
                }

                if (!future.isDone()) {
                    future.cancel(true);
                    System.out.println("Failed to connect after 2 minutes and 30 seconds");
                    P2P4AllClient.ipToStateMap.put(targetIp,ConnectionProgress.FAILED);
                    return;
                }

                if(P2P4AllClient.ipToStateMap.get(targetIp) == ConnectionProgress.SUCCESS) {
                    if (!areWeTheServer) {
                        System.out.println("Connection established!\n\nWaiting for you to join @ 127.0.0.1:" + gamePort);
                        P2P4AllClient.SERVER_CONNECT_ADDRESS = "127.0.0.1:" + gamePort;
                        P2P4AllClient.ipToStateMap.put(targetIp,ConnectionProgress.SUCCESS);
                    } else {
                        System.out.println("Connection established!\nWaiting for the player to join");
                        P2P4AllClient.ipToStateMap.put(targetIp,ConnectionProgress.SUCCESS);
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
