package sid.t0001.world.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;

import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import sid.t0001.client.model.t0001Armatures;
import sid.t0001.gameasset.t0001Animations;
import sid.t0001.gameasset.t0001Entities;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.gameasset.Animations;

import yesman.epicfight.gameasset.MobCombatBehaviors;
import yesman.epicfight.registry.entries.EpicFightAttributes;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.world.capabilities.entitypatch.*;
import yesman.epicfight.world.damagesource.StunType;

import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;

public class DarknessEntityPatch extends MobPatch<DarknessEntity> {



    public static void initAttributes(EntityAttributeModificationEvent event) {
        event.add(t0001Entities.DARKNESS_ENTITY.get(), EpicFightAttributes.MAX_STRIKES, 4.0D);
        event.add(t0001Entities.DARKNESS_ENTITY.get(), EpicFightAttributes.IMPACT, 2.0D);
    }

    @Override
    protected void initAI() {
        super.initAI();
      //  this.original.goalSelector.addGoal(1,new AnimatedAttackGoal<>(this,  MobCombatBehaviors.HOGLIN.build(this)));
        //would throw an error due to lack of headbone in darknessentitty
    }


    @Override
    public AssetAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
        return null;
    }

    @Override
    public void initAnimator(Animator animator) {
        super.initAnimator(animator);
        animator.addLivingAnimation(LivingMotions.IDLE, t0001Animations.DARKNESS_IDLE);
        animator.addLivingAnimation(LivingMotions.WALK,  t0001Animations.DARKNESS_IDLE);
        animator.addLivingAnimation(LivingMotions.CHASE,  t0001Animations.DARKNESS_IDLE);
        animator.addLivingAnimation(LivingMotions.FALL, Animations.BIPED_FALL);
        animator.addLivingAnimation(LivingMotions.MOUNT, Animations.BIPED_MOUNT);
        animator.addLivingAnimation(LivingMotions.DEATH, t0001Animations.DARKNESS_DEATH);
    }

    @Override
    public void updateMotion(boolean considerInaction) {
        super.commonMobUpdateMotion(considerInaction);
    }



    public DarknessEntityPatch(DarknessEntity original){
        super(original,Factions.NEUTRAL);
        this.armature= t0001Armatures.DARKNESSARMATURE.get();
    }

    public SoundEvent getWeaponHitSound(InteractionHand hand) {
        return SoundEvents.ENDERMAN_SCREAM;
    }

    @Override
    public SoundEvent getSwingSound(InteractionHand hand) {
        return EpicFightSounds.WHOOSH_SMALL.get();
    }
}
