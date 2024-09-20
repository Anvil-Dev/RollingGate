package dev.anvilcraft.rg;

import dev.anvilcraft.rg.api.Rule;

public class RollingGateRules {
    @Rule(allowed = {"zh_cn", "en_us"}, categories = {RollingGateCategories.DISABLED})
    public static String language = "zh_cn";
}
