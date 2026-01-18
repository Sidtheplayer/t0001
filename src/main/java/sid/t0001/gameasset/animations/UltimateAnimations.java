package sid.t0001.gameasset.animations;

import net.minecraft.world.entity.LivingEntity;
import sid.t0001.utils.CGSColliderPresets;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.LongHitAnimation;
import yesman.epicfight.api.utils.HitEntityList;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.model.armature.HumanoidArmature;

import javax.swing.*;

public class UltimateAnimations {
    public static AnimationManager.AnimationAccessor<AttackAnimation> ONE_INCH_COUNTER;
    public static AnimationManager.AnimationAccessor<LongHitAnimation> ONE_INCH_COUNTER_HIT;


    public static void build(AnimationManager.AnimationBuilder builder) {
        Armatures.ArmatureAccessor<HumanoidArmature> biped = Armatures.BIPED;



        ONE_INCH_COUNTER = builder.nextAccessor("biped/skill/one_inch_counter/one_inch_counter", (accessor) -> new AttackAnimation(0.01F, accessor,biped
        ,
                new AttackAnimation.Phase(0.01F, 0.2F, 0.01F, 0.3F, 1.0F, 1.2F,
                        biped.get().handR,CGSColliderPresets.ONE_INCH_COUNTER)
                        .addProperty(AnimationProperty.AttackPhaseProperty.SWING_SOUND, EpicFightSounds.BLUNT_HIT_HARD.get())
                        .addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, EpicFightSounds.LASER_BLAST.get())
                        .addProperty(AnimationProperty.AttackPhaseProperty.HIT_PRIORITY, HitEntityList.Priority.TARGET)
                        .addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.adder((float) Math.pow(10, 2)))

        )
                        .addProperty(AnimationProperty.AttackAnimationProperty.FIXED_HEAD_ROTATION,true)
                        .addState(EntityState.PHASE_LEVEL,3)
                        .addProperty(AnimationProperty.AttackAnimationProperty.CANCELABLE_MOVE, false)
                        .addProperty(AnimationProperty.AttackAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)



        );



        ONE_INCH_COUNTER_HIT = builder.nextAccessor("biped/skill/one_inch_counter/one_inch_counter_hit", (accessor) -> new LongHitAnimation(0.12F, accessor,biped)
                .addProperty(AnimationProperty.ActionAnimationProperty.CANCELABLE_MOVE,false)
                .addEvents(AnimationProperty.ActionAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create((
                                (entitypatch, animation, params) -> {
                                 entitypatch.getOriginal().deathTime = 50;
                                 var dmgsrc = entitypatch.getOriginal().damageSources();
                                 LivingEntity original = entitypatch.getOriginal();
                                 entitypatch.getOriginal().hurt(dmgsrc.generic(), (float) Math.pow(10, original.getArmorValue()));
                                }
                        )
                        , AnimationEvent.Side.SERVER
                ))
        );
    }
}
