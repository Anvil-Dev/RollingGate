package dev.anvilcraft.rg.tools.chest.menu.control;

import lombok.Getter;

import java.util.List;

@Getter
public abstract class ButtonList {
    protected final List<Button> buttons;

    public ButtonList(List<Button> buttons, boolean required) {
        this.buttons = buttons;
        if (required) {
            buttons.getFirst().turnOnWithoutFunction();
            for (Button button : this.buttons) {
                button.addTurnOffFunction((() -> {
                    if (this.isAllOff()) {
                        button.turnOnWithoutFunction();
                    }
                }));
            }
        }
    }

    public boolean isAllOff() {
        for (Button button : this.buttons) {
            if (button.isOn()) {
                return false;
            }
        }
        return true;
    }

    public boolean isAllOn() {
        for (Button button : this.buttons) {
            if (!button.isOn()) {
                return false;
            }
        }
        return true;
    }
}
