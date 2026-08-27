package sid.base.world.entity;

import com.lowdragmc.photon.client.fx.BlockEffectExecutor;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXHelper;
import com.lowdragmc.photon.command.BlockEffectCommand;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.registry.entries.EpicFightItems;

import java.util.ArrayList;
import java.util.List;

public class ShadowCloneEntity extends TamableAnimal {

    public ShadowCloneEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    public static List<ShadowCloneEntity> getShadowCloneList(Player player) {
        return player.level().getEntitiesOfClass(
                ShadowCloneEntity.class,
                player.getBoundingBox().inflate(64.0),
                clone -> clone.isTame() && clone.getOwner() == player
        );
    }

    public static final List<Item> USABLE_WEAPONS =
            new ArrayList<>(
                    List.of(
                    EpicFightItems.IRON_LONGSWORD.value(),
                    EpicFightItems.GLOVE.value(),
                    EpicFightItems.IRON_DAGGER.value(),
                    EpicFightItems.UCHIGATANA.value(),
                    Items.IRON_SWORD
                    )
            );



    @Override
    public boolean canFallInLove() {

        return false; //What is love?
    }

    @Override
    public boolean isCustomNameVisible() {
        return true;
    }

    @Override
    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();

        if(this.level().isClientSide){
            //TODO:STUFF
        }


    }



    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();

        if(this.getRandom().nextFloat() <= 0.35f){
            this.setItemInHand(InteractionHand.MAIN_HAND, USABLE_WEAPONS
                    .get(this.getRandom().nextInt(0, USABLE_WEAPONS.size())).getDefaultInstance());
        }

        if (getOwner() != null && getOwner() instanceof Player player) {
            setCustomName(Component.literal(player.getScoreboardName() + "'s ShadowClone"));
        }

        if(this.level().isClientSide){
            FX fx = FXHelper.getFX(ResourceLocation.parse("photon:shadow_clone_smoke"));
            if(fx == null) return;
            BlockEffectExecutor executor = new BlockEffectExecutor(fx, this.level(), this.getOnPos());
            executor.setOffset(0,1.25, 0);
            executor.setScale(1,1,1);
            executor.setRotation(0,0,0);
            executor.setAllowMulti(true);
            executor.setDelay(0);
            executor.setCheckState(false);
            executor.start();
        }


    }

    public static AttributeSupplier.Builder createBaseAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 15.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, (new HurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, true, this::shouldAttack));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, AbstractSkeleton.class, true, this::shouldAttack));
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

        // Never attack the owner's team :D
        if (isTargetSameTeam(target)) {
            return false;
        }

        // Never attack livestock :D
        if (target instanceof Cow
                || target instanceof Pig
                || target instanceof Sheep
                || target instanceof Chicken) {
            return false;
        }

        if (!(target instanceof TamableAnimal other)) {
            return false;
        }

        if (!this.isTame() || !other.isTame()) {
            return false;
        }

        return target.getType() != this.getType();
    }


    private boolean isTargetSameTeam(LivingEntity target){
        LivingEntity owner = getOwner();

        if (owner == null || owner.getTeam() == null) {
            return false;
        }

        return owner.getTeam().isAlliedTo(target.getTeam());
    }

    @Override
    public boolean isFood(@NotNull ItemStack itemStack) {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return null; //Not Breedable
    }
}
