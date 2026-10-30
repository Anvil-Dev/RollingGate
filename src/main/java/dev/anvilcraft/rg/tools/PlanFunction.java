package dev.anvilcraft.rg.tools;

import dev.anvilcraft.rg.RollingGate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

@EventBusSubscriber(modid = RollingGate.MODID)
@SuppressWarnings("unused")
public class PlanFunction {
    public static final Map<Long, Collection<Consumer<MinecraftServer>>> PLAN_FUNCTION = new TreeMap<>();

    public static void addPlan(long time, Consumer<MinecraftServer> consumer) {
        PlanFunction.PLAN_FUNCTION.computeIfAbsent(time, k -> new ArrayList<>()).add(consumer);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (PlanFunction.PLAN_FUNCTION.isEmpty()) return;
        MinecraftServer server = event.getServer();
        ServerLevel level = server.overworld();
        long time = level.getGameTime();
        Iterator<Map.Entry<Long, Collection<Consumer<MinecraftServer>>>> iterator = PlanFunction.PLAN_FUNCTION.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Collection<Consumer<MinecraftServer>>> entry = iterator.next();
            if (entry.getKey() > time) continue;
            iterator.remove();
            if (entry.getKey() == time) {
                entry.getValue().forEach(consumer -> consumer.accept(server));
                break;
            }
        }
    }
}
