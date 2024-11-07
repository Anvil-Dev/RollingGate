package dev.anvilcraft.rg.api;

import dev.anvilcraft.rg.api.client.ClientRGRuleManager;
import dev.anvilcraft.rg.api.server.ServerRGRuleManager;
import net.neoforged.fml.IExtensionPoint;

public interface RGAdditional extends IExtensionPoint {
    default void loadServerRules(ServerRGRuleManager manager) {
    }

    default void loadClientRules(ClientRGRuleManager manager) {
    }
}
