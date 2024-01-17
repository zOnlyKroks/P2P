package de.zonlykroks.p2p4all.client;

import com.mojang.logging.LogUtils;
import de.zonlykroks.p2p4all.config.P2PYACLConfig;
import de.zonlykroks.p2p4all.event.MinecraftClientShutdownEvent;
import de.zonlykroks.p2p4all.net.Tunnel;
import de.zonlykroks.p2p4all.util.ConnectionProgress;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceType;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class P2P4AllClient implements ClientModInitializer {

    public static final Map<String, ConnectionProgress> ipToStateMap = new ConcurrentHashMap<>();

    public static String SERVER_CONNECT_ADDRESS;
    public static final Logger LOGGER = LogUtils.getLogger();

    public static Map<String, Tunnel> currentlyRunningTunnels = new ConcurrentHashMap<>();


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

        MinecraftClientShutdownEvent.SHUTDOWN.register(P2P4AllClient::clearAllTunnels);

        Thread printingHook = new Thread(P2P4AllClient::clearAllTunnels);
        Runtime.getRuntime().addShutdownHook(printingHook);
    }

    public static void clearAllTunnels() {
        P2P4AllClient.currentlyRunningTunnels.values().forEach(Tunnel::close);
        P2P4AllClient.currentlyRunningTunnels.clear();
    }
}
