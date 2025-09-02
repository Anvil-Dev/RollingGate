package dev.anvilcraft.rg.client;

import dev.anvilcraft.rg.RollingGate;
import dev.anvilcraft.rg.api.client.ClientRGRuleManager;
import dev.anvilcraft.rg.api.event.ClientLoadedEvent;
import dev.anvilcraft.rg.api.event.RGValidatorNotPassedEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Mod(value = RollingGate.MODID, dist = Dist.CLIENT)
public class RollingGateClient {
    public static final ClientRGRuleManager CLIENT_RULE_MANAGER = new ClientRGRuleManager(RollingGate.MODID);

    public RollingGateClient(@NotNull IEventBus eventBus, @NotNull ModContainer modContainer) throws ClassNotFoundException {
        RollingGateClient.CLIENT_RULE_MANAGER.compileContent();
        Map<String, ModConfigSpec> configSpecs = RollingGateClient.CLIENT_RULE_MANAGER.getSpecMap();
        for (Map.Entry<String, ModConfigSpec> entry : configSpecs.entrySet()) {
            ModConfigSpec value = entry.getValue();
            modContainer.registerConfig(ModConfig.Type.CLIENT, value, "%s-client.toml".formatted(entry.getKey()));
        }
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        eventBus.register(this);
        NeoForge.EVENT_BUS.addListener(RollingGateClient::onLoaded);
    }

    @SubscribeEvent
    public void onLoad(final @NotNull ModConfigEvent event) {
        RollingGateClient.CLIENT_RULE_MANAGER.reInit();
    }

    public static void onLoaded(final @NotNull ClientLoadedEvent event) {
        RollingGateClient.CLIENT_RULE_MANAGER.loadSuccess();
    }

    @SubscribeEvent
    public <T> void onLoad(final @NotNull RGValidatorNotPassedEvent<T> event) {
        RollingGateClient.CLIENT_RULE_MANAGER.reInit();
        @SuppressWarnings("unchecked")
        ModConfigSpec.ConfigValue<T> value = (ModConfigSpec.ConfigValue<T>) RollingGateClient.CLIENT_RULE_MANAGER.getConfigValueMap()
            .get(event.getRule());
        value.set(event.getOldValue());
    }
}
