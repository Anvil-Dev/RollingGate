package dev.anvilcraft.rg.mixin;


import dev.anvilcraft.rg.tools.chest.menu.control.Button;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    @Shadow @Final
    PatchedDataComponentMap components;

    @Inject(method = "getComponents", at = @At("HEAD"), cancellable = true)
    private void getComponents(CallbackInfoReturnable<DataComponentMap> cir) {
        CustomData customData = this.components.get(DataComponents.CUSTOM_DATA);
        if (customData == null || customData.copyTag().get(Button.RG_CLEAR) == null) {
            return;
        }
        cir.setReturnValue(this.components);
    }
}
