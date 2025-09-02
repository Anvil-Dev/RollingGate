package dev.anvilcraft.rg.mixin;

import dev.anvilcraft.rg.api.event.ClientLoadedEvent;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
abstract class TitleScreenMixin {
    @Inject(method = "init", at = @At("RETURN"))
    private void onResourceLoadFinished(CallbackInfo ci) {
        if (ClientLoadedEvent.loaded) {
            return;
        }
        ClientLoadedEvent.loaded = true;
        NeoForge.EVENT_BUS.post(new ClientLoadedEvent());
    }
}
