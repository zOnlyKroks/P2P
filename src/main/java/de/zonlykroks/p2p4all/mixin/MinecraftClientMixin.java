package de.zonlykroks.p2p4all.mixin;

import de.zonlykroks.p2p4all.event.MinecraftClientShutdownEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.ApiServices;
import net.minecraft.world.GameMode;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow
    private IntegratedServer server;


    @Inject(
            method = "startIntegratedServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/QuickPlayLogger;setWorld(Lnet/minecraft/client/QuickPlayLogger$WorldType;Ljava/lang/String;Ljava/lang/String;)V",
                    shift = At.Shift.AFTER
            )
    )
    public void p2p4all$initLan(LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, boolean newWorld, CallbackInfo ci) {
        this.server.openToLan(GameMode.CREATIVE, true, 25565);
    }

    @Inject(method = "close", at = @At("HEAD"))
    public void p2p4all$shutdownHook(CallbackInfo ci) {
        MinecraftClientShutdownEvent.SHUTDOWN.invoker().shutdown();
    }

}
