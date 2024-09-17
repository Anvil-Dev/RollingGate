package dev.anvilcraft.rg.api;

import net.neoforged.fml.IExtensionPoint;

public interface RGAdditional extends IExtensionPoint {
    void loadRules(RGRuleManager manager);
}
