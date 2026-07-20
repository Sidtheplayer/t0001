package sid.base.gameasset.animations.types;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import sid.base.world.ExtraSpecialDamageTypeTags;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.LongHitAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.IdentifierProvider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;

import java.util.concurrent.atomic.AtomicLong;

public class ProtectedHitAnimation extends LongHitAnimation {

    private IdentifierProvider name;

    private static final AtomicLong atomicCounter = new AtomicLong(0);

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
            return damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || damageSource.is(ExtraSpecialDamageTypeTags.SPECIAL_EXECUTION) ?
                    AttackResult.ResultType.SUCCESS : AttackResult.ResultType.MISSED;
        });
    }

    @Override
    public void begin(LivingEntityPatch<?> entitypatch) {
        super.begin(entitypatch);

        name = IdentifierProvider.constant(entitypatch.getOriginal().getStringUUID() + "_" + atomicCounter.incrementAndGet());

        if (!entitypatch.getLevel().isClientSide()) {
            entitypatch.getEventListener().registerEvent(EpicFightEventHooks.Entity.TAKE_DAMAGE_PRE, (event) -> {

                float damage = event.getDamage();
                DamageSource damageSource = event.getDamageSource();
                if (!damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                        && damageSource.is(ExtraSpecialDamageTypeTags.SPECIAL_EXECUTION)
                ) {
                    float health = entitypatch.getOriginal().getHealth();
                    if (damage >= health) {
                        damage = Math.max(health - 0.01f, 0.0f);
                    }
                    event.attachValueModifier(ValueModifier.setter(damage));
                }


            }, name);
        }

    }

    @Override
    public void end(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> nextAnimation, boolean isEnd) {
        super.end(entitypatch, nextAnimation, isEnd);

        if (!entitypatch.getLevel().isClientSide()) {
            //A little trick I came up with when coding dawn day's battle staff innate
            entitypatch.getEventListener().removeListenersBelongTo(name);
        }

    }

}
