package dev.anvilcraft.rg.event.listener;

import dev.anvilcraft.rg.RollingGate;
import dev.anvilcraft.rg.api.RGRule;
import dev.anvilcraft.rg.api.RGValidator;
import dev.anvilcraft.rg.api.event.RGRuleChangeEvent;
import dev.anvilcraft.rg.tools.ModCommands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = RollingGate.MODID)
public class RGRuleChangeEventListener {
    @SubscribeEvent
    public static void onRuleChange(RGRuleChangeEvent.Server<?> event) {
        RGRule<?> rule = event.getRule();
        if (RGRuleChangeEventListener.isCommand(rule)) {
            ModCommands.notifyPlayersCommandsChanged(event.getServer());
        }
    }

    public static boolean isCommand(RGRule<?> rule) {
        for (RGValidator<?> validator : rule.validators()) {
            if (validator instanceof RGValidator.CommandRuleValidator) {
                return true;
            }
        }
        return false;
    }
}
