package sid.base.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
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
import sid.base.world.t0001Sounds;
import yesman.epicfight.registry.entries.EpicFightItems;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;


public class Amogus extends TamableAnimal implements NeutralMob {

    public Amogus(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.setItemInHand(InteractionHand.MAIN_HAND, EpicFightItems.IRON_DAGGER.get().getDefaultInstance());
    }


    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D);
    }

    private static final Set<EntityType<?>> PREY_TYPES = Set.of(
            EntityType.VILLAGER,
            EntityType.FOX,
            EntityType.RABBIT,
            EntityType.BOAT
    );

    public static final Predicate<LivingEntity> PREY_SELECTOR = entity -> PREY_TYPES.contains(entity.getType());


    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 0.5D, 10.0F, 2.0F));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(6, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

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
        return stack.is(ItemTags.MEAT);
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

        if ( !this.level().isClientSide || this.isFood(itemstack)) {
            if (!this.isTame() && this.isFood(itemstack)) {
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
            } else if (this.getHealth() < this.getMaxHealth() && this.isFood(itemstack)) {
                this.heal((float) Objects.requireNonNull(item.getFoodProperties(itemstack, this)).nutrition());
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
                amogus.setTame(true,true);
            }
        }
        return amogus;
    }

    protected void applyTamingSideEffects() {
        if (this.isTame()) {
            Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(40.0D);
            this.setHealth(40.0F);
        } else {
            Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(8.0D);
        }

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



    @Override protected SoundEvent getAmbientSound() {
        return this.random.nextFloat() < 0.005F
                ? t0001Sounds.AMOGUS_AMBIENT.get()
                : SoundEvents.WOLF_AMBIENT;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 50;
    }

    @Override protected SoundEvent getHurtSound(@NotNull DamageSource source) { return SoundEvents.HOSTILE_HURT; }
    @Override protected SoundEvent getDeathSound() { return t0001Sounds.AMOGUS_DEATH.get(); }
    @Override protected float getSoundVolume() { return 0.25F; }
}
