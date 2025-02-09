package dev.anvilcraft.rg.mixin;

import dev.anvilcraft.rg.event.ServerAboutToStopEvent;
import net.minecraft.client.server.IntegratedServer;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IntegratedServer.class)
abstract class IntegratedServerMixin {
    @Inject(method = "halt", at = @At("HEAD"))
    private void halt(boolean waitForServer, CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new ServerAboutToStopEvent((IntegratedServer) (Object) this));
    }
}
