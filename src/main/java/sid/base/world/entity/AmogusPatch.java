package sid.base.world.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;

import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import sid.base.client.model.t0001Armatures;
import sid.base.gameasset.t0001Entities;
import sid.base.world.ai.CustomMobCombatBehaviours;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;

import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.registry.entries.EpicFightAttributes;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.StunType;

import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;

import static yesman.epicfight.gameasset.Animations.BIPED_HIT_SHORT;

public class AmogusPatch extends HumanoidMobPatch<Amogus> {

    public AmogusPatch(Amogus original) {
        super(original, Factions.WITHER);
        this.armature = t0001Armatures.AMOGUS.get();
    }


    // you also need to make patched renderer class,
    // along with this and register it to bus (check main class and ClientModEvents)

    public static void initAttributes(EntityAttributeModificationEvent event) {
        event.add(t0001Entities.AMOGUS.get(), EpicFightAttributes.MAX_STRIKES, 4.0D);
        event.add(t0001Entities.AMOGUS.get(), EpicFightAttributes.IMPACT, 2.0D);
        event.add(t0001Entities.AMOGUS.get(), EpicFightAttributes.WEIGHT, 10.0D);
    }

    @Override
    public HitParticleType getWeaponHitParticle(InteractionHand hand) {
        return EpicFightParticles.HIT_BLADE.get();
    }

    @Override
    public void initAnimator(Animator animator) {
        super.initAnimator(animator);
        animator.addLivingAnimation(LivingMotions.IDLE, Animations.BIPED_IDLE);
        animator.addLivingAnimation(LivingMotions.WALK, Animations.BIPED_WALK);
        animator.addLivingAnimation(LivingMotions.RUN, Animations.BIPED_RUN);
        animator.addLivingAnimation(LivingMotions.DEATH, Animations.BIPED_DEATH);
    }

    @Override
    public void updateMotion(boolean considerInaction) {
        super.commonMobUpdateMotion(considerInaction);
    }


    @Override
    protected void initAI() {
        super.initAI();
        this.original.goalSelector.addGoal(0, new AnimatedAttackGoal<>(this, CustomMobCombatBehaviours.AMOGUS_AXE_INNATE_SPAM.build(this)));
        this.original.goalSelector.addGoal(1, new TargetChasingGoal(this, this.original, 1.0D, true));
    }

    @Override
    protected void setWeaponMotions() {
        this.weaponAttackMotions = Maps.newHashMap();
        this.weaponAttackMotions.put(CapabilityItem.WeaponCategories.DAGGER, ImmutableMap.of(CapabilityItem.Styles.COMMON,CustomMobCombatBehaviours.AMOGUS_AXE_INNATE_SPAM));
    }

    @Override
    public AnimationAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
        if (this.original.getVehicle() != null) {
            return Animations.BIPED_HIT_ON_MOUNT;
        } else {
            return switch (stunType) {
                case LONG -> Animations.BIPED_HIT_LONG;
                case SHORT, HOLD -> BIPED_HIT_SHORT;
                case KNOCKDOWN -> Animations.BIPED_KNOCKDOWN;
                case NEUTRALIZE -> Animations.BIPED_COMMON_NEUTRALIZED;
                case FALL -> Animations.BIPED_LANDING;
                case NONE -> null;
            };
        }

    }

    @Override
    public SoundEvent getWeaponHitSound(InteractionHand hand) {
        return EpicFightSounds.BLADE_HIT.get();
    }

    @Override
    public SoundEvent getSwingSound(InteractionHand hand) {
        return EpicFightSounds.WHOOSH_SMALL.get();
    }
}
