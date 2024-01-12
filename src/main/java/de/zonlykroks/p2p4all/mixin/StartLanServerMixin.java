package de.zonlykroks.p2p4all.mixin;

import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OpenToLanScreen.class)
public class StartLanServerMixin {
    @Shadow
    private int port;


    @Inject(method = "method_19851(Lnet/minecraft/server/integrated/IntegratedServer;Lnet/minecraft/client/gui/widget/ButtonWidget;)V", at = @At("HEAD"))
    public void p2p4all$changePort(IntegratedServer integratedServer, ButtonWidget button, CallbackInfo ci) {
        this.port = 25565;
    }
}
