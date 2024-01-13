package de.zonlykroks.p2p4all.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameMenuScreen.class)
@Debug(export = true)
public class GameMenuScreenMixin extends Screen {

    @Unique
    private Widget p2pButtonWidget;

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;isInSingleplayer()Z", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    public void p2p4all$addCustomButton(CallbackInfo ci, GridWidget gridWidget, GridWidget.Adder adder) {
        this.p2pButtonWidget = ButtonWidget.builder(Text.literal("P2P"), (button) -> {
//            MinecraftClient.getInstance().setScreen(new GoleWarningScreen(this, true));
        }).width(204).build();

        adder.add(p2pButtonWidget, 2);
    }
}
