package dev.anvilcraft.rg.client;

import dev.anvilcraft.rg.RollingGate;
import dev.anvilcraft.rg.api.client.ClientRGRuleManager;
import dev.anvilcraft.rg.api.event.RGValidatorNotPassedEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@EventBusSubscriber(value = Dist.CLIENT, modid = RollingGate.MODID)
public class RollingGateClient {
    public static final ClientRGRuleManager CLIENT_RULE_MANAGER = new ClientRGRuleManager(RollingGate.MODID);

    @SuppressWarnings("unused")
    public static void onClientSetup(@NotNull ModContainer modContainer) throws ClassNotFoundException {
        RollingGateClient.CLIENT_RULE_MANAGER.compileContent();
        Map<String, ModConfigSpec> configSpecs = RollingGateClient.CLIENT_RULE_MANAGER.getSpecMap();
        for (Map.Entry<String, ModConfigSpec> entry : configSpecs.entrySet()) {
            ModConfigSpec value = entry.getValue();
            modContainer.registerConfig(ModConfig.Type.CLIENT, value, "%s-client.toml".formatted(entry.getKey()));
        }
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void onLoad(final @NotNull ModConfigEvent event) {
        RollingGateClient.CLIENT_RULE_MANAGER.reInit();
    }

    @SubscribeEvent
    public static void onLoaded(final @NotNull FMLLoadCompleteEvent event) {
        RollingGateClient.CLIENT_RULE_MANAGER.loadSuccess();
    }

    @SubscribeEvent
    public static <T> void onLoad(final @NotNull RGValidatorNotPassedEvent<T> event) {
        RollingGateClient.CLIENT_RULE_MANAGER.reInit();
        @SuppressWarnings("unchecked")
        ModConfigSpec.ConfigValue<T> value = (ModConfigSpec.ConfigValue<T>) RollingGateClient.CLIENT_RULE_MANAGER.getConfigValueMap()
            .get(event.getRule());
        value.set(event.getOldValue());
    }
}
