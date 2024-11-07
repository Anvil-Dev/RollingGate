package dev.anvilcraft.rg;

import com.mojang.logging.LogUtils;
import dev.anvilcraft.rg.api.client.ClientRGRuleManager;
import dev.anvilcraft.rg.api.RGAdditional;
import dev.anvilcraft.rg.api.server.ServerRGRuleManager;
import dev.anvilcraft.rg.api.server.TranslationUtil;
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
    private static final ServerRGRuleManager SERVER_RULE_MANAGER = new ServerRGRuleManager(RollingGate.MODID);
    private static final ClientRGRuleManager CLIENT_RULE_MANAGER = new ClientRGRuleManager(RollingGate.MODID);

    public RollingGate(@NotNull IEventBus modEventBus, @NotNull ModContainer modContainer) {
        modEventBus.addListener(this::onLoadComplete);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::registerCommand);
        modContainer.registerExtensionPoint(RGAdditional.class, this);
    }

    @Override
    public void loadServerRules(@NotNull ServerRGRuleManager manager) {
        manager.register(RollingGateServerRules.class);
        TranslationUtil.loadLanguage(RollingGate.class, MODID, "zh_cn");
        TranslationUtil.loadLanguage(RollingGate.class, MODID, "en_us");
    }

    @Override
    public void loadClientRules(@NotNull ClientRGRuleManager manager) {
        manager.register(RollingGateClientRules.class);
    }

    @SubscribeEvent
    public void onLoadComplete(FMLLoadCompleteEvent event) {
        ModList.get().forEachModContainer((modId, modContainer) -> {
            RollingGate.SERVER_RULE_MANAGER.setNamespace(modId);
            Optional<RGAdditional> additional = modContainer.getCustomExtension(RGAdditional.class);
            additional.ifPresent(add -> add.loadServerRules(RollingGate.SERVER_RULE_MANAGER));
            additional.ifPresent(add -> add.loadClientRules(RollingGate.CLIENT_RULE_MANAGER));
        });
        RollingGate.CLIENT_RULE_MANAGER.reInit();
    }

    @SubscribeEvent
    public void onServerStarting(@NotNull ServerStartingEvent event) {
        RollingGate.SERVER_RULE_MANAGER.reInit(event.getServer());
    }

    @SubscribeEvent
    public void registerCommand(@NotNull RegisterCommandsEvent event) {
        RollingGate.SERVER_RULE_MANAGER.generateCommand(event.getDispatcher(), MODID, "rg");
    }
}
