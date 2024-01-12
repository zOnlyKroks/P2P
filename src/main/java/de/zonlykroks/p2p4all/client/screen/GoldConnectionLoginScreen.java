package de.zonlykroks.p2p4all.client.screen;

import de.zonlykroks.p2p4all.client.P2P4AllClient;
import de.zonlykroks.p2p4all.config.P2PConfig;
import de.zonlykroks.p2p4all.event.MinecraftClientShutdownEvent;
import de.zonlykroks.p2p4all.util.GoleExecutor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WarningScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;

public class GoldConnectionLoginScreen extends WarningScreen {

    public static String connectionLog = "";

    private final Screen parent,where;

    private ButtonWidget doneButtonWidget;

    protected GoldConnectionLoginScreen(Screen parent, Screen where) {
        super(Text.literal("Trying to punch holes through your NAT"), Text.empty(), Text.empty());

        this.where = where;
        this.parent = parent;

        try {
            startGole();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void drawTitle(DrawContext context) {
        context.drawTextWithShadow(this.textRenderer, this.title, (this.width - MinecraftClient.getInstance().textRenderer.getWidth("Gole Installation Warning Screen")) / 2, 30, 16777215);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawTitle(context);

        MultilineText messageText = MultilineText.create(this.textRenderer, Text.literal(connectionLog), this.width);

        int i = this.width / 2 - messageText.getMaxWidth() / 2;
        messageText.drawWithShadow(context, i, 70, this.getLineHeight(), 16777215);
    }

    @Override
    protected void initButtons(int yOffset) {
        this.doneButtonWidget = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.PROCEED, button -> {
            try {
                MinecraftClient.getInstance().setScreen(this.where);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }).dimensions(this.width / 2 - 155, 100 + yOffset, 150, 20).build());

        this.doneButtonWidget.active = false;

        this.addDrawableChild(
                ButtonWidget.builder(ScreenTexts.BACK, button -> this.client.setScreen(this.parent)).dimensions(this.width / 2 - 155 + 160, 100 + yOffset, 150, 20).build()
        );
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
                System.out.println("Failed to connect after 2 minutes and 30 seconds");
                return;
            }

            if (!P2PConfig.areYouTheServer) {
                System.out.println("Connection established!\n\nWaiting for you to join @ 127.0.0.1:" + gamePort);
                GoldConnectionLoginScreen.connectionLog = GoldConnectionLoginScreen.connectionLog + "Connection established!\n\nWaiting for you to join @ 127.0.0.1:" + gamePort;
                P2P4AllClient.SERVER_CONNECT_ADDRESS = "127.0.0.1:" + gamePort;
            } else {
                System.out.println("Connection established!\nWaiting for the player to join");
                GoldConnectionLoginScreen.connectionLog = GoldConnectionLoginScreen.connectionLog + "Connection established!\nWaiting for the player to join";
                P2P4AllClient.SERVER_CONNECT_ADDRESS = "You are the Server, start a LAN world!";
            }

            doneButtonWidget.active = true;
        }).start();
    }
}
