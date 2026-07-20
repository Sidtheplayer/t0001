package sid.base.gameasset.animations.types;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import sid.base.world.ExtraSpecialDamageTypeTags;
import sid.base.world.SpecialDamageTypes;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationProperty;
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
            return damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) ||
                    damageSource.is(ExtraSpecialDamageTypeTags.SPECIAL_EXECUTION) ||
                    damageSource.is(SpecialDamageTypes.SPECIAL_EXECUTION_FINISHER) ?
                    AttackResult.ResultType.SUCCESS : AttackResult.ResultType.MISSED;
        });
    }

    @Override
    public void begin(LivingEntityPatch<?> entitypatch) {
        super.begin(entitypatch);

        name = IdentifierProvider.constant(entitypatch.getOriginal().getStringUUID() + "_" + atomicCounter.incrementAndGet());

        entitypatch.getEventListener().registerEvent(EpicFightEventHooks.Entity.TAKE_DAMAGE_PRE, (event) -> {

            System.out.println("Pre-DmgEvent Registered!: " + name.getStringId());

            if (!entitypatch.isLogicalClient()) {

                float damage = event.getDamage();

                DamageSource damageSource = event.getDamageSource();

                if (!damageSource.is(ExtraSpecialDamageTypeTags.SPECIAL_EXECUTION) &&
                        !damageSource.is(SpecialDamageTypes.SPECIAL_EXECUTION_FINISHER)) {
                    System.out.println("Blocked non-execution damage from: " + damageSource.getMsgId());
                    event.cancel();
                    return;
                }


                if (damageSource.is(SpecialDamageTypes.SPECIAL_EXECUTION_FINISHER)) {
                    System.out.println("Execution finished");
                    return;
                }

                if (!damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) &&
                        damageSource.is(ExtraSpecialDamageTypeTags.SPECIAL_EXECUTION)) {

                    float health = entitypatch.getOriginal().getHealth();

                    if (damage >= health) {
                        float cappedDamage = Math.max(health - 0.01f, 0.0f);
                        event.attachValueModifier(ValueModifier.setter(cappedDamage));
                        System.out.println("   Capped execution damage from " + damage + " to " + cappedDamage);
                    } else {
                        System.out.println("   Execution damage below health: " + damage + " < " + health);
                    }
                }
            }


        }, name, -100);

        entitypatch.getEventListener().registerEvent(EpicFightEventHooks.Animation.END, (event) ->
        {
            if (event.getAnimation().get().equals(this)) {
                System.out.println("Pre-DmgEvent Removed!: " + name.getStringId());
                entitypatch.getEventListener().removeListenersBelongTo(name);
            }

        }, name);


    }


}
