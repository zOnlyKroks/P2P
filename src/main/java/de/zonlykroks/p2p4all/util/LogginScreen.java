package de.zonlykroks.p2p4all.util;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

public class LogginScreen extends Screen {

    public final Map<String, ConnectionProgress> ipToStateMap = new HashMap<>();

    public LogginScreen(Text title) {
        super(title);
    }
}
