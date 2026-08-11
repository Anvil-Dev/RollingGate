package dev.anvilcraft.rg.network;

import dev.anvilcraft.rg.RollingGate;
import dev.anvilcraft.rg.api.RGRule;
import dev.anvilcraft.rg.api.event.RGRuleChangeEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class RollingGateNetwork {
    private static final String NETWORK_VERSION = "1";
    private static final Set<UUID> HELLO_PLAYERS = new HashSet<>();

    private RollingGateNetwork() {
    }

    public static void registerPayloadHandlers(@NotNull RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(NETWORK_VERSION).optional();
        registrar.playToServer(ServerRulesHelloPayload.TYPE, ServerRulesHelloPayload.STREAM_CODEC, RollingGateNetwork::handleHello);
        registrar.playToClient(ServerRulesSyncPayload.TYPE, ServerRulesSyncPayload.STREAM_CODEC, ClientboundPayloadHandlers::handleServerRulesSync);
    }

    private static void handleHello(@NotNull ServerRulesHelloPayload payload, @NotNull IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        HELLO_PLAYERS.add(player.getUUID());
        PacketDistributor.sendToPlayer(player, new ServerRulesSyncPayload(RollingGate.SERVER_RULE_MANAGER.getSerializedCurrentRules()));
    }

    public static void onServerRuleChange(@NotNull RGRuleChangeEvent.Server<?> event) {
        if (event.isCanceled()) return;
        syncRuleToHelloPlayers(event.getRule(), event.getNewValue());
    }

    public static void onPlayerLoggedOut(@NotNull PlayerEvent.PlayerLoggedOutEvent event) {
        HELLO_PLAYERS.remove(event.getEntity().getUUID());
    }

    public static void onServerStopped(@NotNull ServerStoppedEvent event) {
        HELLO_PLAYERS.clear();
    }

    public static void syncRulesToHelloPlayers() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        ServerRulesSyncPayload payload = new ServerRulesSyncPayload(RollingGate.SERVER_RULE_MANAGER.getSerializedCurrentRules());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (HELLO_PLAYERS.contains(player.getUUID())) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }

    public static void syncRuleToHelloPlayers(@NotNull RGRule<?> rule) {
        syncRuleToHelloPlayers(rule, rule.getValue());
    }

    public static <T> void syncRuleToHelloPlayers(@NotNull RGRule<T> rule, Object value) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        ServerRulesSyncPayload payload = ServerRulesSyncPayload.single(rule.serialize(), rule.codec().encode(rule.type().cast(value)));
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (HELLO_PLAYERS.contains(player.getUUID())) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }
}
