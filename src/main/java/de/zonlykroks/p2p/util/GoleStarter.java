package de.zonlykroks.p2p.util;

import de.zonlykroks.p2p.api.GoleAPIEvents;

import de.zonlykroks.p2p.client.P2PClient;
import de.zonlykroks.p2p.config.P2PYACLConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
                if(!validateIPAddress()) return;

                int gamePort = areWeTheServer ? P2PYACLConfig.get().localServerPort : P2PYACLConfig.get().localClientGamePort;

                int port = Integer.parseInt(password);
                int port1 = areWeTheServer ? port + 1 : port;
                int port2 = areWeTheServer ? port : port + 1;

                GoleProcess process = GoleExecutor.execute(new File(P2PYACLConfig.HANDLER.instance().golePath), targetIp, port1, port2, areWeTheServer, gamePort);
                CompletableFuture<Void> future = process.associatedCompletableFuture();

                P2PClient.currentlyRunningTunnels.put(targetIp, process);

                if(!handleWait(future)) return;

                handleFinish(gamePort, port1, port2);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
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

    private void handleFinish(int gamePort, int port1, int port2) {
        if(P2PClient.ipToStateMap.get(targetIp) == ConnectionProgress.SUCCESS) {
            if (!areWeTheServer) {
                System.out.println("Connection established!\n\nWaiting for you to join @ 127.0.0.1:" + gamePort);
                P2PClient.SERVER_CONNECT_ADDRESS = "127.0.0.1:" + gamePort;

                handleClient(port2);
            } else {
                System.out.println("Connection established!\nWaiting for the player to join");

                handleServer(port1);
            }
        }
    }

    private boolean validateIPAddress() {
        try {
            InetAddress.getByName(targetIp);
            return true;
        } catch (Exception ex) {
            System.out.println("Couldn't parse IP address \"" + targetIp + "\"");
            P2PClient.ipToStateMap.put(targetIp,ConnectionProgress.FAILED);
            return false;
        }
    }

    private void handleClient(int port2) {
        new Thread(() -> {
            try {
                Thread.sleep(6000);
                System.out.println("A");

                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(InetAddress.getByName(targetIp), port2));
                System.out.println(socket.getInetAddress() + ":" + port2);

                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                writer.println("TEST!");
                System.out.println("B");
                System.exit(-1);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

    private void handleServer(int port1) {
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(port1);
                System.out.println(serverSocket.getInetAddress() + ":" + port1);
                Socket sck = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(sck.getInputStream()));

                System.out.println("A");
                System.out.println(reader.readLine());
                System.out.println("B");
                System.exit(-1);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
