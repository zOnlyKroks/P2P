package de.zonlykroks.p2p.mixin;

import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClickableWidget.class)
public interface ClickableWidgetAccessor {

    @Accessor("height")
    void setHeight(int height);

}
