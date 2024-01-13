package de.zonlykroks.p2p4all.util;

import de.zonlykroks.p2p4all.client.P2P4AllClient;
import de.zonlykroks.p2p4all.client.screen.GoleLogScreen;
import de.zonlykroks.p2p4all.config.P2PConfig;
import de.zonlykroks.p2p4all.event.MinecraftClientShutdownEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

public class GoleStarter {

    private GoleLogScreen goleLogScreen;

    public GoleStarter(Screen parent, Screen where) throws IOException {
        int gamePort = P2PConfig.areYouTheServer ? 25565 : 39332;

        String targetIp = decodeIpAddress(P2PConfig.TARGET_IP);

        try {
            InetAddress.getByName(targetIp);
        } catch (Exception ex) {
            System.out.println("Couldn't parse IP address \"" + targetIp + "\"");
            return;
        }

        int port = 40_000 + ( P2PConfig.password.isEmpty() ? 0 : P2PConfig.password.hashCode() % 20_000);
        int port1 = P2PConfig.areYouTheServer ? port + 1 : port;
        int port2 = P2PConfig.areYouTheServer ? port : port + 1;

        CompletableFuture<Void> future = GoleExecutor.execute(new File(P2PConfig.goleFilePath), "tcp", targetIp, port1, port2, !P2PConfig.areYouTheServer, gamePort);

        this.goleLogScreen = new GoleLogScreen(parent, where, future);
        GoleLogScreen.connectionLog = "";
        MinecraftClient.getInstance().setScreen(this.goleLogScreen);

        MinecraftClientShutdownEvent.SHUTDOWN.register(() -> {
            future.cancel(true);
        });

        new Thread(() -> {
            long wait = System.currentTimeMillis() + (150000);
            while (!future.isDone() && System.currentTimeMillis() <= wait) {
                //Do nothing
            }

            if (!future.isDone()) {
                future.cancel(true);
                GoleLogScreen.connectionLog = GoleLogScreen.connectionLog + "Failed to connect after 2 minutes and 30 seconds\n";
                System.out.println("Failed to connect after 2 minutes and 30 seconds");
                return;
            }

            if (!P2PConfig.areYouTheServer) {
                System.out.println("Connection established!\n\nWaiting for you to join @ 127.0.0.1:" + gamePort);
                GoleLogScreen.connectionLog = GoleLogScreen.connectionLog + "Connection established!\n\nWaiting for you to join @ 127.0.0.1:" + gamePort;
                P2P4AllClient.SERVER_CONNECT_ADDRESS = "127.0.0.1:" + gamePort;
            } else {
                System.out.println("Connection established!\nWaiting for the player to join");
                GoleLogScreen.connectionLog = GoleLogScreen.connectionLog + "Connection established!\nWaiting for the player to join";
                P2P4AllClient.SERVER_CONNECT_ADDRESS = "You are the Server, start a LAN world!";
            }
        }).start();
    }

    private String decodeIpAddress(String encodedIpAddress) {
        try {
            // Decode Base64 to bytes
            byte[] ipBytes = Base64.getDecoder().decode(encodedIpAddress);

            // Convert bytes to InetAddress and get the IP address
            InetAddress inetAddress = InetAddress.getByAddress(ipBytes);
            return inetAddress.getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
