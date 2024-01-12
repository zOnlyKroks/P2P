package de.zonlykroks.p2p4all.mixin;

import de.zonlykroks.p2p4all.client.P2P4AllClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.DirectConnectScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DirectConnectScreen.class)
public abstract class DirectConnectScreenMixin extends Screen {

    @Shadow
    private TextFieldWidget addressField;

    protected DirectConnectScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void p2p4all$injectP2PServerAddress(CallbackInfo ci) {
        if(P2P4AllClient.SERVER_CONNECT_ADDRESS != null) {
            this.addressField.setText(P2P4AllClient.SERVER_CONNECT_ADDRESS);
        }
    }
}
