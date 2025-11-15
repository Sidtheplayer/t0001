package sid.t0001.world.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class DarknessEntity extends PathfinderMob {
    private static final int START_LIFE = 2;
    private static final double DAMAGE_RADIUS = 3.0D;
    private static final double DETECTION_RADIUS = 15.0D;

    public DarknessEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setHealth(100.0F);

    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    @Override
    public @NotNull Iterable<ItemStack> getArmorSlots() {
        return Collections.emptyList();
    }



    @Override
    public @NotNull ItemStack getItemBySlot(@NotNull EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(@NotNull EquipmentSlot slot, @NotNull ItemStack stack) {}

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.BEACON_AMBIENT;
    }
}
