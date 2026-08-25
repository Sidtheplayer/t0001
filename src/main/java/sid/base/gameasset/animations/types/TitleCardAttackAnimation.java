package sid.base.gameasset.animations.types;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.Nullable;
import sid.base.gameasset.animations.CustomAnimationProperties;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;

import java.util.Set;

/// 😏
public class TitleCardAttackAnimation extends AttackAnimation {

    public TitleCardAttackAnimation(float transitionTime, float antic, float preDelay, float contact, float recovery, @Nullable Collider collider, Joint colliderJoint, AnimationManager.AnimationAccessor<? extends AttackAnimation> accessor, AssetAccessor<? extends Armature> armature) {
        super(transitionTime, antic, preDelay, contact, recovery, collider, colliderJoint, accessor, armature);

        this.addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.EXECUTION, DamageTypeTags.BYPASSES_INVULNERABILITY));
        this.addProperty(AnimationProperty.AttackAnimationProperty.FIXED_HEAD_ROTATION, true); // remove all movement
        this.addProperty(AnimationProperty.AttackAnimationProperty.REMOVE_DELTA_MOVEMENT, true);
        this.addProperty(AnimationProperty.AttackAnimationProperty.STOP_MOVEMENT, true);
        this.addProperty(AnimationProperty.AttackAnimationProperty.CANCELABLE_MOVE, false);
        this.newTimePair(0.0f, Float.MAX_VALUE).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false);
        this.newTimePair(0.0f, Float.MAX_VALUE).addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false);
        this.newTimePair(0.0f, Float.MAX_VALUE).addStateRemoveOld(EntityState.UPDATE_LIVING_MOTION,false);
        this.addState(EntityState.TURNING_LOCKED, true);
        this.addState(EntityState.MOVEMENT_LOCKED, true);
        this.addState(EntityState.INACTION,true);
        this.addState(EntityState.ATTACK_RESULT, (damageSource -> AttackResult.ResultType.MISSED));// invincibility

    }

    public TitleCardAttackAnimation(float transitionTime, float antic, float preDelay, float contact, float recovery, InteractionHand hand, @Nullable Collider collider, Joint colliderJoint, AnimationManager.AnimationAccessor<? extends AttackAnimation> accessor, AssetAccessor<? extends Armature> armature) {
        super(transitionTime, antic, preDelay, contact, recovery, hand, collider, colliderJoint, accessor, armature);

        this.addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.EXECUTION, DamageTypeTags.BYPASSES_INVULNERABILITY));
        this.addProperty(AnimationProperty.AttackAnimationProperty.FIXED_HEAD_ROTATION, true); // remove all movement
        this.addProperty(AnimationProperty.AttackAnimationProperty.REMOVE_DELTA_MOVEMENT, true);
        this.addProperty(AnimationProperty.AttackAnimationProperty.STOP_MOVEMENT, true);
        this.addProperty(AnimationProperty.AttackAnimationProperty.CANCELABLE_MOVE, false);
        this.newTimePair(0.0f, Float.MAX_VALUE).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false);
        this.newTimePair(0.0f, Float.MAX_VALUE).addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false);
        this.newTimePair(0.0f, Float.MAX_VALUE).addStateRemoveOld(EntityState.UPDATE_LIVING_MOTION,false);
        this.addState(EntityState.INACTION,true);
        this.addState(EntityState.TURNING_LOCKED, true);
        this.addState(EntityState.MOVEMENT_LOCKED, true);
        this.addState(EntityState.ATTACK_RESULT, (damageSource -> AttackResult.ResultType.MISSED));// invincibility

    }

    public TitleCardAttackAnimation(float transitionTime, AnimationManager.AnimationAccessor<? extends AttackAnimation> accessor, AssetAccessor<? extends Armature> armature, Phase... phases) {
        super(transitionTime, accessor, armature, phases);
        this.addProperty(AnimationProperty.AttackAnimationProperty.CANCELABLE_MOVE, false);
        this.addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.EXECUTION, DamageTypeTags.BYPASSES_INVULNERABILITY));
        this.addProperty(AnimationProperty.AttackAnimationProperty.FIXED_HEAD_ROTATION, true); // remove all movement
        this.addProperty(AnimationProperty.AttackAnimationProperty.REMOVE_DELTA_MOVEMENT, true);
        this.addProperty(AnimationProperty.AttackAnimationProperty.STOP_MOVEMENT, true);
        this.newTimePair(0.0f, Float.MAX_VALUE).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false);
        this.newTimePair(0.0f, Float.MAX_VALUE).addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false);
        this.newTimePair(0.0f, Float.MAX_VALUE).addStateRemoveOld(EntityState.UPDATE_LIVING_MOTION,false);
        this.addState(EntityState.INACTION,true);
        this.addState(EntityState.TURNING_LOCKED, true);
        this.addState(EntityState.MOVEMENT_LOCKED, true);
        this.addState(EntityState.ATTACK_RESULT, (damageSource -> AttackResult.ResultType.MISSED));// invincibility
    }

    public TitleCardAttackAnimation(float convertTime, float antic, float preDelay, float contact, float recovery, InteractionHand hand, @Nullable Collider collider, Joint colliderJoint, String path, AssetAccessor<? extends Armature> armature) {
        super(convertTime, antic, preDelay, contact, recovery, hand, collider, colliderJoint, path, armature);
        this.addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.EXECUTION, DamageTypeTags.BYPASSES_INVULNERABILITY));
        this.addProperty(AnimationProperty.AttackAnimationProperty.FIXED_HEAD_ROTATION, true); // remove all movement
        this.addProperty(AnimationProperty.AttackAnimationProperty.REMOVE_DELTA_MOVEMENT, true);
        this.addProperty(AnimationProperty.AttackAnimationProperty.CANCELABLE_MOVE, false);
        this.addProperty(AnimationProperty.AttackAnimationProperty.STOP_MOVEMENT, true);
        this.newTimePair(0.0F, Float.MAX_VALUE).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false);
        this.newTimePair(0.0F, Float.MAX_VALUE).addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false);
        this.addStateRemoveOld(EntityState.UPDATE_LIVING_MOTION,false);
        this.addState(EntityState.TURNING_LOCKED, true);
        this.addState(EntityState.MOVEMENT_LOCKED, true);
        this.addState(EntityState.ATTACK_RESULT, (damageSource -> AttackResult.ResultType.MISSED));// invincibility
    }


    public TitleCardAttackAnimation(float convertTime, String path, AssetAccessor<? extends Armature> armature, Phase... phases) {
        super(convertTime, path, armature, phases);
        this.addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.EXECUTION, DamageTypeTags.BYPASSES_INVULNERABILITY));
        this.addProperty(AnimationProperty.AttackAnimationProperty.FIXED_HEAD_ROTATION, true); // remove all movement
        this.addProperty(AnimationProperty.AttackAnimationProperty.REMOVE_DELTA_MOVEMENT, true);
        this.addProperty(AnimationProperty.AttackAnimationProperty.CANCELABLE_MOVE, false);
        this.addProperty(AnimationProperty.AttackAnimationProperty.STOP_MOVEMENT, true);
        this.addProperty(AnimationProperty.AttackAnimationProperty.STOP_MOVEMENT, true);

        this.addState(EntityState.TURNING_LOCKED, true);
        this.addState(EntityState.MOVEMENT_LOCKED, true);
        this.addState(EntityState.ATTACK_RESULT, (damageSource -> AttackResult.ResultType.MISSED));// invincibility
    }




    @Override
    public void tick(LivingEntityPatch<?> entitypatch) {
        super.tick(entitypatch);
        handleTProperties(entitypatch);
    }

    @Override
    public void linkTick(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> linkAnimation) {
        super.linkTick(entitypatch, linkAnimation);
        handleLTProperties(entitypatch, linkAnimation);
    }

    protected void handleLTProperties(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> linkAnimation){
        AnimationPlayer player = entitypatch.getAnimator().getPlayerFor(null);
        if(player == null || player.getAnimation().checkType(TitleCardAttackAnimation.class)) return;

        this.getProperty(CustomAnimationProperties.SSSpecialAnimationProperty.NO_PHYSICS_TIME).ifPresent((noPhysicsTime) -> {
            entitypatch.getOriginal().noPhysics = noPhysicsTime.isTimeInPairs(linkAnimation.get().isLinkAnimation() ? 0.0f : player.getElapsedTime());
                }
        );

    }

    protected void handleTProperties(LivingEntityPatch<?> entitypatch){
        AnimationPlayer player = entitypatch.getAnimator().getPlayerFor(null);
        if(player == null || player.getAnimation().checkType(TitleCardAttackAnimation.class)) return;

        this.getProperty(CustomAnimationProperties.SSSpecialAnimationProperty.NO_PHYSICS_TIME).ifPresent((noPhysicsTime) -> {
            entitypatch.getOriginal().noPhysics = noPhysicsTime.isTimeInPairs(player.getElapsedTime());
                }
        );

    }

    @Override
    public void end(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> nextAnimation, boolean isEnd) {
        super.end(entitypatch, nextAnimation, isEnd);

        this.getProperty(CustomAnimationProperties.SSSpecialAnimationProperty.NO_PHYSICS_TIME).ifPresent((noPhysicsTime) -> {
                    entitypatch.getOriginal().noPhysics = false;
                }
        );

    }


}
