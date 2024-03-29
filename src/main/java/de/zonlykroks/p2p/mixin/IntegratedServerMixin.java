package de.zonlykroks.p2p.mixin;

import de.zonlykroks.p2p.client.P2PClient;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IntegratedServer.class)
public class IntegratedServerMixin {

    @Inject(method = "shutdown", at = @At("HEAD"))
    public void p2p4all$shutdownInternalServer(CallbackInfo ci) {
        P2PClient.clearAllTunnels(false);
    }

}
