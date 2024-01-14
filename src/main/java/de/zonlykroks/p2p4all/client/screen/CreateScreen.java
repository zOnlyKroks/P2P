package de.zonlykroks.p2p4all.client.screen;

import de.zonlykroks.p2p4all.client.P2P4AllClient;
import de.zonlykroks.p2p4all.config.P2PYACLConfig;
import de.zonlykroks.p2p4all.mixin.accessors.ScreenAccessor;
import de.zonlykroks.p2p4all.util.GoleDownloader;
import de.zonlykroks.p2p4all.util.GoleStarter;
import de.zonlykroks.p2p4all.util.LogginScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.QuickPlay;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.WorldIcon;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;

import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.lwjgl.opengl.GL20.*;


public class CreateScreen extends LogginScreen {
    private final Screen parent;
    private PortFieldWidget portNumber;
    private ButtonWidget createServerButton;
    private LevelSummary selectedWorld;
    private CompletableFuture<List<LevelSummary>> levelsFuture = null;
    private Optional<WorldIcon> worldIcon;
    private boolean shouldTunnel = false;

    public CreateScreen(Screen parent) {
        super(Text.translatable("p2p.screen.create.title"));
        this.parent = parent;
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    public void handleCreation() {
        P2P4AllClient.currentlyRunningTunnels.values().forEach(voidCompletableFuture -> voidCompletableFuture.cancel(true));
        P2P4AllClient.currentlyRunningTunnels.clear();
        // Then do your magic here.
        // Will gladly do

        new GoleDownloader();

        if(shouldTunnel) {
            for (int i = 0; i < P2PYACLConfig.get().savedIPs.size(); i++) {
                int port = Integer.parseInt(this.portNumber.getText()) + i;
                GoleStarter goleStarter = new GoleStarter(this,P2PYACLConfig.get().savedIPs.get(i), Integer.toString(port), true);
                goleStarter.start();
            }
        }

        Runnable startWorld = () -> QuickPlay.startQuickPlay(MinecraftClient.getInstance(), new RunArgs.QuickPlay(null,selectedWorld.getName(),"",""), null);

        if(shouldTunnel) {
            MinecraftClient.getInstance().setScreen(new ConnectionStateScreen(this, startWorld));
        } else {
            startWorld.run();
        }

    }

    @Override
    protected void init() {
        int startX = this.width / 2 - 50;

        this.portNumber = new PortFieldWidget(this.client.textRenderer, startX + 5, 10+this.textRenderer.fontHeight+20, 200, 20, Text.translatable("p2p.screen.create.port_number"));


        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, (btn) -> this.client.setScreen(this.parent)).dimensions(5, 5, this.textRenderer.getWidth(ScreenTexts.BACK) + 10, 20).build());

        this.createServerButton = ButtonWidget.builder(Text.translatable("p2p.screen.create.btn.create"), (btn) -> handleCreation()).dimensions(this.width - this.textRenderer.getWidth(Text.translatable("p2p.screen.create.btn.create")) - 15, 5, this.textRenderer.getWidth(Text.translatable("p2p.screen.create.btn.create")) + 10, 20).build();

        LevelStorage.LevelList saves = this.client.getLevelStorage().getLevelList();

        this.levelsFuture = client.getLevelStorage().loadSummaries(saves);
        this.worldIcon = Optional.empty();
        this.levelsFuture.thenAcceptAsync((val) -> {
            if(!val.isEmpty()) {
                this.selectedWorld = val.get(0);
                handleWorldIcon();
            }

            this.addDrawableChild(CyclingButtonWidget.<LevelSummary>builder(optVal -> Text.of(optVal.getDisplayName())).values(val).build(10, this.height - 10 - (this.textRenderer.fontHeight * 3) - 25, startX - 15, 20, Text.translatable("p2p.screen.create.world_select"), (button, value) -> {
                selectedWorld = value;
                handleWorldIcon();
            }));
        });

        this.addDrawableChild(portNumber);
        this.addDrawableChild(createServerButton);

