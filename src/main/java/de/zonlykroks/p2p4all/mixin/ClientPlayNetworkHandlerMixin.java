package de.zonlykroks.p2p4all.mixin;

import de.zonlykroks.p2p4all.config.P2PYACLConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onGameJoin", at = @At("RETURN"))
    public void p2p4all$startLan(GameJoinS2CPacket packet, CallbackInfo ci) {
        if(MinecraftClient.getInstance().getServer() == null) return;
        MinecraftClient.getInstance().getServer().openToLan(
                GameMode.byName(P2PYACLConfig.get().lanGameMode.name,GameMode.DEFAULT),
                P2PYACLConfig.get().allowCheatsInLANWorld,
                P2PYACLConfig.get().internalLanPort);
    }

}
