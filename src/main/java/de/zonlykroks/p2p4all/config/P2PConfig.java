package de.zonlykroks.p2p4all.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class P2PConfig extends MidnightConfig {

    @Entry public static String TARGET_IP = "";
    @Entry public static boolean areYouTheServer = true;

    @Entry public static String password = "";

    @Entry public static String goleFilePath = "";

}
