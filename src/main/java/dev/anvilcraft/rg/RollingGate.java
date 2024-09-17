package dev.anvilcraft.rg;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import dev.anvilcraft.rg.api.RGAdditional;
import dev.anvilcraft.rg.api.RGRuleManager;
import dev.anvilcraft.rg.util.ConfigUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Optional;

@Mod(RollingGate.MODID)
public class RollingGate implements RGAdditional {
    public static final String MODID = "rolling_gate";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final RGRuleManager RULE_MANAGER = new RGRuleManager();
    private static final LevelResource RULE_PATH = new LevelResource("rolling_gate.json");

    public RollingGate(@NotNull IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.addListener(this::reInitRules);
        modEventBus.addListener(this::loadRGRules);
    }

    @Override
    public void loadRules(@NotNull RGRuleManager manager) {
        manager.register(RollingGateRules.class);
    }

    @SubscribeEvent
    public void loadRGRules(FMLLoadCompleteEvent event) {
        ModList.get().forEachModContainer((modId, modContainer) -> {
            RollingGate.RULE_MANAGER.setNamespace(modId);
            Optional<RGAdditional> additional = modContainer.getCustomExtension(RGAdditional.class);
            additional.ifPresent(add -> add.loadRules(RollingGate.RULE_MANAGER));
        });
    }

    @SubscribeEvent
    public void reInitRules(@NotNull ServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        Path path = server.getWorldPath(RollingGate.RULE_PATH);
        JsonObject config = ConfigUtil.getOrCreateContent(path);
        RollingGate.RULE_MANAGER.reInit(config);
    }
}
