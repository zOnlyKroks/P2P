package de.zonlykroks.p2p4all.mixin;

import de.zonlykroks.p2p4all.config.P2PConfig;
import de.zonlykroks.p2p4all.util.GoleDownloader;
import de.zonlykroks.p2p4all.util.GoleExecutor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.world.timer.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;

@Mixin(OpenToLanScreen.class)
public abstract class OpenToLanScreenMixin extends Screen {

    public OpenToLanScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onLanInit(Screen screen, CallbackInfo ci) {
        try {
            startGole();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Unique
    private void startGole() throws IOException {
        int gamePort = 25565;

        try {
            InetAddress.getByName(P2PConfig.TARGET_IP);
        } catch (Exception ex) {
           System.out.println("Couldn't parse IP address \"" + P2PConfig.TARGET_IP + "\"");
           return;
        }

        int port = 40_000 + ( P2PConfig.password.isEmpty() ? 25565 : P2PConfig.password.hashCode() % 20_000);
        int port1 = P2PConfig.areYouTheServer ? port + 1 : port;
        int port2 = P2PConfig.areYouTheServer ? port : port + 1;

        CompletableFuture<Void> future = GoleExecutor.execute(new File(P2PConfig.goleFilePath), "tcp", P2PConfig.TARGET_IP, port1, port2, !P2PConfig.areYouTheServer, gamePort);


        long wait = System.currentTimeMillis() + (15000);
        ClientTickEvents.END_CLIENT_TICK.register(client1 -> {
            if(future.isDone() || System.currentTimeMillis() >= wait) {
                future.cancel(true);
                throw new RuntimeException("Failed to connect after 2 minutes and 30 seconds");
            }
        });

        if (!P2PConfig.areYouTheServer) {
            System.out.println("Connection established!\n\nWaiting for you to join @ 127.0.0.1:" + gamePort);
        } else {
            System.out.println("Connection established!\nWaiting for the player to join");
        }
    }
}
