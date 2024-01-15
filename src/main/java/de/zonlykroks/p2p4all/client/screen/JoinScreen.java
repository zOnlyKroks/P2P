package de.zonlykroks.p2p4all.client.screen;

import de.zonlykroks.p2p4all.client.P2P4AllClient;
import de.zonlykroks.p2p4all.config.P2PYACLConfig;
import de.zonlykroks.p2p4all.net.Tunnel;
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
import java.net.SocketException;
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

        Tunnel tunnel = new Tunnel();
        try {
            tunnel.init(false);
        } catch (SocketException e) {
            e.printStackTrace();
            //TODO this is a fatal error, whats the clean way to abort here?
        }
        String localAddr = getPublicIP() + ":" + tunnel.getLocalPort();
        // dont hate me, this is cause java lambdas are kinda stupid
        P2P4AllClient.currentlyRunningTunnels.put("initializing", tunnel);

        IpFieldWidget ipFieldWidget = new IpFieldWidget(MinecraftClient.getInstance().textRenderer, (this.width / 2) - 100,40, 200,20, Text.translatable("p2p.btn.join.ip.preview"));
        PortFieldWidget portWidget = new PortFieldWidget(MinecraftClient.getInstance().textRenderer, (this.width / 2) - 100,80,200,20, Text.translatable("p2p.btn.join.port.preview"));

        this.addDrawableChild(ipFieldWidget);
        this.addDrawableChild(portWidget);

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.PROCEED, (button) -> {
            String ip = ipFieldWidget.getText();
            int port = Integer.parseInt(portWidget.getText());

            Tunnel t = P2P4AllClient.currentlyRunningTunnels.remove("initializing");
            t.setTarget(ip, port);
            P2P4AllClient.currentlyRunningTunnels.put(ip + ":" + port, t);

            new Thread(() -> {
                t.connect();
                t.createLocalTunnel();
            }).start();

            MinecraftClient.getInstance().setScreen(new ConnectionStateScreen(this, () -> {
                ServerInfo info = new ServerInfo("P2P", "localhost:25564", ServerInfo.ServerType.OTHER);
                MinecraftClient.getInstance().setScreen(new DirectConnectScreen(new MultiplayerScreen(this), b -> {
                    if(b) {
                        ConnectScreen.connect(this, this.client, ServerAddress.parse(info.address), info, false);
                    }
                }, info));
            }));
        }).dimensions(this.width / 2 - 155, 120, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, (button) -> {
            this.client.setScreen(this.parent);
        }).dimensions(this.width / 2 - 155 + 160,  120, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Your IP: " + localAddr), button -> {
            this.client.keyboard.setClipboard(localAddr);
        }).dimensions(
                (this.width / 2) - MinecraftClient.getInstance().textRenderer.getWidth("Your IP: " + localAddr),
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
