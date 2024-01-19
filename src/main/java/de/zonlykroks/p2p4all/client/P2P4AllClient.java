package de.zonlykroks.p2p4all.client;

import de.zonlykroks.p2p4all.api.GoleAPIEvents;
import de.zonlykroks.p2p4all.config.P2PYACLConfig;
import de.zonlykroks.p2p4all.api.MinecraftClientShutdownEvent;
import de.zonlykroks.p2p4all.util.ConnectionProgress;
import de.zonlykroks.p2p4all.util.GoleProcess;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceType;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class P2P4AllClient implements ClientModInitializer {

    public static final Map<String, ConnectionProgress> ipToStateMap = new HashMap<>();

    public static String SERVER_CONNECT_ADDRESS;

    public static Map<String, GoleProcess> currentlyRunningTunnels = Collections.synchronizedMap(new HashMap<>());


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

        GoleAPIEvents.IP_STATE_CHANGE.register((ip, oldProg, newProg) -> {
            if(oldProg != newProg) {
                P2P4AllClient.ipToStateMap.put(ip,newProg);

                if(P2PYACLConfig.get().verboseLogging) {
                    System.out.println("IP :" + ip + "  ,from: " + oldProg + " ,to: " + newProg);
                }
            }
        });

        MinecraftClientShutdownEvent.SHUTDOWN.register(P2P4AllClient::clearAllTunnels);

        Thread printingHook = new Thread(P2P4AllClient::clearAllTunnels);
        Runtime.getRuntime().addShutdownHook(printingHook);
    }

    public static void clearAllTunnels() {
        P2P4AllClient.currentlyRunningTunnels.forEach((ip, goleProcess) -> {
            if(P2PYACLConfig.get().verboseLogging) {
                System.out.println("Terminating connection associated to ip: " + ip);
            }

            goleProcess.associatedCompletableFuture().cancel(true);
            goleProcess.goleProcess().destroy();
        });
        P2P4AllClient.currentlyRunningTunnels.clear();
    }
}
