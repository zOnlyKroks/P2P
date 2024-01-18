package de.zonlykroks.p2p4all.api;

import de.zonlykroks.p2p4all.util.ConnectionProgress;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;

public interface GoleAPIEvents {

    Event<StartDownload> START_DOWNLOAD = EventFactory.createArrayBacked(StartDownload.class, callbacks  -> client -> {
        for (StartDownload event : callbacks) {
            event.startDownload(client);
        }
    });

    Event<FinishDownload> FINISH_DOWNLOAD = EventFactory.createArrayBacked(FinishDownload.class, callbacks  -> client -> {
        for (FinishDownload event : callbacks) {
            event.finishDownload(client);
        }
    });

    Event<IpStateChange> IP_STATE_CHANGE = EventFactory.createArrayBacked(IpStateChange.class, callbacks  -> (ip, oldProg, newProg) -> {
        for (IpStateChange event : callbacks) {
            event.ipStateChange(ip, oldProg, newProg);
        }
    });

    @FunctionalInterface
    interface StartDownload {
        void startDownload(MinecraftClient client);
    }

    @FunctionalInterface
    interface FinishDownload {
        void finishDownload(MinecraftClient client);
    }

    @FunctionalInterface
    interface IpStateChange {
        void ipStateChange(String ip, ConnectionProgress oldProg, ConnectionProgress newProg);
    }

}
