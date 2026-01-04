package sid.t0001.world.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xame.t0001;
import yesman.epicfight.world.item.WeaponItem;

import java.util.List;
import java.util.Random;

import static sid.t0001.gameasset.ReusableEvents.handleBreak;

public class DragonGodSwordItem extends WeaponItem {
    public DragonGodSwordItem(Item.Properties build, Tier materialIn) {
        super(materialIn, 4, -1.96F, build);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack pToRepair, @NotNull ItemStack pRepair) {
        return pRepair.getItem() == Items.NETHERITE_INGOT; //
    }

    private static final Random RANDOM = new Random();

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return RANDOM.nextBoolean();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack Stack, @Nullable Level pLevel, List<Component> tooltipComponents, @NotNull TooltipFlag pIsAdvanced) {
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.translatable("item." + t0001.MODID + ".dragon_god_sword.tooltip"));
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return Component.translatable(this.getDescriptionId(stack))
                .withStyle(style -> style
                        .withBold(true)
                        .withItalic(true)
                        .withColor(ChatFormatting.GOLD)
                );
    }



    @Override
    public <T extends LivingEntity> int damageItem(
            ItemStack stack,
            int amount,
            T entity,
            java.util.function.Consumer<T> onBroken) {

        // Check if this damage will break the item
        int currentDamage = stack.getDamageValue();
        int maxDamage = stack.getMaxDamage();
        boolean willBreak = (currentDamage + amount) >= maxDamage;

        if (willBreak) {
            // Replace with broken sword variant BEFORE the item breaks
            ItemStack broken = t0001Items.DRAGON_GOD_SWORD_BROKEN.get().getDefaultInstance();

            if (entity.getMainHandItem() == stack) {
                entity.setItemInHand(InteractionHand.MAIN_HAND, broken);
            } else if (entity.getOffhandItem() == stack) {
                entity.setItemInHand(InteractionHand.OFF_HAND, broken);
            }

            handleBreak(entity, broken);


            return 0; // Return 0 to prevent further damage
        }

        return super.damageItem(stack, amount, entity, onBroken);
    }
}