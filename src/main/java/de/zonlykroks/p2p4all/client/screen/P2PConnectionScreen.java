package de.zonlykroks.p2p4all.client.screen;

import de.zonlykroks.p2p4all.client.P2P4AllClient;
import de.zonlykroks.p2p4all.client.net.Tunnel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.util.Base64;

public class P2PConnectionScreen extends Screen {

    private final Screen parent;
    private final boolean isServer;

    public P2PConnectionScreen(Screen parent, boolean isServer) {
        super(Text.literal("P2P (here be dragons!)"));
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
            if(this.client != null) {
                this.client.keyboard.setClipboard(encodedIP);
            }
        }).width(200).build();
        adder.add(ipEditWidget);
        
        EditBoxWidget targetIdWidget = new EditBoxWidget(MinecraftClient.getInstance().textRenderer, 0,0, 200,20, Text.literal("Target ID"), Text.empty());
        adder.add(targetIdWidget);

        adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> {

            String destIp = new String(Base64.getDecoder().decode(targetIdWidget.getText().trim()));
            System.out.println(destIp);
            Tunnel tunnel = new Tunnel(40000, destIp, this.isServer);
            P2P4AllClient.TUNNEL = tunnel;

            if (this.isServer) {
                new Thread(() -> {
                    try {
                        tunnel.start();
                        tunnel.createLocalTunnel();
                    } catch (SocketException e) {
                        e.printStackTrace();
                        System.out.println("failed to setup connection");
                    }
                }).start();
            } else {
                try {
                    tunnel.start();
                    new Thread(() -> {
                        try {
                            tunnel.createLocalTunnel();
                        } catch (SocketException e) {
                            e.printStackTrace();
                            System.out.println("failed to set up local proxy");
                        }
                    }).start();
                    System.out.println("trying to connect through tunnel localhost:40001");
                    ServerAddress addr = new ServerAddress("localhost", 40001);
                    ServerInfo info = new ServerInfo("P2P", "localhost:40001", ServerInfo.ServerType.LAN);
                    ConnectScreen.connect(this, MinecraftClient.getInstance(), addr, info, false);

                } catch (SocketException e) {
                    e.printStackTrace();
                    System.out.println("failed to setup connection");

                }
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
            BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            return in.readLine();
        }catch (Exception e) {
            e.printStackTrace();
            return "127.0.0.1";
        }
    }

    private String encodeIpAddress(String ipAddress) {
        try {
            // Convert the IP address to bytes
            String ip = InetAddress.getByName(ipAddress).getHostAddress() + ":40000";

            // Encode the bytes to Base64
            return Base64.getEncoder().encodeToString(ip.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
