package dev.anvilcraft.rg.event;

import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.event.server.ServerLifecycleEvent;

public class ServerLoadedLevelEvent extends ServerLifecycleEvent {
    public ServerLoadedLevelEvent(MinecraftServer server) {
        super(server);
    }
}
