package dev.anvilcraft.rg;

import dev.anvilcraft.rg.api.Rule;

public class RollingGateServerRules {
    @Rule(allowed = {"zh_cn", "en_us"}, categories = {RollingGateCategories.BASE})
    public static String language = "zh_cn";
}
