package dev.anvilcraft.rg.tools.chest.menu.control;

import dev.anvilcraft.rg.api.server.TranslationUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

@SuppressWarnings("unused")
public class AutoResetButton extends Button {
    public AutoResetButton(String key) {
        super(false,
            TranslationUtil.trans(key).withStyle(
                Style.EMPTY
                    .withBold(true)
                    .withItalic(false)
                    .withColor(ChatFormatting.WHITE)
            ),
            TranslationUtil.trans(key).withStyle(
                Style.EMPTY
                    .withBold(true)
                    .withItalic(false)
                    .withColor(ChatFormatting.WHITE)
            )
        );
        this.addTurnOnFunction(this::turnOffWithoutFunction);
    }

    public AutoResetButton(String key, Item item) {
        super(false,
            item,
            item,
            1,
            TranslationUtil.trans(key).withStyle(
                Style.EMPTY
                    .withBold(true)
                    .withItalic(false)
                    .withColor(ChatFormatting.WHITE)
            ),
            TranslationUtil.trans(key).withStyle(
                Style.EMPTY
                    .withBold(true)
                    .withItalic(false)
                    .withColor(ChatFormatting.WHITE)
            )
        );
        this.addTurnOnFunction(this::turnOffWithoutFunction);
    }

    public static final AutoResetButton NONE = new AutoResetButton("rolling_gate.chest_menu.button.none", Items.RED_STAINED_GLASS_PANE);
}
