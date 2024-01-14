package de.zonlykroks.p2p4all.util;


import de.zonlykroks.p2p4all.client.screen.CreateScreen;
import de.zonlykroks.p2p4all.config.P2PYACLConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GoleExecutor {

    private static final ExecutorService e = Executors.newCachedThreadPool();

    public static CompletableFuture<Void> execute(LogginScreen parent, File g,String addr2, int port1, int port2, boolean areWeTheServer, int gamePort) throws IOException {
        CompletableFuture<Void> future = new CompletableFuture<>();
        ProcessBuilder builder = new ProcessBuilder();

        String addr1 = "0.0.0.0";
        InetAddress address = InetAddress.getByName(addr2);
        if (address.isLoopbackAddress()) {
            addr1 = "127.0.0.1";
        } else if (address.isAnyLocalAddress()) {
            addr1 = InetAddress.getLocalHost().toString();
            while (addr1.indexOf('/') >= 0) {
                addr1 = addr1.substring(addr1.indexOf('/'));
            }
        }

        String mode = areWeTheServer ? "server" : "client";

        builder.command(g.getAbsolutePath(), "-v", "udp", addr1 + ":" + port1, addr2 + ":" + port2, "-op", mode, "-fwd=127.0.0.1:" + gamePort, "-proto=kcp");

        System.out.println(String.join(" ", builder.command()));
        builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        Process p = builder.start();
        future.exceptionally(ex -> {
            CompletableFuture.runAsync(p::destroy, e);
            return null;
        });

        CompletableFuture.runAsync(() -> {
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            try {
                line = br.readLine();
            } catch (Exception ex) {
                ex.printStackTrace();
                future.completeExceptionally(ex);
                return;
            }
            while (line != null) {
                //IF you want debug output
                if(P2PYACLConfig.get().verboseLogging) {
                    System.out.println(line);
                }

                if(!line.contains("send:") && !line.contains("wait")) {
                    parent.ipToStateMap.put(addr2,ConnectionProgress.PUNCHING);
                }

                if (line.toLowerCase().contains("wait") || line.toLowerCase().contains("tunnel created")) {
                    parent.ipToStateMap.put(addr2,ConnectionProgress.SUCCESS);
                }

                if (line.toLowerCase().contains("wait")) {
                    parent.ipToStateMap.put(addr2,ConnectionProgress.SUCCESS);
                    future.complete(null);
                }
                try {
                    line = br.readLine();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    future.completeExceptionally(ex);
                    return;
                }
            }
            future.completeExceptionally(new Exception("Reached end of program's output"));
        }, e);
        return future;
    }

}
