package sid.base.world.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.jetbrains.annotations.NotNull;
import sid.base.main.t0001;
import yesman.epicfight.world.item.TieredWeaponItem;
import yesman.epicfight.world.item.WeaponItem;

import java.util.List;
import java.util.Random;

import static sid.base.gameasset.ReusableEvents.handleBreak;

public class DragonGodSwordItem extends WeaponItem implements IItemExtension {


    public DragonGodSwordItem(Properties properties) {
        super(properties);
    }

    public static ItemAttributeModifiers createDragonGodSwordAttributes(float attackDamage, float attackSpeed, Tier tier) {
        return TieredWeaponItem.createAttributes(tier,attackDamage,attackSpeed);
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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
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
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHurtEnemy(stack, target, attacker);

        float amount = 1;

        int currentDamage = stack.getDamageValue();
        int maxDamage = stack.getMaxDamage();
        boolean willBreak = (currentDamage + amount) >= maxDamage;

        if (willBreak) {
            // Replace with broken sword variant BEFORE the item breaks
            ItemStack broken = t0001Items.DRAGON_GOD_SWORD_BROKEN.get().getDefaultInstance();

            if (attacker.getMainHandItem() == stack) {
                attacker.setItemInHand(InteractionHand.MAIN_HAND, broken);
            } else if (attacker.getOffhandItem() == stack) {
                attacker.setItemInHand(InteractionHand.OFF_HAND, broken);
            }

            handleBreak(attacker, broken);
        }

    }
}