        this.addDrawableChild(CyclingButtonWidget.<Boolean>builder(optVal -> {
            if(optVal) {
                return Text.translatable("p2p.screen.create.btn.public").formatted(Formatting.GREEN);
            } else {
                return Text.translatable("p2p.screen.create.btn.private").formatted(Formatting.RED);
            }
        }).values(true, false).initially(false).build(startX + 5, 10+this.textRenderer.fontHeight+20 + 25, 200, 20, Text.translatable("p2p.screen.create.btn.public_private"), (button, value) -> {
            this.shouldTunnel = value;
        }));

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("p2p.screen.config_button"), button -> {
            MinecraftClient.getInstance().setScreen(P2PYACLConfig.getInstance().generateScreen(this));
        }).dimensions(startX + 5, 10 + this.textRenderer.fontHeight + 20 + 25 + 25 + 25 + 10, 200, 20).build());
    }


    @Override
    public void tick() {
        super.tick();
        this.createServerButton.active = this.portNumber.isFullPort();
    }

    private void handleWorldIcon() {
        if(Files.exists(selectedWorld.getIconPath())) {
            this.worldIcon = Optional.of(WorldIcon.forWorld(this.client.getTextureManager(), this.selectedWorld.getName()));
            try {
                this.worldIcon.get().load(NativeImage.read(Files.newInputStream(selectedWorld.getIconPath())));
            } catch (Exception ignored) {
                this.worldIcon = Optional.empty();
            }
        } else {
            this.worldIcon = Optional.empty();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int startX = this.width / 2 - 50;

        this.renderBackground(context, mouseX, mouseY, delta);

        context.fill(5, 10 + textRenderer.fontHeight + 10, startX, this.height - 5, 0x77000000);
        context.drawBorder(5, 10 + textRenderer.fontHeight + 10, startX - 5, this.height - 5 - (10 + textRenderer.fontHeight + 10), 0x22000000);

        for (Drawable drawable : ((ScreenAccessor) this).getDrawables()) {
            drawable.render(context, mouseX, mouseY, delta);
        }

        Text worldInfoText = Text.translatable("p2p.screen.create.no_world_selected");

        if(selectedWorld != null)  {
            // Draw world name in that box.
            worldInfoText = selectedWorld.getDetails();
        }

        context.drawTextWrapped(textRenderer, worldInfoText, 10, this.height - 10 - (this.textRenderer.fontHeight * 3), startX - 15, 0xFFFFFF);

        if(levelsFuture != null) {
            if(!levelsFuture.isDone()) {
                context.drawText(textRenderer, "Loading Worlds...", 5, 10 + textRenderer.fontHeight + 10, 0x212121, false);
            }
        }

        // public void drawTexture(Identifier texture, int x, int y, int u, int v, int width, int height) {
        // Draw world icon in the box from x: 5, y: 5 to 5 above the button.

        context.enableScissor(15, textRenderer.fontHeight + 25, 15 + startX - 25, textRenderer.fontHeight + 25 + startX - 25);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        if(worldIcon.isEmpty()) {
            context.drawTexture(new Identifier("textures/misc/unknown_server.png"), 10, 10 + textRenderer.fontHeight + 10, 0, 0, startX - 15, startX - 15);
        } else {
            context.drawTexture(worldIcon.get().getTextureId(), 15, textRenderer.fontHeight + 25, 0, 0, startX - 25, startX - 25);
        }

        // Draw border around image.
        context.drawBorder(15, textRenderer.fontHeight + 25, startX - 25, startX - 25, 0x2FFFFFFF);
        context.disableScissor();

        context.drawCenteredTextWithShadow(textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);

        context.drawText(textRenderer, Text.translatable("p2p.screen.create.port_number"), startX + 5, 10+this.textRenderer.fontHeight+10, 0x777777, true);

        Text tunnelText = Text.translatable("p2p.screen.create.access_info");
        if(shouldTunnel) {
            tunnelText = Text.translatable("p2p.screen.create.access_info_tunnel");
        }

        context.drawTextWrapped(textRenderer, tunnelText, startX + 5, 10+this.textRenderer.fontHeight+20 + 50, 200, 0xFFFFFF);
    }
}
