package de.zonlykroks.p2p.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.QuickPlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(QuickPlay.class)
public interface QuickPlayInvoker {

    @Invoker("startSingleplayer")
    static void startSingleplayer(MinecraftClient client, String levelName) {

    }

}
