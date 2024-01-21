package de.zonlykroks.p2p.api;

import de.zonlykroks.p2p.util.ConnectionProgress;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        void startDownload(@NotNull MinecraftClient client);
    }

    @FunctionalInterface
    interface FinishDownload {
        void finishDownload(@NotNull MinecraftClient client);
    }

    @FunctionalInterface
    interface IpStateChange {
        void ipStateChange(@NotNull String ip, @Nullable ConnectionProgress oldProg, @NotNull ConnectionProgress newProg);
    }

}
