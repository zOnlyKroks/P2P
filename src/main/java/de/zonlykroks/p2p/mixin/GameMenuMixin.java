package de.zonlykroks.p2p.mixin;

import de.zonlykroks.p2p.client.screen.CreateScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameMenuScreen.class)
public class GameMenuMixin extends Screen {

    protected GameMenuMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/GridWidget;refreshPositions()V"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void p2p$injectMenuScreen(CallbackInfo ci, GridWidget gridWidget, GridWidget.Adder adder, Text text) {
        adder.add(ButtonWidget.builder(Text.translatable("p2p.button.ingame"), (press) -> this.client.setScreen(new CreateScreen(this, true))).width(204).build(), 2);
    }

}
