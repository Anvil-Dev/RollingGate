package dev.anvilcraft.rg.event;

import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.event.server.ServerLifecycleEvent;

public class ServerAboutToStopEvent extends ServerLifecycleEvent {
    public ServerAboutToStopEvent(MinecraftServer server) {
        super(server);
    }
}
