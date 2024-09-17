package dev.anvilcraft.rg;

import dev.anvilcraft.rg.api.Rule;

public class RollingGateRules {
    @Rule(allowed = {"true", "false"}, categories = {RollingGateCategories.DISABLED})
    public static final boolean antiCheatDisabled = false;
}
