package de.zonlykroks.p2p4all.client;

import de.zonlykroks.p2p4all.config.P2PYACLConfig;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceType;

public class P2P4AllClient implements ClientModInitializer {

    public static String SERVER_CONNECT_ADDRESS = null;


    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        P2PYACLConfig.load();

        // Don't do this if "Sounds" is loaded - as it also does this.
        if(FabricLoader.getInstance().isModLoaded("sounds")) {
            ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new YACLImageReloadListenerFabric());
        }
    }
}
