package de.zonlykroks.p2p4all.client;

import de.zonlykroks.p2p4all.client.net.Tunnel;
import net.fabricmc.api.ClientModInitializer;

public class P2P4AllClient implements ClientModInitializer {

    public static String SERVER_CONNECT_ADDRESS = null;
    public static Tunnel TUNNEL;

    @Override
    public void onInitializeClient() {}
}
