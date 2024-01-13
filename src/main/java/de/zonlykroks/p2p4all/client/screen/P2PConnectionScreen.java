package de.zonlykroks.p2p4all.client.screen;

import de.zonlykroks.p2p4all.config.P2PConfig;
import de.zonlykroks.p2p4all.util.GoleStarter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

public class P2PConnectionScreen extends Screen {

    private final Screen parent;
    private final boolean isServer;

    public P2PConnectionScreen(Screen parent, boolean isServer) {
        super(Text.literal("P2P," + (isServer ? "Server" : "Client") + "version (here be dragons!)"));
        this.parent = parent;
        this.isServer = isServer;
    }

    @Override
    protected void init() {
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(5).marginBottom(4).alignHorizontalCenter();
        GridWidget.Adder adder = gridWidget.createAdder(2);

        String ip = getPublicIP();
        String encodedIP = encodeIpAddress(ip);

        ButtonWidget ipEditWidget = ButtonWidget.builder(Text.literal("Your ID: " + encodedIP), button -> {
            if (!java.awt.GraphicsEnvironment.isHeadless()) {
                StringSelection stringSelection = new StringSelection(encodedIP);

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

                clipboard.setContents(stringSelection, null);
            }
        }).width(200).build();
        adder.add(ipEditWidget);
        
        EditBoxWidget targetIpWidget = new EditBoxWidget(MinecraftClient.getInstance().textRenderer, 0,0, 200,20, Text.literal("Target ID"), Text.empty());
        adder.add(targetIpWidget);
        targetIpWidget.setText(P2PConfig.TARGET_IP);

        EditBoxWidget connectionID = new EditBoxWidget(MinecraftClient.getInstance().textRenderer, 0,0, 200,20, Text.literal("Connection ID (must match on both sides, unique for each client!)"), Text.empty());

        ButtonWidget randomizeConnectionID = ButtonWidget.builder(Text.literal("Randomize Connection ID"), button -> {
            Random random = new Random();
            connectionID.setText(random.nextInt(20000) + "");
        }).width(200).build();

        adder.add(randomizeConnectionID);

        adder.add(connectionID);
        connectionID.setText(P2PConfig.password);

        adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> {
            P2PConfig.TARGET_IP = targetIpWidget.getText();
            P2PConfig.password = connectionID.getText();
            P2PConfig.areYouTheServer = this.isServer;
            P2PConfig.write("p2p4all");

            try {
                new GoleStarter(this.parent, isServer ? new OpenToLanScreen(this.parent) : new MultiplayerScreen(this.parent));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).width(100).build(), 2, adder.copyPositioner().marginTop(6));

        adder.add(ButtonWidget.builder(ScreenTexts.CANCEL, button -> MinecraftClient.getInstance().setScreen(this.parent)).width(100).build(),2, adder.copyPositioner().marginTop(6));

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

    private String encodeIpAddress(String ipAddress) {
        try {
            // Convert the IP address to bytes
            byte[] ipBytes = InetAddress.getByName(ipAddress).getAddress();

            // Encode the bytes to Base64
            return Base64.getEncoder().encodeToString(ipBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
