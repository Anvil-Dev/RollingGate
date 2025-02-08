package dev.anvilcraft.rg.mixin;

import dev.anvilcraft.rg.event.ServerLoadedLevelEvent;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(
        method = {"loadLevel"},
        at = {@At("RETURN")}
    )
    private void serverLoadedWorlds(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new ServerLoadedLevelEvent((MinecraftServer) (Object) this));
    }
}
