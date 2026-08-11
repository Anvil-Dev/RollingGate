package dev.anvilcraft.rg;

import com.mojang.logging.LogUtils;
import dev.anvilcraft.rg.api.server.ServerRGRuleManager;
import dev.anvilcraft.rg.network.RollingGateNetwork;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Mod(RollingGate.MODID)
public class RollingGate {
    public static final String MODID = "rolling_gate";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ServerRGRuleManager SERVER_RULE_MANAGER = new ServerRGRuleManager(RollingGate.MODID);

    public RollingGate(@NotNull IEventBus modEventBus, @SuppressWarnings("unused") @NotNull ModContainer modContainer) throws ClassNotFoundException {
        modEventBus.addListener(RollingGateNetwork::registerPayloadHandlers);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::registerCommand);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, RollingGateNetwork::onServerRuleChange);
        NeoForge.EVENT_BUS.addListener(RollingGateNetwork::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(RollingGateNetwork::onServerStopped);
        SERVER_RULE_MANAGER.compileContent();
        if (FMLEnvironment.dist.isClient()) {
            Class<?> clientClass = RollingGate.class.getClassLoader().loadClass("dev.anvilcraft.rg.client.RollingGateClient");
            try {
                Method setup = clientClass.getMethod("onClientSetup", ModContainer.class);
                setup.invoke(null, modContainer);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                RollingGate.LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @SubscribeEvent
    public void onServerStarting(@NotNull ServerStartingEvent event) {
        RollingGate.SERVER_RULE_MANAGER.reInit(event.getServer());
    }

    @SubscribeEvent
    public void registerCommand(@NotNull RegisterCommandsEvent event) {
        RollingGate.SERVER_RULE_MANAGER.generateCommand(event.getDispatcher(), MODID, "rg");
    }

    public static @NotNull ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
