package de.zonlykroks.p2p4all.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class P2PScreen extends Screen {
    private final Screen parent;
    private ImageButtonWidget createScreenButton;
    private ImageButtonWidget joinScreenButton;

    public P2PScreen(@Nullable Screen parent) {
        super(Text.translatable("p2p.screen.title"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.client.textRenderer, Text.translatable("p2p.screen.title"), this.width / 2, 10, 0xFFFFFF);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    protected void init() {
        super.init();

        int fontHeight = this.client.textRenderer.fontHeight;
        DynamicGridWidget grid = new DynamicGridWidget(15, 10 + fontHeight + 10, width - 20, height - 20 - fontHeight - 10 - 20);

        this.joinScreenButton = new ImageButtonWidget(0, 0, 0, 0, Text.translatable("p2p.screen.btn.join"), new Identifier("p2p", "textures/gui/join.webp"));
        this.createScreenButton = new ImageButtonWidget(0, 0, 0, 0, Text.translatable("p2p.screen.btn.create"), new Identifier("p2p", "textures/gui/create.webp"));

        this.createScreenButton.setClickEvent((btn) -> this.client.setScreen(new CreateScreen(this)));

        this.joinScreenButton.setClickEvent( (btn) -> this.client.setScreen(new JoinScreen(this)));

        grid.addChild(this.createScreenButton, 2, 1);
        grid.addChild(this.joinScreenButton, 2, 1);

        grid.setPadding(5);

        grid.calculateLayout();

        grid.forEachChild(this::addDrawableChild);

        ButtonWidget buttonWidget = ButtonWidget.builder(ScreenTexts.BACK, (btn) -> this.client.setScreen(this.parent)).dimensions(this.width / 2 - 100, this.height - 30, 200, 20).build();

        this.addDrawableChild(buttonWidget);
    }
}
