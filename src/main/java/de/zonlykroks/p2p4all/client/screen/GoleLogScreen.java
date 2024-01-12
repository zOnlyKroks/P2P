package de.zonlykroks.p2p4all.client.screen;


import de.zonlykroks.p2p4all.util.GoleDownloader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WarningScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

public class GoleLogScreen extends WarningScreen {

    public static String connectionLog = "";

    private final Screen parent,where;
    private ButtonWidget buttonWidget;

    private final CompletableFuture<Void> future;

    public GoleLogScreen(Screen parent, Screen where, CompletableFuture<Void> future) {
        super(Text.literal("Gole Connection Log"), Text.empty(), Text.empty());
        this.parent = parent;
        this.where = where;
        this.future = future;
    }

    private int timer = 0;

    @Override
    public void tick() {
        timer++;

        if(timer >= 1500) {
            timer = 0;
            GoleLogScreen.connectionLog = "";
        }

        if(future.isDone()) {
            this.buttonWidget.active = true;
        }
    }

    @Override
    protected void drawTitle(DrawContext context) {
        context.drawTextWithShadow(this.textRenderer, this.title, (this.width - MinecraftClient.getInstance().textRenderer.getWidth("Gole Connection Log")) / 2, 30, 16777215);
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
        this.buttonWidget = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.PROCEED, button -> {
            MinecraftClient.getInstance().setScreen(this.where);
        }).dimensions(this.width / 2 - 155, 100 + yOffset, 150, 20).build());

        this.buttonWidget.active = false;

        this.addDrawableChild(
                ButtonWidget.builder(ScreenTexts.BACK, button -> {
                    this.client.setScreen(this.parent);
                    this.future.cancel(true);
                }).dimensions(this.width / 2 - 155 + 160, 100 + yOffset, 150, 20).build()
        );
    }
}
