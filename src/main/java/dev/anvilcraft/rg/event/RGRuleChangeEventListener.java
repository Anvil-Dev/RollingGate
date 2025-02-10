package dev.anvilcraft.rg.event;

import dev.anvilcraft.rg.RollingGate;
import dev.anvilcraft.rg.api.RGRule;
import dev.anvilcraft.rg.api.RGValidator;
import dev.anvilcraft.rg.api.event.RGRuleChangeEvent;
import dev.anvilcraft.rg.mixin.DedicatedServerAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = RollingGate.MODID)
public class RGRuleChangeEventListener {
    @SubscribeEvent
    public static void onRuleChange(@NotNull RGRuleChangeEvent.Server<Integer> event) {
        RGRule<Integer> rule = event.getRule();
        if (rule.name().equals("viewDistance")) {
            changeViewDistance(event.getServer(), event.getNewValue());
        } else if (rule.name().equals("simulationDistance")) {
            changeSimulationDistance(event.getServer(), event.getNewValue());
        }
        if (RGRuleChangeEventListener.isCommand(rule)) notifyPlayersCommandsChanged(event.getServer());
    }

    public static void changeViewDistance(@NotNull MinecraftServer server, int value) {
        if (!server.isDedicatedServer()) return;
        int distance = value >= 2 ? value : ((DedicatedServerAccessor) server).getSettings().getProperties().viewDistance;
        server.getPlayerList().setViewDistance(distance);
    }

    public static void changeSimulationDistance(@NotNull MinecraftServer server, int value) {
        if (!server.isDedicatedServer()) return;
        int distance = value >= 2 ? value : ((DedicatedServerAccessor) server).getSettings().getProperties().simulationDistance;
        server.getPlayerList().setSimulationDistance(distance);
    }

    public static boolean isCommand(@NotNull RGRule<?> rule) {
        for (RGValidator<?> validator : rule.validators()) {
            if (validator instanceof RGValidator.CommandRuleValidator) {
                return true;
            }
        }
        return false;
    }

    public static void notifyPlayersCommandsChanged(MinecraftServer server) {
        if (server == null) return;
        server.tell(new TickTask(server.getTickCount(), () ->
        {
            try {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    server.getCommands().sendCommands(player);
                }
            } catch (NullPointerException e) {
                RollingGate.LOGGER.warn("Exception while refreshing commands, please report this to RollingGate", e);
            }
        }));
    }
}
