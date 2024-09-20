package dev.anvilcraft.rg;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import dev.anvilcraft.rg.api.RGAdditional;
import dev.anvilcraft.rg.api.RGEnvironment;
import dev.anvilcraft.rg.api.RGRuleManager;
import dev.anvilcraft.rg.util.TranslationUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Optional;

@Mod(RollingGate.MODID)
public class RollingGate implements RGAdditional {
    public static final String MODID = "rolling_gate";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final RGRuleManager RULE_MANAGER = new RGRuleManager(RollingGate.MODID, RGEnvironment.SERVER);

    public RollingGate(@NotNull IEventBus modEventBus, @NotNull ModContainer modContainer) {
        modEventBus.addListener(this::loadRGRules);
        NeoForge.EVENT_BUS.addListener(this::reInitRules);
        NeoForge.EVENT_BUS.addListener(this::registerCommand);
        modContainer.registerExtensionPoint(RGAdditional.class, this);
    }

    @Override
    public void loadRules(@NotNull RGRuleManager manager) {
        manager.register(RollingGateRules.class);
        TranslationUtil.loadLanguage(RollingGate.class, "rolling_gate", "zh_cn");
        TranslationUtil.loadLanguage(RollingGate.class, "rolling_gate", "en_us");
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
        RollingGate.RULE_MANAGER.reInit(server);
    }

    @SubscribeEvent
    public void registerCommand(@NotNull RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(MODID);
        LiteralArgumentBuilder<CommandSourceStack> root2 = Commands.literal("rg");
        RollingGate.RULE_MANAGER.generateCommand(root);
        RollingGate.RULE_MANAGER.generateCommand(root2);
        event.getDispatcher().register(root);
        event.getDispatcher().register(root2);
    }
}
