package de.zonlykroks.p2p4all.client.screen;

import de.zonlykroks.p2p4all.util.GoleDownloader;
import de.zonlykroks.p2p4all.util.GoleStarter;
import de.zonlykroks.p2p4all.util.LogginScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class JoinScreen extends LogginScreen {
    private static final Text PORT = Text.translatable("p2p.button.title.port");
    private static final Text IP = Text.translatable("p2p.button.title.ip");
    private final Screen parent;

    protected JoinScreen(Screen parent) {
        super(Text.translatable("p2p.screen.join.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        TextFieldWidget ipFieldWidget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, (this.width / 2) - 100,40, 200,20, Text.translatable("p2p.btn.join.ip.preview"));

        TextFieldWidget portWidget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, (this.width / 2) - 100,80,200,20, Text.translatable("p2p.btn.join.port.preview"));

        this.addDrawableChild(ipFieldWidget);
        this.addDrawableChild(portWidget);

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.PROCEED, (button) -> {
            String ip = ipFieldWidget.getText();
            new GoleDownloader();
            GoleStarter goleStarter = new GoleStarter(this, ip,portWidget.getText(),false);
            goleStarter.start();
        }).dimensions(this.width / 2 - 155, 120, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, (button) -> {
            this.client.setScreen(this.parent);
        }).dimensions(this.width / 2 - 155 + 160,  120, 150, 20).build());
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
}
