package de.zonlykroks.p2p4all.client.screen;

import de.zonlykroks.p2p4all.config.P2PConfig;
import de.zonlykroks.p2p4all.util.GoleExecutor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class P2PScreen extends Screen {
    public P2PScreen() {
        super(Text.literal("P2P, here be dragons!"));
    }

    @Override
    protected void init() {
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(5).marginBottom(4).alignHorizontalCenter();
        GridWidget.Adder adder = gridWidget.createAdder(2);

        adder.add(new TextWidget(Text.literal("Own IP: " + getPublicIP()), MinecraftClient.getInstance().textRenderer));

        EditBoxWidget targetIpWidget = new EditBoxWidget(MinecraftClient.getInstance().textRenderer, 0,0, 200,20, Text.literal("Target IP"), Text.empty());
        adder.add(targetIpWidget);
        targetIpWidget.setText(P2PConfig.TARGET_IP);

        adder.add(ButtonWidget.builder(P2PConfig.areYouTheServer ? Text.literal("Side: Server") : Text.literal("Side: Client"), buttonPressAction -> {
            P2PConfig.areYouTheServer = !P2PConfig.areYouTheServer;
            P2PConfig.write("p2p4all");

            if(P2PConfig.areYouTheServer) {
                buttonPressAction.setMessage(Text.literal("Side: Server"));
            }else {
                buttonPressAction.setMessage(Text.literal("Side: Client"));
            }
        }).width(100).build());

        EditBoxWidget passwordWidget = new EditBoxWidget(MinecraftClient.getInstance().textRenderer, 0,0, 200,20, Text.literal("Optional: Password (needs to match on all sides)"), Text.empty());
        adder.add(passwordWidget);
        passwordWidget.setText(P2PConfig.password);

        adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> {
            P2PConfig.TARGET_IP = targetIpWidget.getText();
            P2PConfig.password = passwordWidget.getText();
            P2PConfig.write("p2p4all");
            this.client.setScreen(new TitleScreen(true));

            try {
                startGole();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }).width(200).build(), 2, adder.copyPositioner().marginTop(6));

        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, this.height / 6 - 12, this.width, this.height, 0.5F, 0.0F);
        gridWidget.forEachChild(this::addDrawableChild);
    }

    private String getPublicIP(){
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));

            return in.readLine();
        }catch (Exception e) {
            e.printStackTrace();
            return "127.0.0.1";
        }
    }

    private void startGole() throws IOException {
        int gamePort = P2PConfig.areYouTheServer ? 25565 : 39332;

        try {
            InetAddress.getByName(P2PConfig.TARGET_IP);
        } catch (Exception ex) {
            System.out.println("Couldn't parse IP address \"" + P2PConfig.TARGET_IP + "\"");
            return;
        }

        int port = 40_000 + ( P2PConfig.password.isEmpty() ? 0 : P2PConfig.password.hashCode() % 20_000);
        int port1 = P2PConfig.areYouTheServer ? port + 1 : port;
        int port2 = P2PConfig.areYouTheServer ? port : port + 1;

        CompletableFuture<Void> future = GoleExecutor.execute(new File(P2PConfig.goleFilePath), "tcp", P2PConfig.TARGET_IP, port1, port2, !P2PConfig.areYouTheServer, gamePort);

        long wait = System.currentTimeMillis() + (150000);
        AtomicBoolean cancelled = new AtomicBoolean(false);

        ClientTickEvents.END_CLIENT_TICK.register(client1 -> {
            if(cancelled.get()) return;
            if(future.isDone() || System.currentTimeMillis() >= wait) {
                cancelled.set(true);
                future.cancel(true);
                if(MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.sendMessage(Text.literal("Failed to connect after 2 minutes and 30 seconds, relaunch server by going in and out of the p2p menu"));
                }
            }
        });

        if (!P2PConfig.areYouTheServer) {
            System.out.println("Connection established!\n\nWaiting for you to join @ 127.0.0.1:" + gamePort);
        } else {
            System.out.println("Connection established!\nWaiting for the player to join");
        }
    }
}
