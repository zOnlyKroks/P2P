package de.zonlykroks.p2p4all.client.screen;

import de.zonlykroks.p2p4all.util.GoleDownloader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WarningScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class GoleWarningScreen extends WarningScreen {

    private final Screen parent;

    public GoleWarningScreen(Screen parent) {
        super(Text.literal("Gole Installation Warning Screen"),
                Text.literal("Once you click on proceed, we WILL download an EXTERNAL programm called, gole (https://github.com/shawwwn/Gole), if you want to download it yourself, close this window, close the game and put the .exe corresponding to your operating system into the config folder of this mod."),
                Text.empty());
        this.parent = parent;
    }

    @Override
    protected void drawTitle(DrawContext context) {
        context.drawTextWithShadow(this.textRenderer, this.title, (this.width - MinecraftClient.getInstance().textRenderer.getWidth("Gole Installation Warning Screen")) / 2, 30, 16777215);
    }

    @Override
    protected void initButtons(int yOffset) {
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.PROCEED, button -> {
            try {
                new GoleDownloader();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }).dimensions(this.width / 2 - 155, 100 + yOffset, 150, 20).build());

        this.addDrawableChild(
                ButtonWidget.builder(ScreenTexts.BACK, button -> this.client.setScreen(this.parent)).dimensions(this.width / 2 - 155 + 160, 100 + yOffset, 150, 20).build()
        );
    }
}
