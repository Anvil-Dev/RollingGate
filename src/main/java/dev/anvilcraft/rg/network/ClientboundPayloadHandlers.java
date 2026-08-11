package dev.anvilcraft.rg.network;

import dev.anvilcraft.rg.RollingGate;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class ClientboundPayloadHandlers {
    private ClientboundPayloadHandlers() {
    }

    public static void handleServerRulesSync(@NotNull ServerRulesSyncPayload payload, @NotNull IPayloadContext context) {
        try {
            Class<?> clientClass = Class.forName("dev.anvilcraft.rg.client.RollingGateClient");
            Object manager = clientClass.getField("CLIENT_SERVER_RULE_MANAGER").get(null);
            manager.getClass().getMethod("applySerializedRules", Map.class).invoke(manager, payload.rules());
        } catch (ReflectiveOperationException e) {
            RollingGate.LOGGER.error("Failed to apply synced server rules", e);
        }
    }
}
