package dev.anvilcraft.rg.api.event;

import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class ServerPlayerChatEvent extends PlayerEvent {
    private final ServerPlayer player;
    @Getter
    private final Component component;
    @Getter
    private final PlayerChatMessage playerchatmessage;
    @Getter
    private final FilteredText text;

    public ServerPlayerChatEvent(ServerPlayer player, Component component, PlayerChatMessage playerchatmessage, FilteredText text) {
        super(player);
        this.player = player;
        this.component = component;
        this.playerchatmessage = playerchatmessage;
        this.text = text;
    }

    @Override
    public ServerPlayer getEntity() {
        return this.player;
    }
}
