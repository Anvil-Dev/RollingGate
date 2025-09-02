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
        if (event.getNewValue()) {
            GLFW.glfwSetWindowAttrib(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        } else {
            GLFW.glfwSetWindowAttrib(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
        }
    }
}
