package dev.anvilcraft.rg;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(RollingGate.MODID)
public class RollingGate {
    public static final String MODID = "rolling_gate";
    private static final Logger LOGGER = LogUtils.getLogger();

    public RollingGate(IEventBus modEventBus, ModContainer modContainer) {
    }
}
