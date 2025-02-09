package dev.anvilcraft.rg.mixin;

import dev.anvilcraft.rg.event.ServerAboutToStopEvent;
import dev.anvilcraft.rg.event.ServerLoadedLevelEvent;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
abstract class MinecraftServerMixin {
    @Inject(
        method = {"stopServer"},
        at = {@At("HEAD")}
    )
    private void serverClosed(CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        if (!(server instanceof IntegratedServer)) return;
        NeoForge.EVENT_BUS.post(new ServerAboutToStopEvent(server));
    }

    @Inject(
        method = {"loadLevel"},
        at = {@At("RETURN")}
    )
    private void serverLoadedWorlds(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new ServerLoadedLevelEvent((MinecraftServer) (Object) this));
    }
}
