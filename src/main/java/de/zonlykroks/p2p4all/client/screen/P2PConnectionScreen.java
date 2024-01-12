package de.zonlykroks.p2p4all.client.screen;

import de.zonlykroks.p2p4all.config.P2PConfig;
import de.zonlykroks.p2p4all.util.GoleStarter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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

        adder.add(new TextWidget(Text.literal("Your ID: " + Base64.getEncoder().encodeToString(getPublicIP().getBytes(StandardCharsets.UTF_8))), MinecraftClient.getInstance().textRenderer));

        EditBoxWidget targetIpWidget = new EditBoxWidget(MinecraftClient.getInstance().textRenderer, 0,0, 200,20, Text.literal("Target ID"), Text.empty());
        adder.add(targetIpWidget);
        targetIpWidget.setText(P2PConfig.TARGET_IP);

        EmptyWidget emptyWidget = new EmptyWidget(0,0);
        adder.add(emptyWidget);

        EditBoxWidget passwordWidget = new EditBoxWidget(MinecraftClient.getInstance().textRenderer, 0,0, 200,20, Text.literal(" Target ID (must match on both sides, unique for each client!)"), Text.empty());
        adder.add(passwordWidget);
        passwordWidget.setText(P2PConfig.password);

        adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> {
            P2PConfig.TARGET_IP = new String(Base64.getDecoder().decode(targetIpWidget.getText().getBytes(StandardCharsets.UTF_8)));
            P2PConfig.password = passwordWidget.getText();
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
}
