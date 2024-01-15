package de.zonlykroks.p2p4all.client.screen;

import de.zonlykroks.p2p4all.client.P2P4AllClient;
import de.zonlykroks.p2p4all.config.P2PYACLConfig;
import de.zonlykroks.p2p4all.util.GoleDownloader;
import de.zonlykroks.p2p4all.util.GoleStarter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.DirectConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class JoinScreen extends Screen {
    private static final Text PORT = Text.translatable("p2p.button.title.port");
    private static final Text IP = Text.translatable("p2p.button.title.ip");
    private final Screen parent;

    protected JoinScreen(Screen parent) {
        super(Text.translatable("p2p.screen.join.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        P2P4AllClient.ipToStateMap.clear();
        P2P4AllClient.clearAllTunnels();

        IpFieldWidget ipFieldWidget = new IpFieldWidget(MinecraftClient.getInstance().textRenderer, (this.width / 2) - 100,40, 200,20, Text.translatable("p2p.btn.join.ip.preview"));

        PortFieldWidget portWidget = new PortFieldWidget(MinecraftClient.getInstance().textRenderer, (this.width / 2) - 100,80,200,20, Text.translatable("p2p.btn.join.port.preview"));

        this.addDrawableChild(ipFieldWidget);
        this.addDrawableChild(portWidget);

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.PROCEED, (button) -> {
            new GoleDownloader();

            String ip = ipFieldWidget.getText();

            GoleStarter goleStarter = new GoleStarter(ip,portWidget.getText(),false);
            goleStarter.start();

            ServerInfo info = new ServerInfo("P2P", "127.0.0.1:39332", ServerInfo.ServerType.OTHER);

            MinecraftClient.getInstance().setScreen(new ConnectionStateScreen(this, () -> {
                MinecraftClient.getInstance().setScreen(new DirectConnectScreen(new MultiplayerScreen(this), b -> {
                    if(b) {
                        ConnectScreen.connect(this, this.client, ServerAddress.parse(info.address), info, false);
                    }
                }, info));
            }));
        }).dimensions(this.width / 2 - 100, 120, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, (button) -> {
            this.client.setScreen(this.parent);
        }).dimensions(this.width / 2,  120, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Your IP: " + getPublicIP()), button -> this.client.keyboard.setClipboard(getPublicIP())).dimensions(
                (this.width / 2) - MinecraftClient.getInstance().textRenderer.getWidth("Your IP: " + getPublicIP()),
                120 + textRenderer.fontHeight + 20 + 10,
                200,
                20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int x = this.width / 2 - 100;
        context.drawText(
                client.textRenderer,
                IP,
                //TODO: Uhm this works, but why?
                x,
                20 + textRenderer.fontHeight,
                0xFFFFFF,
                false
        );

        context.drawText(
                client.textRenderer,
                PORT,
                //TODO: Uhm this works, but why?
                x,
                60 + textRenderer.fontHeight,
                0xFFFFFF,
                false
        );
    }

    private String getPublicIP() {
        try {
            URL ip = new URL(P2PYACLConfig.get().ipPingService);
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    ip.openStream()));

            return in.readLine();
        }catch (Exception e) {
            return "x.x.x.x";
        }
    }
}
