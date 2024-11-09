package dev.anvilcraft.rg;

import dev.anvilcraft.rg.api.RGValidator;
import dev.anvilcraft.rg.api.Rule;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class RollingGateServerRules {
    @Rule(allowed = {"zh_cn", "en_us"}, categories = RollingGateCategories.BASE)
    public static String language = "zh_cn";

    public static class ViewDistanceValidator extends RGValidator.IntegerValidator {
        @Override
        public @NotNull Map.Entry<Integer, Integer> getRange() {
            return Map.entry(0, 32);
        }
    }

    @Rule(
        allowed = {"0", "12", "16", "32"},
        categories = RollingGateCategories.CREATIVE,
        validator = ViewDistanceValidator.class
    )
    public static int viewDistance = 0;

    @Rule(
        allowed = {"0", "12", "16", "32"},
        categories = RollingGateCategories.CREATIVE,
        validator = ViewDistanceValidator.class
    )
    public static int simulationDistance = 0;
}
