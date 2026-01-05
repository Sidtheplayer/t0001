package sid.t0001.world.item;


import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import sid.t0001.main.t0001;
import yesman.epicfight.world.item.WeaponItem;

import java.util.List;


public class T001Item extends WeaponItem {
    public T001Item(Item.Properties build, Tier materialIn) {
        super( build);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.translatable("item." + t0001.MODID + ".sanic_swurd.tooltip"));
    }


}