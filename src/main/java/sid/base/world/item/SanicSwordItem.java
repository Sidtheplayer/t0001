package sid.base.world.item;


import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import sid.base.main.t0001;
import yesman.epicfight.world.item.WeaponItem;

import java.util.List;


public class SanicSwordItem extends WeaponItem {
    public SanicSwordItem(Item.Properties build, Tier materialIn) {
        super( build);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.translatable("item." + t0001.MODID + ".sanic_swurd.tooltip"));
    }


}