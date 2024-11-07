package dev.anvilcraft.rg.client;

import dev.anvilcraft.rg.RollingGate;
import dev.anvilcraft.rg.api.RGAdditional;
import dev.anvilcraft.rg.api.client.ClientRGRuleManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

import java.util.Optional;

@EventBusSubscriber(value = Dist.CLIENT, modid = RollingGate.MODID, bus = EventBusSubscriber.Bus.MOD)
public class RollingGateClient {
    private static final ClientRGRuleManager CLIENT_RULE_MANAGER = new ClientRGRuleManager(RollingGate.MODID);

    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        ModList.get().forEachModContainer((modId, modContainer) -> {
            RollingGateClient.CLIENT_RULE_MANAGER.setNamespace(modId);
            Optional<RGAdditional> additional = modContainer.getCustomExtension(RGAdditional.class);
            additional.ifPresent(add -> add.loadClientRules(RollingGateClient.CLIENT_RULE_MANAGER));
        });
        RollingGateClient.CLIENT_RULE_MANAGER.reInit();
    }
}
