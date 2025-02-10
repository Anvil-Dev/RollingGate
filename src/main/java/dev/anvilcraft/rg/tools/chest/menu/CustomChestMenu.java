package dev.anvilcraft.rg.tools.chest.menu;

import dev.anvilcraft.rg.tools.chest.menu.control.Button;
import dev.anvilcraft.rg.tools.chest.menu.control.ButtonList;
import net.minecraft.world.Container;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public abstract class CustomChestMenu implements Container {
    public final List<Map.Entry<Integer, Button>> buttons = new ArrayList<>();
    public final List<ButtonList> buttonLists = new ArrayList<>();

    public void tick() {
        this.checkButton();
    }

    public void addButton(int slot, Button button) {
        if (getContainerSize() < (slot + 1)) {
            return;
        }
        buttons.add(Map.entry(slot, button));
    }

    public void addButtonList(ButtonList buttonList) {
        this.buttonLists.add(buttonList);
    }

    private void checkButton() {
        for (Map.Entry<Integer, Button> button : buttons) {
            button.getValue().checkButton(this, button.getKey());
        }
    }
}

