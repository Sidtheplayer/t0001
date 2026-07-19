package sid.base.gameasset.animations.types;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
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
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
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

        this.stateSpectrumBlueprint.clear();

        this.addProperty(AnimationProperty.ActionAnimationProperty.CANCELABLE_MOVE, false)
                .addProperty(AnimationProperty.ActionAnimationProperty.IS_DEATH_ANIMATION, true)
                .addProperty(AnimationProperty.ActionAnimationProperty.FIXED_HEAD_ROTATION, true)
                .addState(EntityState.ATTACK_RESULT, damageSource -> {
                    if (damageSource instanceof EpicFightDamageSource epicFightDamageSource) {
                        epicFightDamageSource.setStunType(StunType.NONE);
                        epicFightDamageSource.addRuntimeTag(EpicFightDamageTypeTags.NO_STUN);
                    }
                    return damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || damageSource.is(EpicFightDamageTypeTags.EXECUTION) ?
                            AttackResult.ResultType.SUCCESS : AttackResult.ResultType.MISSED;
                });
    }

    @Override
    public void begin(LivingEntityPatch<?> entitypatch) {
        super.begin(entitypatch);
        name = IdentifierProvider.constant(entitypatch.getOriginal().getStringUUID() + "_" + atomicCounter.incrementAndGet());

        if (!entitypatch.getLevel().isClientSide()) {
            entitypatch.getEventListener().registerEvent(EpicFightEventHooks.Entity.TAKE_DAMAGE_PRE,(event) -> {
                if (event.getDamageSource().getEntity() != null) {

                    LivingEntity attacker = event.getDamageSource().getEntity().getControllingPassenger();

                    EpicFightCapabilities.getUnparameterizedEntityPatch(attacker, LivingEntityPatch.class).ifPresent(
                            targetPatch ->{
                                targetPatch.getEventListener().registerEvent(EpicFightEventHooks.Entity.DELIVER_DAMAGE_POST,(evt) -> {
                                    DamageSource damageSource = evt.getDamageSource();
                                    float damage = evt.getModifiedDamage();
                                    if(!damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && damageSource.is(ExtraSpecialDamageTypeTags.SPECIAL_EXECUTION)) {
                                        float health = entitypatch.getOriginal().getHealth();
                                        if(damage >= health){
                                            damage = health - 0.01f;
                                        }
                                        evt.setModifiedDamage(damage);
                                    }
                                }, name);
                            }
                    );
                }

            }, name);
        }
    }

    @Override
    public void end(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> nextAnimation, boolean isEnd) {
        super.end(entitypatch, nextAnimation, isEnd);

        if (!entitypatch.getLevel().isClientSide()) {
            //A little trick I came up with when coding dawn day's battle staff innate
            LivingEntity lastAttacker = entitypatch.getOriginal().getLastAttacker();
            EpicFightCapabilities.getUnparameterizedEntityPatch(lastAttacker, LivingEntityPatch.class).ifPresent(targetPatch -> {
                targetPatch.getEventListener().removeListenersBelongTo(name);
            });
            entitypatch.getEventListener().removeListenersBelongTo(name);
        }

    }

}
