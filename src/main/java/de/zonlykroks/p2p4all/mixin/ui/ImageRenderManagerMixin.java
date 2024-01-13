package de.zonlykroks.p2p4all.mixin.ui;

import dev.isxander.yacl3.gui.image.ImageRendererManager;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Pseudo
@Mixin(ImageRendererManager.class)
public class ImageRenderManagerMixin {
    @Mutable
    @Shadow @Final private static ExecutorService SINGLE_THREAD_EXECUTOR;

    static {
        // Don't do this if "Sounds" is loaded - as it also does this.
        if(!FabricLoader.getInstance().isModLoaded("sounds")) {
            SINGLE_THREAD_EXECUTOR = Executors.newFixedThreadPool(5, task -> new Thread(task, "YACL Image Prep (" + task.hashCode() + ")"));
        }
    }
}