package de.zonlykroks.p2p4all.mixin;

import de.zonlykroks.p2p4all.api.MinecraftClientShutdownEvent;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
@Debug(export = true)
public class MinecraftClientMixin {

    @Inject(method = "close", at = @At("HEAD"))
    public void p2p4all$shutdownHook(CallbackInfo ci) {
        MinecraftClientShutdownEvent.SHUTDOWN.invoker().shutdown();
    }
}
