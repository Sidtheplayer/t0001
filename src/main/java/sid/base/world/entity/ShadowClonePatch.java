package sid.base.world.entity;

import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;

public class ShadowClonePatch extends HumanoidMobPatch<ShadowCloneEntity> {


    public ShadowClonePatch(ShadowCloneEntity original, Faction faction) {
        super(original, Factions.NEUTRAL);
    }

    @Override
    protected void initAI() {
        super.initAI();

        this.original.goalSelector.addGoal(1, new TargetChasingGoal(this, this.original, 1.0D, true));

    }


    @Override
    public void initAnimator(Animator animator) {
        super.initAnimator(animator);
        animator.addLivingAnimation(LivingMotions.IDLE, Animations.BIPED_IDLE);
        animator.addLivingAnimation(LivingMotions.WALK, Animations.BIPED_WALK);
        animator.addLivingAnimation(LivingMotions.RUN, Animations.BIPED_RUN);
        animator.addLivingAnimation(LivingMotions.SNEAK, Animations.BIPED_SNEAK);
        animator.addLivingAnimation(LivingMotions.SWIM, Animations.BIPED_SWIM);
        animator.addLivingAnimation(LivingMotions.FLOAT, Animations.BIPED_FLOAT);
        animator.addLivingAnimation(LivingMotions.KNEEL, Animations.BIPED_KNEEL);
        animator.addLivingAnimation(LivingMotions.FALL, Animations.BIPED_FALL);
        animator.addLivingAnimation(LivingMotions.MOUNT, Animations.BIPED_MOUNT);
        animator.addLivingAnimation(LivingMotions.SIT, Animations.BIPED_SIT);
        animator.addLivingAnimation(LivingMotions.FLY, Animations.BIPED_FLYING);
        animator.addLivingAnimation(LivingMotions.DEATH, Animations.BIPED_DEATH);
        animator.addLivingAnimation(LivingMotions.JUMP, Animations.BIPED_JUMP);
        animator.addLivingAnimation(LivingMotions.CLIMB, Animations.BIPED_CLIMBING);
        animator.addLivingAnimation(LivingMotions.SLEEP, Animations.BIPED_SLEEPING);
        animator.addLivingAnimation(LivingMotions.CREATIVE_FLY, Animations.BIPED_CREATIVE_FLYING);
        animator.addLivingAnimation(LivingMotions.CREATIVE_IDLE, Animations.BIPED_CREATIVE_IDLE);


        animator.addLivingAnimation(LivingMotions.DIGGING, Animations.BIPED_DIG);
        animator.addLivingAnimation(LivingMotions.AIM, Animations.BIPED_BOW_AIM);
        animator.addLivingAnimation(LivingMotions.SHOT, Animations.BIPED_BOW_SHOT);
        animator.addLivingAnimation(LivingMotions.DRINK, Animations.BIPED_DRINK);
        animator.addLivingAnimation(LivingMotions.EAT, Animations.BIPED_EAT);
        animator.addLivingAnimation(LivingMotions.SPECTATE, Animations.BIPED_SPYGLASS_USE);
    }

    @Override
    public void updateMotion(boolean considerInaction) {
        super.commonMobUpdateMotion(considerInaction);
    }
}
