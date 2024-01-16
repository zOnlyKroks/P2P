package de.zonlykroks.p2p4all.mixin;

import de.zonlykroks.p2p4all.client.P2P4AllClient;
import de.zonlykroks.p2p4all.net.Tunnel;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IntegratedServer.class)
public class IntegratedServerMixin {

    @Inject(method = "shutdown", at = @At("HEAD"))
    public void p2p4all$shutdownInternalServer(CallbackInfo ci) {
        P2P4AllClient.currentlyRunningTunnels.values().forEach(Tunnel::close);
        P2P4AllClient.currentlyRunningTunnels.clear();
    }


    @Inject(method = "openToLan", at = @At(value = "RETURN", ordinal = 0))
    public void p2p4all$connectTunnels(GameMode gameMode, boolean cheatsAllowed, int port, CallbackInfoReturnable<Boolean> cir) {
        new Thread(() -> {
           for (Tunnel t : P2P4AllClient.currentlyRunningTunnels.values()) {
               t.createLocalTunnel();
           }
        });
    }
}
