package sid.base.gameasset.animations.types;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import sid.base.gameasset.animations.CustomAnimationProperties;
import sid.base.world.ExtraSpecialDamageTypeTags;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;

import java.util.HashSet;
import java.util.Set;


//Critical Bug: END Event firing before the actual end of the animation somehow???!?!?!? the fu-
public class ProtectedHitAnimation extends ActionAnimation {

    public static final Set<LivingEntity> ProtectedEntities = new HashSet<>();

    public ProtectedHitAnimation(float transitionTime, AnimationManager.AnimationAccessor<? extends ProtectedHitAnimation> accessor, AssetAccessor<? extends Armature> armature) {
        super(transitionTime, accessor, armature);


        this.addProperty(AnimationProperty.ActionAnimationProperty.CANCELABLE_MOVE, false);
        this.addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, true);
        this.addProperty(AnimationProperty.ActionAnimationProperty.REMOVE_DELTA_MOVEMENT, true);
        this.addProperty(AnimationProperty.ActionAnimationProperty.FIXED_HEAD_ROTATION, true);

        this.stateSpectrumBlueprint.clear()
                .newTimePair(0.0F, Float.MAX_VALUE)
                .addState(EntityState.TURNING_LOCKED, true)
                .addState(EntityState.MOVEMENT_LOCKED, true)
                .addState(EntityState.UPDATE_LIVING_MOTION, false)
                .addState(EntityState.COMBO_ATTACKS_DOABLE, false)
                .addState(EntityState.SKILL_EXECUTABLE, false)
                .addState(EntityState.INACTION, true)
                .addState(EntityState.ATTACK_RESULT, (damageSource) -> {
                    if (damageSource instanceof EpicFightDamageSource epicFightDamageSource) {
                        epicFightDamageSource.setStunType(StunType.NONE);
                        epicFightDamageSource.addRuntimeTag(EpicFightDamageTypeTags.NO_STUN);
                    }
                    return damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) ||
                            damageSource.is(ExtraSpecialDamageTypeTags.SPECIAL_EXECUTION) ||
                            damageSource.is(ExtraSpecialDamageTypeTags.SPECIAL_EXECUTION_FINISHER) ?
                            AttackResult.ResultType.SUCCESS : AttackResult.ResultType.MISSED;
                });
    }


    @Override
    public void linkTick(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> linkAnimation) {
        super.linkTick(entitypatch, linkAnimation);

        handleProperties(entitypatch,linkAnimation);
    }

    protected void handleProperties(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> animation){
        AnimationPlayer player = entitypatch.getAnimator().getPlayerFor(animation);
        if(player == null) return;

        this.getProperty(CustomAnimationProperties.SSSpecialAnimationProperty.NO_PHYSICS_TIME).ifPresent((noPhysicsTime) -> {
            if(noPhysicsTime.isTimeInPairs(animation.get().isLinkAnimation() ? 0.0F : player.getElapsedTime())){
                entitypatch.getOriginal().noPhysics = true;
            }
                }
        );

    }

    @Override
    public void end(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> nextAnimation, boolean isEnd) {
        super.end(entitypatch, nextAnimation, isEnd);

        if (isEnd) {
            this.getProperty(CustomAnimationProperties.SSSpecialAnimationProperty.NO_PHYSICS_TIME).ifPresent((noPhysicsTime) -> {
                        entitypatch.getOriginal().noPhysics = false;
                    }
            );
        }


    }


}
