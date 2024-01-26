package de.zonlykroks.p2p.util;


import de.zonlykroks.p2p.api.GoleAPIEvents;
import de.zonlykroks.p2p.config.P2PYACLConfig;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GoleExecutor {

    private static final ExecutorService e = Executors.newCachedThreadPool();

    public static @NotNull GoleProcess execute(File g, String addr2, int port1, int port2, boolean areWeTheServer, int gamePort) throws IOException {
        CompletableFuture<Void> future = new CompletableFuture<>();
        ProcessBuilder builder = new ProcessBuilder();

        String addr1 = calculateAddress1(addr2);

        Set<PosixFilePermission> ownerWritable = PosixFilePermissions.fromString("rwxrwxrwx");
        Files.setPosixFilePermissions(g.toPath(), ownerWritable);

        builder.command(g.getAbsolutePath(), "-v",
                "udp",
                addr1 + ":" + port1,
                addr2 + ":" + port2,
                "-op",
                areWeTheServer ? "server" : "client",
                "-fwd=127.0.0.1:" + gamePort,
                "-proto=kcp");

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
                    GoleAPIEvents.IP_STATE_CHANGE.invoker().ipStateChange(addr2, ConnectionProgress.PENDING , ConnectionProgress.PUNCHING);
                }

                if (line.toLowerCase().contains("wait") || line.toLowerCase().contains("tunnel created")) {
                    GoleAPIEvents.IP_STATE_CHANGE.invoker().ipStateChange(addr2, ConnectionProgress.PUNCHING , ConnectionProgress.SUCCESS);
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
        return new GoleProcess(p, future);
    }

    private static String calculateAddress1(String addr2) throws IOException {
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
        return addr1;
    }
}
