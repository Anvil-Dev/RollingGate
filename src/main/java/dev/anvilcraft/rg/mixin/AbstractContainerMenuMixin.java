package dev.anvilcraft.rg.mixin;

import dev.anvilcraft.rg.tools.chest.menu.control.Button;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {
    @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
    private void doClick(int slotIndex, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (slotIndex < 0) return;
        Slot slot = ((AbstractContainerMenu) (Object) this).getSlot(slotIndex);
        ItemStack itemStack = slot.getItem();
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null || customData.copyTag().get(Button.RG_CLEAR) == null) {
            return;
        }
        if (customData.copyTag().getBoolean(Button.RG_CLEAR)) {
            itemStack.setCount(0);
            ci.cancel();
        }
    }
}
