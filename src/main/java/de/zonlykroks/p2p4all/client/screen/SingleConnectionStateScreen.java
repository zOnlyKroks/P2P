package de.zonlykroks.p2p4all.client.screen;

import de.zonlykroks.p2p4all.client.P2P4AllClient;
import de.zonlykroks.p2p4all.util.ConnectionProgress;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class SingleConnectionStateScreen extends Screen {
    private static final Text JOIN_WORLD = Text.translatable("p2p.screen.button.join_world");
    private static final Text CANCEL_CONNECTION = Text.translatable("p2p.screen.button.cancel_connection");
    private static final String ESTABLISHED_CONNECTION = "p2p.screen.established_connections";
    private final Screen parent;
    private final Runnable runnable;
    private final long screenOpenTimeMillis;

    private ButtonWidget joinWorldButton;
    private long establishedConnections = 0;

    public SingleConnectionStateScreen(Screen parent, @Nullable Runnable runnable) {
        super(Text.translatable("p2p.test"));
        this.parent = parent;
        this.runnable = runnable;
        this.screenOpenTimeMillis = System.currentTimeMillis();
    }

    @Override
    protected void init() {
        joinWorldButton = ButtonWidget.builder(JOIN_WORLD, (buttonWidget) -> runnable.run())
                .dimensions((this.width - this.textRenderer.getWidth(JOIN_WORLD)) / 2, 100, this.textRenderer.getWidth(JOIN_WORLD) + 10, 20)
                .build();
        joinWorldButton.active = false;

        this.addDrawableChild(
                joinWorldButton
        );

        this.addDrawableChild(
                ButtonWidget.builder(CANCEL_CONNECTION, buttonWidget -> {
                            this.close();
                            P2P4AllClient.clearAllTunnels();
                        })
                        .dimensions((this.width - this.textRenderer.getWidth(CANCEL_CONNECTION)) / 2, 130, this.textRenderer.getWidth(CANCEL_CONNECTION) + 10, 20)
                        .build()
        );
    }

    @Override
    public void tick() {
        if(!joinWorldButton.active && System.currentTimeMillis() - screenOpenTimeMillis > 10000 && this.establishedConnections > 0) {
            joinWorldButton.active = true;
        }
        this.establishedConnections = P2P4AllClient.ipToStateMap.values().stream().filter(connectionProgress -> connectionProgress == ConnectionProgress.SUCCESS).count();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        var ip = P2P4AllClient.ipToStateMap.keySet().stream().findFirst().get();
        int x = (width - textRenderer.getWidth(ip)) / 2;
        final int y = 90;
        var connectionProgress = P2P4AllClient.ipToStateMap.get(ip);
        context.drawText(client.textRenderer, ip, (width - textRenderer.getWidth(ip)) / 2, y, 0xFFFFFF, false);
        connectionProgress.tryIncrementIndex();
        context.drawGuiTexture(connectionProgress.getId(), (width + textRenderer.getWidth(ip)) / 2 + 1, y, connectionProgress.getWidth(), connectionProgress.getHeight());
        /**
        context.drawText(
                client.textRenderer,
                Text.translatable(ESTABLISHED_CONNECTION, establishedConnections),
                //Uhm this works, but why?
                (width - client.textRenderer.getWidth(Text.translatable(ESTABLISHED_CONNECTION, establishedConnections))) / 2,
                80 + textRenderer.fontHeight,
                0x00FF00,
                false
        );**/
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
