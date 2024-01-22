package de.zonlykroks.p2p.client.screen;

import de.zonlykroks.p2p.client.P2PClient;
import de.zonlykroks.p2p.util.ConnectionProgress;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionStateScreen extends Screen {
    private static final Text START_WORLD = Text.translatable("p2p.screen.button.start_world");
    private static final Text CANCEL_CONNECTION = Text.translatable("p2p.screen.button.cancel_connection");
    private static final String ESTABLISHED_CONNECTION = "p2p.screen.established_connections";
    private final Screen parent;
    private final Runnable runnable;
    private final long screenOpenTimeMillis;

    private ButtonWidget startWorldButton;
    private long establishedConnections = 0;

    public ConnectionStateScreen(Screen parent, @Nullable Runnable runnable) {
        super(Text.translatable("p2p.screen.server_connection_state"));
        this.parent = parent;
        this.runnable = runnable;
        this.screenOpenTimeMillis = System.currentTimeMillis();
    }

    @Override
    protected void init() {
        startWorldButton = ButtonWidget.builder(START_WORLD, (buttonWidget) -> runnable.run())
                .dimensions((this.width - this.textRenderer.getWidth(START_WORLD)) / 2, 100, this.textRenderer.getWidth(START_WORLD) + 10, 20)
                .build();
        startWorldButton.active = false;

        this.addDrawableChild(
                startWorldButton
        );

        this.addDrawableChild(
                ButtonWidget.builder(CANCEL_CONNECTION, buttonWidget -> {
                            this.close();
                            P2PClient.clearAllTunnels();
                        })
                        .dimensions((this.width - this.textRenderer.getWidth(CANCEL_CONNECTION)) / 2, 200, this.textRenderer.getWidth(CANCEL_CONNECTION) + 10, 20)
                        .build()
        );
    }

    @Override
    public void tick() {
        if(!startWorldButton.active && System.currentTimeMillis() - screenOpenTimeMillis > 10000 && this.establishedConnections > 0) {
            startWorldButton.active = true;
        }
        this.establishedConnections = P2PClient.ipToStateMap.values().stream().filter(connectionProgress -> connectionProgress == ConnectionProgress.SUCCESS).count();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(context);
        super.render(context, mouseX, mouseY, delta);
        assert this.client != null;
        int x = width / 40;
        AtomicInteger y = new AtomicInteger(height / 10);
        P2PClient.ipToStateMap.forEach((ip, connectionProgress) -> {
            context.drawText(client.textRenderer, ip, x, y.get(), 0xFFFFFF, false);
            connectionProgress.tryIncrementIndex();
            context.drawTexture(connectionProgress.getId(), x + 70, y.get(),0,0, connectionProgress.getWidth(), connectionProgress.getHeight());
            y.set(y.get() + 12);
        });
        context.drawText(
                client.textRenderer,
                Text.translatable(ESTABLISHED_CONNECTION, establishedConnections),
                (width - client.textRenderer.getWidth(Text.translatable(ESTABLISHED_CONNECTION, establishedConnections))) / 2,
                80 + textRenderer.fontHeight,
                0x00FF00,
                false
        );
    }

    @Override
    public void close() {
        assert this.client != null;
        this.client.setScreen(parent);
    }
}
