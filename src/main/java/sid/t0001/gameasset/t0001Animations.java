package sid.t0001.gameasset;



import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.particle.EpicFightParticles;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationManager.AnimationBuilder;
import yesman.epicfight.api.animation.AnimationManager.AnimationRegistryEvent;
import yesman.epicfight.api.animation.property.AnimationEvent.InTimeEvent;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.DodgeAnimation;

import java.util.Set;

import yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackAnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackPhaseProperty;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.utils.HitEntityList;
import yesman.epicfight.api.utils.TimePairList;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;


import yesman.epicfight.gameasset.EpicFightSounds;
import org.xame.t0001;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.ColliderPreset;

import static sid.t0001.gameasset.ReusableEvents.AFTER_IMAGE;


//this fucking took ages, fuck coding, thank god, I switched to intellij otherwise I would have died on VS Code

@Mod.EventBusSubscriber(modid = t0001.MODID, bus = Bus.MOD)
public class t0001Animations {
    public static AnimationAccessor<DodgeAnimation> ACCELERATE;
    public static AnimationAccessor<DodgeAnimation> ACCELERATE_BACK;
    public static AnimationAccessor<AttackAnimation> FANG_COUNTER;

    @SubscribeEvent
    public static void registerAnimations(AnimationRegistryEvent event) {
        event.newBuilder(t0001.MODID, t0001Animations::build);
    }

    public static void build(AnimationBuilder builder) {
        //  multiple afterimages (blur effect)
        ACCELERATE = builder.nextAccessor("biped/skill/accelerate_dodge", (accessor) ->
                new DodgeAnimation(0.1F, accessor, 0.2F, 0.4F, Armatures.BIPED)
                        // Spawn a trail of afterimages across the dodge
                        .addEvents(InTimeEvent.create(0.14F, AFTER_IMAGE, AnimationEvent.Side.CLIENT))
                        .addEvents(InTimeEvent.create(0.27F, AFTER_IMAGE, AnimationEvent.Side.CLIENT))
                        .addEvents(InTimeEvent.create(0.36F, AFTER_IMAGE, AnimationEvent.Side.CLIENT))
                        .addEvents(InTimeEvent.create(0.44F, AFTER_IMAGE, AnimationEvent.Side.CLIENT))
                        .addEvents(InTimeEvent.create(0.51F, AFTER_IMAGE, AnimationEvent.Side.CLIENT))
                        // Dodge sound
                        .addEvents(InTimeEvent.create(0.0F, Animations.ReusableSources.PLAY_SOUND, AnimationEvent.Side.CLIENT)
                                .params(t0001Sounds.SMOOTH_DODGE.get()))
        );


        ACCELERATE_BACK = builder.nextAccessor("biped/skill/accelerate_dodge_back", (accessor) ->
                new DodgeAnimation(0.1F, accessor, 0.4F, 0.8F, Armatures.BIPED)
                        .addEvents(InTimeEvent.create(0.0F, Animations.ReusableSources.PLAY_SOUND, AnimationEvent.Side.CLIENT)
                                .params(t0001Sounds.SLAM_SFX.get()))
                        .addEvents(InTimeEvent.create(0.0F, Animations.ReusableSources.FRACTURE_GROUND_SIMPLE, AnimationEvent.Side.SERVER)
                                .params(new yesman.epicfight.api.utils.math.Vec3f(0.0F, 0.0F, -0.01F),
                                        Armatures.BIPED.get().legL, 1.5D, .15F))
                       // .addEvents(ReusableEvents.MyFxHelpers.blockFX(new ResourceLocation("photon:fireline"),0.0F))
        );



        FANG_COUNTER = builder.nextAccessor("biped/skill/jun_take_43", (accessor) -> new AttackAnimation(0.0F, accessor, Armatures.BIPED,
                //  - Right hand Stun strike
                new AttackAnimation.Phase(0.01F, 0.2F, 0.01F, 0.3F, 5.0F, 1.2F,
                        Armatures.BIPED.get().toolR, ColliderPreset.FIST)
                        .addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_ROD.get())
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.LONG)
                        .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.AIR_BURST)
                        .addProperty(AttackPhaseProperty.HIT_SOUND, t0001Sounds.HIT_BOOM.get())
                        .addProperty(AttackPhaseProperty.HIT_PRIORITY, HitEntityList.Priority.TARGET)
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(0.2F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(15.0F))
                ,

                // Left leg stun kick
                new AttackAnimation.Phase(1.45F, 1.55F, 1.60F, 2.1F, 5.0F, 2.0F,
                        Armatures.BIPED.get().legL, ColliderPreset.FIST)
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(2.0F))
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.NEUTRALIZE)
                        .addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_ROD.get())
                        .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.AIR_BURST)
                        .addProperty(AttackPhaseProperty.SOURCE_TAG,  Set.of(EpicFightDamageTypeTags.FINISHER, EpicFightDamageTypeTags.UNBLOCKALBE))
                        .addProperty(AttackPhaseProperty.HIT_SOUND, t0001Sounds.HIT_BOOM.get())
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))



                /* Elbow follow-up disabled due to anim problem in blender
                new AttackAnimation.Phase(0.0F, 2.4F, 2.90F, 3.0F, 3.0F, 5.0F,
                        Armatures.BIPED.get().elbowL, ColliderPreset.FIST)
                        .addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN)
                        .addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.2F))
                        .addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, EpicFightSounds.NEUTRALIZE_MOBS.get())
                        .addProperty(AnimationProperty.AttackPhaseProperty.HIT_PRIORITY, HitEntityList.Priority.TARGET)*/
        )

                .addProperty(AttackAnimationProperty.CANCELABLE_MOVE, false)
                .addState(EntityState.MOVEMENT_LOCKED, true)
                .addState(EntityState.LOCKON_ROTATE, true)
                .addState(EntityState.CAN_BASIC_ATTACK, false)
                .addState(EntityState.CAN_SKILL_EXECUTION, false)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0, 45))
                .addProperty(AttackAnimationProperty.MOVE_VERTICAL, true)
                .addProperty(AttackAnimationProperty.PLAY_SPEED_MODIFIER,  (anim, entity, elapsed, total, partialTicks) ->
                        1.35F)

                .addEvents(
                        InTimeEvent.create(0.35F, (entitypatch, animation, params) -> {
                            if (!entitypatch.isLastAttackSuccess()) {
                                entitypatch.playAnimationSynchronized(Animations.BIPED_IDLE, 0.0F);
                            }
                        }, AnimationEvent.Side.BOTH)
                )

    );




















        //I'll finish this later (I'm probably never gonna)
       /* private static final AnimationEvent.E0 SLAM_GIN = (self, entitypatch, transformSheet) -> {

            HitResult hitResult = entitypatch.getOriginal().pick(50.0D, 1.0F, false);
            Vec3 to = hitResult.getLocation();
            Vec3 from = entitypatch.getOriginal().position();
            Vec3 correction = to.subtract(from).normalize().scale(5.0D);

            TransformSheet correctedCoord = self.getCoord().getCorrectedModelCoord(entitypatch, from, to.add(correction), 0, 2);
            transformSheet.readFrom(correctedCoord);
        };*/

}
}






























