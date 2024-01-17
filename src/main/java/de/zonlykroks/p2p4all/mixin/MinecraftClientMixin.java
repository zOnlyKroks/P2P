package de.zonlykroks.p2p4all.mixin;

import de.zonlykroks.p2p4all.event.MinecraftClientShutdownEvent;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "close", at = @At("HEAD"))
    public void p2p4all$shutdownHook(CallbackInfo ci) {
        MinecraftClientShutdownEvent.SHUTDOWN.invoker().shutdown();
    }

}
