package sid.t0001.world.item;


import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.xame.t0001;

import yesman.epicfight.world.item.WeaponItem;

import java.util.List;


public class T001Item extends WeaponItem {
    public T001Item(Item.Properties build, Tier materialIn) {
        super(materialIn, 3, -2.6F, build);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item." + t0001.MODID + ".sanic_swurd.tooltip"));
    }
}