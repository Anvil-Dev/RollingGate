package dev.anvilcraft.rg.event;

import dev.anvilcraft.rg.RollingGate;
import dev.anvilcraft.rg.api.event.RGRuleChangeEvent;
import dev.anvilcraft.rg.mixin.server.DedicatedServerAccessor;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = RollingGate.MODID)
public class RGRuleChangeEventListener {
    @SubscribeEvent
    public static void onRuleChange(@NotNull RGRuleChangeEvent.Server<Integer> event) {
        if (event.getRule().name().equals("viewDistance")) changeViewDistance(event.getServer(), event.getNewValue());
    }

    public static void changeViewDistance(@NotNull MinecraftServer server, int value) {
        if (!server.isDedicatedServer()) return;
        int distance = value >= 2 ? value : ((DedicatedServerAccessor) server).getSettings().getProperties().viewDistance;
        server.getPlayerList().setViewDistance(distance);
    }
}
