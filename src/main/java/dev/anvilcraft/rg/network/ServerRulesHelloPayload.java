package dev.anvilcraft.rg.network;

import dev.anvilcraft.rg.RollingGate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ServerRulesHelloPayload() implements CustomPacketPayload {
    public static final Type<ServerRulesHelloPayload> TYPE = new Type<>(RollingGate.id("server_rules_hello"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerRulesHelloPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ServerRulesHelloPayload decode(RegistryFriendlyByteBuf buffer) {
            return new ServerRulesHelloPayload();
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, ServerRulesHelloPayload payload) {
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
