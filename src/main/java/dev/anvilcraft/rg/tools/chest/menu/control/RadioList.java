package dev.anvilcraft.rg.tools.chest.menu.control;

import java.util.List;

@SuppressWarnings("unused")
public class RadioList extends ButtonList {
    public RadioList(List<Button> buttons, boolean required) {
        super(buttons, required);
        for (Button button : this.buttons) {
            button.addTurnOnFunction(() -> {
                for (Button button1 : this.buttons) {
                    if (button1 == button) {
                        continue;
                    }
                    button1.turnOffWithoutFunction();
                }
            });
        }
    }
}
