package dev.anvilcraft.rg.mixin;

import dev.anvilcraft.rg.api.event.ServerPlayerChatEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow
    public abstract ServerPlayer getPlayer();

    @Inject(
        method = "lambda$handleChat$1",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;)V",
            shift = At.Shift.AFTER
        )
    )
    private void handleChat(Component component, PlayerChatMessage playerchatmessage, FilteredText text, CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new ServerPlayerChatEvent(this.getPlayer(), component, playerchatmessage, text));
    }
}
