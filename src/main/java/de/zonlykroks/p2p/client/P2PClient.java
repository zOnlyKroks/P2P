package de.zonlykroks.p2p.client;

import de.zonlykroks.p2p.api.GoleAPIEvents;
import de.zonlykroks.p2p.config.P2PYACLConfig;
import de.zonlykroks.p2p.api.MinecraftClientShutdownEvent;
import de.zonlykroks.p2p.util.ConnectionProgress;
import de.zonlykroks.p2p.util.GoleProcess;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceType;

import java.util.*;

public class P2PClient implements ClientModInitializer {

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
                P2PClient.ipToStateMap.put(ip,newProg);

                if(P2PYACLConfig.get().verboseLogging) {
                    System.out.println("IP :" + ip + "  ,from: " + oldProg + " ,to: " + newProg);
                }
            }
        });

        MinecraftClientShutdownEvent.SHUTDOWN.register(P2PClient::clearAllTunnels);

        Thread printingHook = new Thread(P2PClient::clearAllTunnels);
        Runtime.getRuntime().addShutdownHook(printingHook);
    }

    public static void clearAllTunnels() {
        P2PClient.currentlyRunningTunnels.forEach((ip, goleProcess) -> {
            if(P2PYACLConfig.get().verboseLogging) {
                System.out.println("Terminating connection associated to ip: " + ip);
            }

            goleProcess.associatedCompletableFuture().cancel(true);
            goleProcess.goleProcess().destroy();
        });
        P2PClient.currentlyRunningTunnels.clear();
    }
}
