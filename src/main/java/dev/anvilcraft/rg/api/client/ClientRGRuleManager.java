package dev.anvilcraft.rg.api.client;

import dev.anvilcraft.rg.api.RGEnvironment;
import dev.anvilcraft.rg.api.RGRuleManager;

public class ClientRGRuleManager extends RGRuleManager {
    public ClientRGRuleManager(String namespace) {
        super(namespace, RGEnvironment.CLIENT);
    }
}
