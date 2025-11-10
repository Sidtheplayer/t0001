package sid.t0001.world.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xame.t0001;
import yesman.epicfight.world.item.WeaponItem;

import java.util.List;

public class DragonGodSwordBrokenItem extends WeaponItem {
    public DragonGodSwordBrokenItem(Properties build, Tier materialIn) {
        super(materialIn, -1, -1F, build);
    }


    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public boolean isRepairable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack Stack, @Nullable Level pLevel, List<Component> tooltipComponents, @NotNull TooltipFlag pIsAdvanced) {
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.translatable("item." + t0001.MODID + ".dragon_god_sword_broken.tooltip"));
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return Component.translatable(this.getDescriptionId(stack))
                .withStyle(style -> style
                        .withBold(false)
                        .withItalic(true)
                        .withColor(ChatFormatting.GRAY)
                );
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        return super.onDroppedByPlayer(item, player);
    }



}
