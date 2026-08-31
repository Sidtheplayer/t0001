package sid.base.gameasset.animations.types;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import sid.base.gameasset.animations.CustomAnimationProperties;
import sid.base.world.ExtraSpecialDamageTypeTags;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.LongHitAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;

import java.util.HashSet;
import java.util.Set;

public class ProtectedHitAnimation extends LongHitAnimation {

    public static final Set<LivingEntity> ProtectedEntities = new HashSet<>();

    public ProtectedHitAnimation(float transitionTime, AnimationManager.AnimationAccessor<? extends LongHitAnimation> accessor, AssetAccessor<? extends Armature> armature) {
        super(transitionTime, accessor, armature);


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
                    damageSource.is(ExtraSpecialDamageTypeTags.SPECIAL_EXECUTION_FINISHER) ?
                    AttackResult.ResultType.SUCCESS : AttackResult.ResultType.MISSED;
        });
    }

    @Override
    public void begin(LivingEntityPatch<?> entitypatch) {
        super.begin(entitypatch);

        ProtectedEntities.add(entitypatch.getOriginal());
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

        this.getProperty(CustomAnimationProperties.SSSpecialAnimationProperty.NO_PHYSICS_TIME).ifPresent((noPhysicsTime) -> {
                    entitypatch.getOriginal().noPhysics = false;
                }
        );

        ProtectedEntities.remove(entitypatch.getOriginal());

        //calling super later cause of unknown fear of above code fuc'ing up
        super.end(entitypatch, nextAnimation, isEnd);

    }


}
