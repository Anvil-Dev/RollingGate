package dev.anvilcraft.rg.client;

import dev.anvilcraft.rg.RollingGate;
import dev.anvilcraft.rg.api.client.ClientRGRuleManager;
import dev.anvilcraft.rg.api.event.ClientLoadedEvent;
import dev.anvilcraft.rg.api.event.RGValidatorNotPassedEvent;
import dev.anvilcraft.rg.api.server.ServerRGRuleManager;
import dev.anvilcraft.rg.network.ServerRulesHelloPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Mod(value = RollingGate.MODID, dist = Dist.CLIENT)
public class RollingGateClient {
    public static final ClientRGRuleManager CLIENT_RULE_MANAGER = new ClientRGRuleManager(RollingGate.MODID);
    public static final ServerRGRuleManager CLIENT_SERVER_RULE_MANAGER = new ServerRGRuleManager(RollingGate.MODID);

    public RollingGateClient(@NotNull IEventBus eventBus, @NotNull ModContainer modContainer) throws ClassNotFoundException {
        RollingGateClient.CLIENT_SERVER_RULE_MANAGER.compileContent();
        RollingGateClient.CLIENT_RULE_MANAGER.compileContent();
        Map<String, ModConfigSpec> configSpecs = RollingGateClient.CLIENT_RULE_MANAGER.getSpecMap();
        for (Map.Entry<String, ModConfigSpec> entry : configSpecs.entrySet()) {
            ModConfigSpec value = entry.getValue();
            modContainer.registerConfig(ModConfig.Type.CLIENT, value, "%s-client.toml".formatted(entry.getKey()));
        }
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        eventBus.register(this);
        NeoForge.EVENT_BUS.addListener(RollingGateClient::onLoaded);
        NeoForge.EVENT_BUS.addListener(RollingGateClient::onLoggingIn);
        NeoForge.EVENT_BUS.addListener(RollingGateClient::onLoggingOut);
        NeoForge.EVENT_BUS.addListener(RollingGateClient::onIntegratedServerStopped);
    }

    @SubscribeEvent
    public void onLoad(final @NotNull ModConfigEvent event) {
        RollingGateClient.CLIENT_RULE_MANAGER.reInit();
    }

    public static void onLoaded(final @NotNull ClientLoadedEvent event) {
        RollingGateClient.CLIENT_RULE_MANAGER.loadSuccess();
    }

    public static void onLoggingIn(final @NotNull ClientPlayerNetworkEvent.LoggingIn event) {
        PacketDistributor.sendToServer(new ServerRulesHelloPayload());
    }

    public static void onLoggingOut(final @NotNull ClientPlayerNetworkEvent.LoggingOut event) {
        var connection = event.getConnection();
        var server = ServerLifecycleHooks.getCurrentServer();
        boolean integrated = (connection != null && connection.isMemoryConnection())
            || (server != null && !server.isDedicatedServer());
        // Server rules are static fields shared with the integrated server. It
        // still needs them when saving during its later shutdown event.
        if (integrated) return;
        RollingGateClient.CLIENT_SERVER_RULE_MANAGER.resetRulesToDefault();
    }

    public static void onIntegratedServerStopped(final @NotNull ServerStoppedEvent event) {
        if (event.getServer().isDedicatedServer()) return;
        net.minecraft.client.Minecraft.getInstance().execute(() -> {
            // Do not overwrite rules already applied by a newly connected server.
            if (net.minecraft.client.Minecraft.getInstance().getConnection() == null) {
                CLIENT_SERVER_RULE_MANAGER.resetRulesToDefault();
            }
        });
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
