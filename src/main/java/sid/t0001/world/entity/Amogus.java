package sid.t0001.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sid.t0001.gameasset.t0001Entities;
import sid.t0001.gameasset.t0001Sounds;
import yesman.epicfight.world.item.EpicFightItems;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

@SuppressWarnings("deprecation")
public class Amogus extends TamableAnimal implements NeutralMob {
    public Amogus(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.setItemInHand(InteractionHand.MAIN_HAND, EpicFightItems.IRON_DAGGER.get().getDefaultInstance());
    }

    private int ambientSoundCooldown = 0;

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D);
    }

    public static final Predicate<LivingEntity> PREY_SELECTOR = entity -> {
        EntityType<?> entityType = entity.getType();
        return entityType == EntityType.VILLAGER || entityType == EntityType.FOX;
    };

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(6, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(10, new FollowOwnerGoal(this,0.3D,20,30, false));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, (new HurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, true, this::shouldAttack));
        this.targetSelector.addGoal(5, new NonTameRandomTargetGoal<>(this, Animal.class, false, PREY_SELECTOR));
        this.targetSelector.addGoal(6, new NonTameRandomTargetGoal<>(this, Turtle.class, false, Turtle.BABY_ON_LAND_SELECTOR));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, AbstractSkeleton.class, true, this::shouldAttack));
        this.targetSelector.addGoal(8, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.getItem().isEdible() && Objects.requireNonNull(stack.getItem().getFoodProperties()).isMeat();
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (target.getType() == this.getType()
                && target instanceof TamableAnimal other
                && this.isTame()
                && other.isTame()) {
            return false;
        }
        return super.canAttack(target);
    }

    private boolean shouldAttack(LivingEntity target) {
        return target.getType() != this.getType()
                || !(target instanceof TamableAnimal other)
                || !this.isTame()
                || !other.isTame();
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        Item item = itemstack.getItem();

        if (item.isEdible() && Objects.requireNonNull(item.getFoodProperties()).isMeat()) {
            if (!this.isTame()) {
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
                if (this.random.nextInt(3) == 0) {
                    this.tame(player);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
                return InteractionResult.SUCCESS;
            } else if (this.getHealth() < this.getMaxHealth()) {
                this.heal((float) item.getFoodProperties().getNutrition());
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }

        if (this.isTame() && player.isShiftKeyDown()) {
            this.setOrderedToSit(!this.isOrderedToSit());
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public Amogus getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob other) {
        Amogus amogus = t0001Entities.AMOGUS.get().create(level);
        if (amogus != null) {
            UUID uuid = this.getOwnerUUID();
            if (uuid != null) {
                amogus.setOwnerUUID(uuid);
                amogus.setTame(true);
            }
        }
        return amogus;
    }

    public boolean canMate(@NotNull Animal other) {
        if (other == this) return false;
        if (!this.isTame()) return false;
        if (!(other instanceof Amogus a)) return false;
        if (!a.isTame()) return false;
        if (a.isInSittingPose()) return false;
        return this.isInLove() && a.isInLove();
    }

    private int remainingPersistentAngerTime;
    private UUID persistentAngerTarget;

    @Override public int getRemainingPersistentAngerTime() { return this.remainingPersistentAngerTime; }
    @Override public void setRemainingPersistentAngerTime(int time) { this.remainingPersistentAngerTime = time; }
    @Override public @Nullable UUID getPersistentAngerTarget() { return this.persistentAngerTarget; }
    @Override public void setPersistentAngerTarget(@Nullable UUID target) { this.persistentAngerTarget = target; }
    @Override public void startPersistentAngerTimer() { this.remainingPersistentAngerTime = 600; }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState blockIn) {
        this.playSound(SoundEvents.VINE_STEP, 0.15F, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.ambientSoundCooldown > 0) this.ambientSoundCooldown--;
    }

    @Override protected SoundEvent getAmbientSound() {
        if (this.ambientSoundCooldown > 0) return null;
        this.ambientSoundCooldown = 100 + this.random.nextInt(100);
        return this.random.nextFloat() < 0.005F
                ? t0001Sounds.AMOGUS_AMBIENT.get()
                : SoundEvents.WOLF_AMBIENT;
    }

    @Override protected SoundEvent getHurtSound(@NotNull DamageSource source) { return SoundEvents.HOSTILE_HURT; }
    @Override protected SoundEvent getDeathSound() { return t0001Sounds.AMOGUS_DEATH.get(); }
    @Override protected float getSoundVolume() { return 0.25F; }
}
