package dev.anvilcraft.rg;

import com.mojang.logging.LogUtils;
import dev.anvilcraft.rg.api.RGRuleManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(RollingGate.MODID)
public class RollingGate {
    public static final String MODID = "rolling_gate";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RollingGate(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.addListener((ServerStartingEvent event) -> RGRuleManager.reInitSaveRules(event.getServer()));
    }

}
