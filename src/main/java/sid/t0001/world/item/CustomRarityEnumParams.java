package sid.t0001.world.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Rarity;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import sid.t0001.main.t0001;

import java.util.function.UnaryOperator;

public class CustomRarityEnumParams {

    public static HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable(t0001.format("item.%s.dragon_god_sword.tooltip")));

    public static final EnumProxy<Rarity> TRANSCENDENT_RARITY = new EnumProxy<>(
            Rarity.class, -1, "t0001:transcendent",(UnaryOperator<Style>) style ->
            style
                    .withColor(ChatFormatting.GOLD)
                    .withBold(true)
                    .withHoverEvent(hoverEvent)
    );


}
