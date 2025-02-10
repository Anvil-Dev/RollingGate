package dev.anvilcraft.rg.tools.chest.menu.control;

import dev.anvilcraft.rg.api.server.TranslationUtil;
import dev.anvilcraft.rg.tools.chest.Consumer;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
@SuppressWarnings("unused")
public class Button {
    private boolean init = false;
    private boolean flag;
    private final ItemStack onItem;
    private final ItemStack offItem;
    CompoundTag compoundTag = new CompoundTag();
    public static final String RG_CLEAR = "RGClear";

    private final List<Consumer> turnOnConsumers = new ArrayList<>();

    private final List<Consumer> turnOffConsumers = new ArrayList<>();

    public Button() {
        this(true, Items.BARRIER, Items.STRUCTURE_VOID);
    }

    public Button(boolean defaultState) {
        this(defaultState, Items.BARRIER, Items.STRUCTURE_VOID);
    }

    public Button(boolean defaultState, int itemCount) {
        this(defaultState, Items.BARRIER, Items.STRUCTURE_VOID, itemCount);
    }

    public Button(boolean defaultState, int itemCount, Component onText, Component offText) {
        this(defaultState, Items.BARRIER, Items.STRUCTURE_VOID, itemCount, onText, offText);
    }

    public Button(boolean defaultState, Component onText, Component offText) {
        this(defaultState, Items.BARRIER, Items.STRUCTURE_VOID, 1, onText, offText);
    }

    public Button(boolean defaultState, String key) {
        this(defaultState, Items.BARRIER, Items.STRUCTURE_VOID, 1,
            TranslationUtil.trans(key, TranslationUtil.trans("rolling_gate.chest_menu.button.on")).withStyle(
                Style.EMPTY
                    .withBold(true)
                    .withItalic(false)
                    .withColor(ChatFormatting.GREEN)
            ),
            TranslationUtil.trans(key, TranslationUtil.trans("rolling_gate.chest_menu.button.off")).withStyle(
                Style.EMPTY
                    .withBold(true)
                    .withItalic(false)
                    .withColor(ChatFormatting.RED)
            )
        );
    }

    public Button(boolean defaultState, Item onItem, Item offItem) {
        this(defaultState, onItem, offItem, 1);
    }

    public Button(boolean defaultState, Item onItem, Item offItem, int itemCount) {
        this(defaultState, onItem, offItem, itemCount,
            TranslationUtil.trans("rolling_gate.chest_menu.button.on").withStyle(
                Style.EMPTY
                    .withBold(true)
                    .withItalic(false)
                    .withColor(ChatFormatting.GREEN)
            ),
            TranslationUtil.trans("rolling_gate.chest_menu.button.off").withStyle(
                Style.EMPTY
                    .withBold(true)
                    .withItalic(false)
                    .withColor(ChatFormatting.RED)
            )
        );
    }

    public Button(boolean defaultState, Item onItem, Item offItem, int itemCount, Component onText, Component offText) {
        this.flag = defaultState;
        this.compoundTag.putBoolean(RG_CLEAR, true);

        ItemStack onItemStack = new ItemStack(onItem, itemCount);
        onItemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag));
        onItemStack.set(DataComponents.ITEM_NAME, onText);
        this.onItem = onItemStack;

        ItemStack offItemStack = new ItemStack(offItem, itemCount);
        offItemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag.copy()));
        offItemStack.set(DataComponents.ITEM_NAME, offText);
        this.offItem = offItemStack;
    }

    public Button(boolean defaultState, @NotNull ItemStack onItem, @NotNull ItemStack offItem) {
        this.flag = defaultState;
        this.compoundTag.putBoolean(RG_CLEAR, true);

        ItemStack onItemStack = onItem.copy();
        onItemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag.copy()));
        this.onItem = onItemStack;

        ItemStack offItemStack = offItem.copy();
        offItemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag.copy()));
        this.offItem = offItemStack;
    }

    public void checkButton(Container container, int slot) {
        ItemStack onItemStack = this.onItem.copy();
        ItemStack offItemStack = this.offItem.copy();
        if (!this.init) {
            updateButton(container, slot, onItemStack, offItemStack);
            this.init = true;
        }

        ItemStack item = container.getItem(slot);

        if (item.isEmpty()) {
            this.flag = !flag;
            if (flag) {
                runTurnOnFunction();
            } else {
                runTurnOffFunction();
            }
        }

        updateButton(container, slot, onItemStack, offItemStack);
    }

    public void updateButton(@NotNull Container container, int slot, @NotNull ItemStack onItemStack, ItemStack offItemStack) {
        if (!(
            container.getItem(slot).is(onItemStack.getItem()) ||
                container.getItem(slot).is(offItemStack.getItem()) ||
                container.getItem(slot).isEmpty()
        )) {
            return;
        }
        if (flag) {
            container.setItem(slot, onItemStack);
        } else {
            container.setItem(slot, offItemStack);
        }
    }

    public void addTurnOnFunction(Consumer consumer) {
        this.turnOnConsumers.add(consumer);
    }

    public void addTurnOffFunction(Consumer consumer) {
        this.turnOffConsumers.add(consumer);
    }

    public void turnOnWithoutFunction() {
        this.flag = true;
    }

    public void turnOffWithoutFunction() {
        this.flag = false;
    }

    public void turnOn() {
        this.flag = true;
        runTurnOnFunction();
    }

    public void turnOff() {
        this.flag = false;
        runTurnOffFunction();
    }

    public void runTurnOnFunction() {
        for (Consumer turnOnConsumer : this.turnOnConsumers) {
            turnOnConsumer.accept();
        }
    }

    public void runTurnOffFunction() {
        for (Consumer turnOffConsumer : this.turnOffConsumers) {
            turnOffConsumer.accept();
        }
    }

    public boolean isOn() {
        return flag;
    }
}
