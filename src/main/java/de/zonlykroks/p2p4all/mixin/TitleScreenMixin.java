package de.zonlykroks.p2p4all.mixin;

import de.zonlykroks.p2p4all.client.screen.P2PScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    public TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void p2p4all$addButton(CallbackInfo ci) {
        ButtonWidget p2pButton = this.addDrawableChild(ButtonWidget.builder(Text.literal("P2P"), button -> MinecraftClient.getInstance().setScreen(new P2PScreen(this))).width(22).build());

        int l = this.height / 4 + 48;
        int offset = 25;
        p2pButton.setPosition(this.width / 2 + 104 + offset, l + 72 + 12);
    }
}
