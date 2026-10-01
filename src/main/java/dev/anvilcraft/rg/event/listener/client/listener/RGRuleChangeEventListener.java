package dev.anvilcraft.rg.event.listener.client.listener;

import dev.anvilcraft.rg.RollingGate;
import dev.anvilcraft.rg.api.event.RGRuleChangeEvent;
import dev.anvilcraft.rg.client.RollingGateClient;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.sdl.SDLVideo;

@EventBusSubscriber(modid = RollingGate.MODID, value = Dist.CLIENT)
public class RGRuleChangeEventListener {
    @SubscribeEvent
    public static void onRuleChange(@NotNull RGRuleChangeEvent.Client<?> event) {
        RollingGateClient.CLIENT_RULE_MANAGER.onRuleChange(event.getRule(), event.getOldValue(), event.getNewValue());
    }


    @SubscribeEvent
    public static void onWindowResizableChange(@NotNull RGRuleChangeEvent.Client<Boolean> event) {
        if (!"windowResizable".equals(event.getRule().name())) return;
        if (event.getNewValue()) {
            SDLVideo.SDL_SetWindowResizable(Minecraft.getInstance().getWindow().handle(), true);
        } else {
            SDLVideo.SDL_SetWindowResizable(Minecraft.getInstance().getWindow().handle(), false);
        }
    }
}
