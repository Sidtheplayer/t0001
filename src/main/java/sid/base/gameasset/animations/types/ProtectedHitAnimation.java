package sid.base.gameasset.animations.types;

import net.minecraft.tags.DamageTypeTags;
import sid.base.world.ExtraSpecialDamageTypeTags;
import sid.base.world.SpecialDamageTypes;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.LongHitAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;

public class ProtectedHitAnimation extends LongHitAnimation {

    public ProtectedHitAnimation(float transitionTime, AnimationManager.AnimationAccessor<? extends LongHitAnimation> accessor, AssetAccessor<? extends Armature> armature) {
        super(transitionTime, accessor, armature);

        this.stateSpectrumBlueprint.clear();
        this.newTimePair(0.0f, Float.MAX_VALUE);
        this.addProperty(AnimationProperty.ActionAnimationProperty.CANCELABLE_MOVE, false);
        this.addProperty(AnimationProperty.ActionAnimationProperty.IS_DEATH_ANIMATION, true);
        this.addProperty(AnimationProperty.ActionAnimationProperty.FIXED_HEAD_ROTATION, true);
        this.addState(EntityState.ATTACK_RESULT, (damageSource) -> {
            if (damageSource instanceof EpicFightDamageSource epicFightDamageSource) {
                epicFightDamageSource.setStunType(StunType.NONE);
                epicFightDamageSource.addRuntimeTag(EpicFightDamageTypeTags.NO_STUN);
            }
            return damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) ||
                    damageSource.is(ExtraSpecialDamageTypeTags.SPECIAL_EXECUTION) ||
                    damageSource.is(SpecialDamageTypes.SPECIAL_EXECUTION_FINISHER) ?
                    AttackResult.ResultType.SUCCESS : AttackResult.ResultType.MISSED;
        });
    }



}
