package de.zonlykroks.p2p4all.client;

import de.zonlykroks.p2p4all.config.P2PConfig;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ClientModInitializer;

public class P2P4AllClient implements ClientModInitializer {
    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        MidnightConfig.init("p2p4all", P2PConfig.class);
    }
}
