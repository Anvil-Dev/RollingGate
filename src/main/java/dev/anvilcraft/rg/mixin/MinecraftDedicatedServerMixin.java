package dev.anvilcraft.rg.mixin;

import dev.anvilcraft.rg.api.event.ServerAboutToStopEvent;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
abstract class MinecraftDedicatedServerMixin {
    @Inject(
        method = {"stopServer"},
        at = {@At("HEAD")}
    )
    private void serverClosed(CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        NeoForge.EVENT_BUS.post(new ServerAboutToStopEvent(server));
    }
}
