package sid.base.world.entity;

import com.lowdragmc.photon.client.fx.BlockEffectExecutor;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXHelper;
import com.lowdragmc.photon.command.BlockEffectCommand;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sid.base.main.t0001;
import sid.base.utils.HelperUtils;
import sid.base.world.t0001Sounds;
import yesman.epicfight.registry.entries.EpicFightAttributes;
import yesman.epicfight.registry.entries.EpicFightItems;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public void die(@NotNull DamageSource cause) {
        super.die(cause);

        if(this.level().isClientSide)return;

        this.level().playSound(null,
                this.position().x,
                this.position().y + 0.25,
                this.position().z,
                t0001Sounds.DISPERSE,
                SoundSource.HOSTILE,
                1.0f,
                1.0f
        );



        BlockEffectCommand command = new BlockEffectCommand();
        command.setLocation(ResourceLocation.parse("photon:shadow_clone_smoke"));
        command.setPos(this.getOnPos());
        command.setRotation( new Vec3(0,0,0) );
        command.setOffset(new Vec3(0,1.35,0));
        command.setScale(new Vec3(1,1,1));
        command.setAllowMulti(true);
        command.setDelay(0);
        command.setCheckState(false);

        PacketDistributor.sendToPlayersTrackingEntity(this, command);

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
                .add(Attributes.ATTACK_SPEED, 0.40D)
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
    protected void applyTamingSideEffects() {
        super.applyTamingSideEffects();

        if (this.isTame() && !this.level().isClientSide && getOwner() instanceof ServerPlayer player) {
            ServerPlayerPatch patch = EpicFightCapabilities.getServerPlayerPatch(player);

            boolean is_awakened = HelperUtils.is_Awakened(patch);

            if (is_awakened) {


                Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(1.2D);


                Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_SPEED)).addOrReplacePermanentModifier(new AttributeModifier(
                        t0001.identifier("shadow_clone_buff"), 1.15D,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                ));

                Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).addOrReplacePermanentModifier(new AttributeModifier(
                        t0001.identifier("shadow_clone_buff"), 1.15D,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));


                try {
                    Objects.requireNonNull(this.getAttribute(EpicFightAttributes.IMPACT)).addOrReplacePermanentModifier(new AttributeModifier(
                            t0001.identifier("shadow_clone_buff"), 1.15D,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    ));
                } catch (Exception e) {
                    t0001.LOGGER.error(e.getLocalizedMessage());
                }

                this.setHealth(40.0F);
            }

        }
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
                || !isTargetSameTeam(target)
                || !(target instanceof TamableAnimal other)
                || !this.isTame()
                || !other.isTame();
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
