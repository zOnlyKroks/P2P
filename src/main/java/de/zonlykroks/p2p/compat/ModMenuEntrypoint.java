package de.zonlykroks.p2p.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import de.zonlykroks.p2p.config.P2PYACLConfig;

public class ModMenuEntrypoint implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> P2PYACLConfig.getInstance().generateScreen(parent);
    }

}
