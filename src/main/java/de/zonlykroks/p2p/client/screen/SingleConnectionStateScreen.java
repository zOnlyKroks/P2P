package de.zonlykroks.p2p.client.screen;

import de.zonlykroks.p2p.client.P2PClient;
import de.zonlykroks.p2p.util.ConnectionProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class SingleConnectionStateScreen extends Screen {
    private static final Identifier ICONS_TEXTURE = new Identifier("textures/gui/icons.png");
    private static final Text JOIN_WORLD = Text.translatable("p2p.screen.button.join_world");
    private static final Text CANCEL_CONNECTION = Text.translatable("p2p.screen.button.cancel_connection");
    private final Screen parent;
    private final Runnable runnable;
    private final long screenOpenTimeMillis;

    private ButtonWidget joinWorldButton;
    private long establishedConnections = 0;

    public SingleConnectionStateScreen(Screen parent, @Nullable Runnable runnable) {
        super(Text.translatable("p2p.screen.single_connection_screen"));
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
                            P2PClient.clearAllTunnels();
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
        this.establishedConnections = P2PClient.ipToStateMap.values().stream().filter(connectionProgress -> connectionProgress == ConnectionProgress.SUCCESS).count();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(context);
        super.render(context, mouseX, mouseY, delta);
        assert this.client != null;

        var ip = P2PClient.ipToStateMap.keySet().toArray(String[]::new)[0];
        final int y = 90;
        var connectionProgress = P2PClient.ipToStateMap.values().toArray(ConnectionProgress[]::new)[0];
        context.drawText(client.textRenderer, ip, (width - textRenderer.getWidth(ip)) / 2, y, 0xFFFFFF, false);
        connectionProgress.tryIncrementIndex();
        context.drawTexture(ICONS_TEXTURE, (width + textRenderer.getWidth(ip)) / 2 + 1, y,connectionProgress.getId().getLeft(),connectionProgress.getId().getRight(), connectionProgress.getWidth(), connectionProgress.getHeight());
    }

    @Override
    public void close() {
        assert this.client != null;
        this.client.setScreen(parent);
    }
}
