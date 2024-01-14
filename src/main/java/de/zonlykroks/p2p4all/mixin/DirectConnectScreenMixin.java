package de.zonlykroks.p2p4all.mixin;

import net.minecraft.client.gui.screen.multiplayer.DirectConnectScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DirectConnectScreen.class)
public class DirectConnectScreenMixin {

    @Shadow
    private TextFieldWidget addressField;

    @Inject(method = "init", at = @At("TAIL"))
    public void p2p4all$changeToP2PIP(CallbackInfo ci) {
        addressField.setText("127.0.0.1:39332");
    }

}
