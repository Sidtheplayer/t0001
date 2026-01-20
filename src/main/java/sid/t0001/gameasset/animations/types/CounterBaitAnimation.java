package sid.t0001.gameasset.animations.types;

import net.minecraft.world.InteractionHand;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.LongHitAnimation;
import yesman.epicfight.api.animation.types.grappling.GrapplingAttackAnimation;
import yesman.epicfight.api.animation.types.grappling.GrapplingTryAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class CounterBaitAnimation extends GrapplingTryAnimation {
    private final float counterWindowStart;
    private final float counterWindowEnd;
    private boolean counterTriggered = false;

    private final AnimationManager.AnimationAccessor<? extends ActionAnimation> counterFailAnimation;

    public CounterBaitAnimation(float convertTime, float antic, float preDelay, float contact, float recovery,
                                float counterWindowStart, float counterWindowEnd,
                                Collider collider, Joint colliderJoint,
                                AnimationManager.AnimationAccessor<? extends GrapplingTryAnimation> accessor,
                                AnimationManager.AnimationAccessor<? extends LongHitAnimation> grapplingHitAnimation,
                                AnimationManager.AnimationAccessor<? extends GrapplingAttackAnimation> grapplingAttackAnimation,
                                AnimationManager.AnimationAccessor<? extends ActionAnimation> failAnimation,
                                AssetAccessor<? extends Armature> armature) {
        super(convertTime, antic, preDelay, contact, recovery, collider, colliderJoint, accessor,
                grapplingHitAnimation, grapplingAttackAnimation, failAnimation, armature);
        this.counterWindowStart = counterWindowStart;
        this.counterWindowEnd = counterWindowEnd;
        this.counterFailAnimation = failAnimation;


        this.addProperty(ActionAnimationProperty.MOVE_ON_LINK, false);
        this.addProperty(ActionAnimationProperty.COORD_SET_BEGIN, null);
        this.addProperty(ActionAnimationProperty.COORD_SET_TICK, null);
    }

    public CounterBaitAnimation(float convertTime, float antic, float preDelay, float contact, float recovery,
                                float counterWindowStart, float counterWindowEnd,
                                InteractionHand hand, Collider collider, Joint colliderJoint,
                                AnimationManager.AnimationAccessor<? extends GrapplingTryAnimation> accessor,
                                AnimationManager.AnimationAccessor<? extends LongHitAnimation> grapplingHitAnimation,
                                AnimationManager.AnimationAccessor<? extends GrapplingAttackAnimation> grapplingAttackAnimation,
                                AnimationManager.AnimationAccessor<? extends ActionAnimation> failAnimation,
                                AssetAccessor<? extends Armature> armature) {
        super(convertTime, antic, preDelay, contact, recovery, hand, collider, colliderJoint, accessor,
                grapplingHitAnimation, grapplingAttackAnimation, failAnimation, armature);
        this.counterWindowStart = counterWindowStart;
        this.counterWindowEnd = counterWindowEnd;
        this.counterFailAnimation = failAnimation;


        this.addProperty(ActionAnimationProperty.MOVE_ON_LINK, false);
        this.addProperty(ActionAnimationProperty.COORD_SET_BEGIN, null);
        this.addProperty(ActionAnimationProperty.COORD_SET_TICK, null);
    }

    @Override
    public void begin(LivingEntityPatch<?> entitypatch) {
        super.begin(entitypatch);
        this.counterTriggered = false;

        // Stop all movement
        if (!entitypatch.isLogicalClient()) {
            entitypatch.getOriginal().setDeltaMovement(0, entitypatch.getOriginal().getDeltaMovement().y, 0);
        }
    }


    @Override
    public void tick(LivingEntityPatch<?> entitypatch) {
        super.tick(entitypatch);

        // Keep player stationary during bait animation
        if (!entitypatch.isLogicalClient() && !this.counterTriggered) {
            entitypatch.getOriginal().setDeltaMovement(0, entitypatch.getOriginal().getDeltaMovement().y, 0);
        }
    }

    @Override
    public void end(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> nextAnimation, boolean isEnd) {

        if (isEnd && !this.counterTriggered && !entitypatch.isLogicalClient()) {
            entitypatch.reserveAnimation(this.counterFailAnimation);
        }

        // Reset state
        this.counterTriggered = false;
    }

    @Override
    protected void attackTick(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> animation) {
        // No attack behavior
    }

    // Method to be called by the skill event listener when counter is triggered, i think this is not a good code method
    public void triggerCounter() {
        this.counterTriggered = true;
    }

    // Getter methods, I feel like I have complicated a problem that didn't need to be complicated
    public float getCounterWindowStart() {
        return counterWindowStart;
    }

    public float getCounterWindowEnd() {
        return counterWindowEnd;
    }

    public boolean isCounterTriggered() {
        return counterTriggered;
    }
}