package dev.anvilcraft.rg.event.listener.client.listener;

import dev.anvilcraft.rg.RollingGate;
import dev.anvilcraft.rg.api.event.RGRuleChangeEvent;
import dev.anvilcraft.rg.client.RollingGateClient;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = RollingGate.MODID, value = Dist.CLIENT)
public class RGRuleChangeEventListener {
    @SubscribeEvent
    public static void onRuleChange(@NotNull RGRuleChangeEvent.Client<?> event) {
        RollingGateClient.CLIENT_RULE_MANAGER.onRuleChange(event.getRule(), event.getOldValue(), event.getNewValue());
    }


    @SubscribeEvent
    public static void onWindowResizableChange(@NotNull RGRuleChangeEvent.Client<Boolean> event) {
        if (!"windowResizable".equals(event.getRule().name())) return;
        Minecraft minecraft = Minecraft.getInstance();
        boolean resizable = event.getNewValue();
        // Config loading can post this event on a mod-loading worker. GLFW window
        // operations must run on Minecraft's client thread, particularly on macOS.
        minecraft.execute(() -> GLFW.glfwSetWindowAttrib(minecraft.getWindow().getWindow(),
            GLFW.GLFW_RESIZABLE, resizable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE));
    }
}
