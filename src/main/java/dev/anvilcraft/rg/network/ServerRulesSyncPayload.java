package dev.anvilcraft.rg.network;

import dev.anvilcraft.rg.RollingGate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public record ServerRulesSyncPayload(Map<String, String> rules) implements CustomPacketPayload {
    public static final Type<ServerRulesSyncPayload> TYPE = new Type<>(RollingGate.id("server_rules_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerRulesSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ServerRulesSyncPayload decode(@NotNull RegistryFriendlyByteBuf buffer) {
            int size = buffer.readVarInt();
            Map<String, String> rules = new LinkedHashMap<>();
            for (int i = 0; i < size; i++) {
                rules.put(buffer.readUtf(), buffer.readUtf());
            }
            return new ServerRulesSyncPayload(rules);
        }

        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buffer, @NotNull ServerRulesSyncPayload payload) {
            buffer.writeVarInt(payload.rules.size());
            for (Map.Entry<String, String> entry : payload.rules.entrySet()) {
                buffer.writeUtf(entry.getKey());
                buffer.writeUtf(entry.getValue());
            }
        }
    };

    public static ServerRulesSyncPayload single(String rule, String value) {
        return new ServerRulesSyncPayload(Map.of(rule, value));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
