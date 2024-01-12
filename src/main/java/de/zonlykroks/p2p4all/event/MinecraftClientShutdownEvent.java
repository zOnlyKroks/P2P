package de.zonlykroks.p2p4all.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface MinecraftClientShutdownEvent {

    Event<MinecraftClientShutdownEvent> SHUTDOWN = EventFactory.createArrayBacked(MinecraftClientShutdownEvent.class,
            (listeners) -> () -> {
                for (MinecraftClientShutdownEvent listener : listeners) {
                    listener.shutdown();
                }
            });

    void shutdown();

}
