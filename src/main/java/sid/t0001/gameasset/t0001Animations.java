package sid.t0001.gameasset;


import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.xame.t0001;
import sid.t0001.client.model.t0001Armatures;
import sid.t0001.particle.t0001Particles;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationManager.AnimationBuilder;
import yesman.epicfight.api.animation.AnimationManager.AnimationRegistryEvent;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationEvent.InTimeEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackAnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackPhaseProperty;
import yesman.epicfight.api.animation.property.MoveCoordFunctions;
import yesman.epicfight.api.animation.types.*;
import yesman.epicfight.api.utils.HitEntityList;
import yesman.epicfight.api.utils.TimePairList;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;


import java.util.Set;


import static net.minecraft.world.effect.MobEffects.LEVITATION;
import static sid.t0001.gameasset.ReusableEvents.FASTER_AFTERIMAGE;


//this fucking took ages, fuck coding, thank god, I switched to intellij otherwise I would have died on VS Code
// i should have practised math a bit more back then now i struggle
@SuppressWarnings("removal")
@Mod.EventBusSubscriber(modid = t0001.MODID, bus = Bus.MOD)
public class t0001Animations {

    public static AnimationAccessor<DodgeAnimation> ACCELERATE;
    public static AnimationAccessor<DodgeAnimation> ACCELERATE_BACK;
    public static AnimationAccessor<AttackAnimation> FANG_COUNTER;
    public static AnimationAccessor<AttackAnimation> TFU1;
    public static AnimationAccessor<AttackAnimation> TFU2;
    public static AnimationAccessor<AttackAnimation> TFU3;
    public static AnimationAccessor<AttackAnimation> TFU4;
    public static AnimationAccessor<AttackAnimation> TFU4_COPY;
    public static AnimationAccessor<AttackAnimation> TFU5;
    public static AnimationAccessor<AttackAnimation> TFU5_REMADE;
    public static AnimationAccessor<StaticAnimation> DARKNESS_IDLE;
    public static AnimationAccessor<LongHitAnimation> DARKNESS_DEATH;


    @SubscribeEvent
    public static void registerAnimations(AnimationRegistryEvent event) {
        event.newBuilder(t0001.MODID, t0001Animations::build);
    }

    // Tight, Tight, Tight, TIGHT
    public static void build(AnimationBuilder builder) {


        DARKNESS_IDLE = builder.nextAccessor("unnatural/darkness_idle",(accessor) -> new StaticAnimation(true, accessor, t0001Armatures.DARKNESSARMATURE));
        DARKNESS_DEATH = builder.nextAccessor("unnatural/darkness_death", (accessor) -> new LongHitAnimation(0.16F, accessor, t0001Armatures.DARKNESSARMATURE)
                .addProperty(AnimationProperty.StaticAnimationProperty.FIXED_HEAD_ROTATION, true));


        ACCELERATE = builder.nextAccessor("biped/skill/accelerate_dodge", (accessor) ->
                new DodgeAnimation(0.0F, accessor, 0.2F, 0.4F, Armatures.BIPED)
                        // Go GO gadget fps reduceR! ( fixed a bit of lag with faster-fading Afterimages -> "SFAST_AFTERIMAGE")
                        .addEvents(InTimeEvent.create(0.14F, FASTER_AFTERIMAGE, AnimationEvent.Side.CLIENT))
                        .addEvents(InTimeEvent.create(0.27F, FASTER_AFTERIMAGE, AnimationEvent.Side.CLIENT))
                        .addEvents(InTimeEvent.create(0.36F, FASTER_AFTERIMAGE, AnimationEvent.Side.CLIENT))
                        .addEvents(InTimeEvent.create(0.44F, FASTER_AFTERIMAGE, AnimationEvent.Side.CLIENT))
                        .addEvents(InTimeEvent.create(0.51F, FASTER_AFTERIMAGE, AnimationEvent.Side.CLIENT))
                        .addEvents(InTimeEvent.create(0.0F, Animations.ReusableSources.PLAY_SOUND, AnimationEvent.Side.CLIENT)
                                .params(t0001Sounds.SMOOTH_DODGE.get()))
        );


        ACCELERATE_BACK = builder.nextAccessor("biped/skill/accelerate_dodge_back", (accessor) ->
                new DodgeAnimation(0.1F, accessor, 0.4F, 0.8F, Armatures.BIPED)
                        .addEvents(InTimeEvent.create(0.0F, Animations.ReusableSources.PLAY_SOUND, AnimationEvent.Side.CLIENT)
                                .params(t0001Sounds.SLAM_SFX.get()))
                        .addEvents(InTimeEvent.create(0.0F, Animations.ReusableSources.FRACTURE_GROUND_SIMPLE, AnimationEvent.Side.SERVER)
                                .params(new Vec3f(0.0F, 0.0F, -0.01F),
                                        Armatures.BIPED.get().legL, 1.5D, .15F))
                        //.addEvents(ReusableEvents.MyFxHelpers.blockFX(new ResourceLocation("photon:ara"),0.0F))
                        .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, (anim, entity, elapsed, total, partialTicks) ->
                                1.45F)
        );

        // --- TFU animations --- almost every anim except tfu4 and tfu1 are going to get a remake/detailing
        TFU1 = builder.nextAccessor("biped/cutscened_attack/true_kung_fu_1/cs1", (accessor) -> new AttackAnimation(0.0F, accessor, Armatures.BIPED,

                        new AttackAnimation.Phase(0.1F, 0.35F, 0.4F, 0.6F, 1F, 0.7F,
                                Armatures.BIPED.get().handR, ColliderPreset.FIST)
                                .addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH.get())
                                .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.SHORT)
                                .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
                                .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.AIR_BURST)
                                .addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLUNT_HIT.get())
                                .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
                                .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(0.1F))
                                .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.adder(15)),

                        //left
                        new AttackAnimation.Phase(0.7F, 0.5F, 0.7F, 1F, 1.5F, 1.2F,
                                Armatures.BIPED.get().handL, ColliderPreset.FIST)
                                .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.NEUTRALIZE)
                                .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.AIR_BURST)
                                .addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLUNT_HIT_HARD.get())
                                .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
                                .addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.setter(100))
                                .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(0.5F))
                                .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.adder(100)))

                        .addProperty(AttackAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)

                        .addProperty(AttackAnimationProperty.CANCELABLE_MOVE, false)
                        .addProperty(ActionAnimationProperty.MOVE_TIME, TimePairList.create(0, 10))
                        .addProperty(AnimationProperty.StaticAnimationProperty.FIXED_HEAD_ROTATION, true)
                        .addState(EntityState.MOVEMENT_LOCKED, false)
                        .addState(EntityState.LOCKON_ROTATE, true)
                        .addState(EntityState.CAN_BASIC_ATTACK, false)
                        .addState(EntityState.CAN_SKILL_EXECUTION, false)

                        .addState(EntityState.TURNING_LOCKED, true));
//                .addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create(
//                        (entitypatch,params,animation)->{
//                            LivingEntity target = entitypatch.getTarget();
//                            double knockbackstr = 1.2;
//                            if(entitypatch.isLastAttackSuccess()){
//                                Vec3 knockbackDir = target.position()
//                                        .subtract(entitypatch.getOriginal().position())
//                                        .normalize();
//                                target.setDeltaMovement(
//                                        target.getDeltaMovement().add(
//                                                knockbackDir.x * knockbackstr,
//                                                0.1,
//                                                knockbackDir.z * knockbackstr
//                                        )
//                                );
//                                target.hasImpulse = true;
//                            }
//                        }, AnimationEvent.Side.BOTH
//
//                ))


        TFU2 = builder.nextAccessor("biped/cutscened_attack/true_kung_fu_1/cs2", (accessor) -> new AttackAnimation(0.0F, accessor, Armatures.BIPED,

                new AttackAnimation.Phase(0.01F, 0.2F, 0.25F, 0.9F, 11F, 1F,
                        Armatures.BIPED.get().legL, ColliderPreset.HEADBUTT_RAVAGER)
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
                        .addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH.get())
                        .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.UNBLOCKALBE, EpicFightDamageTypeTags.WEAPON_INNATE))
                        .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT)
                        .addProperty(AttackPhaseProperty.HIT_SOUND, t0001Sounds.HIT_BOOM.get())
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(0.9F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.setter(10))
                ,
                //right leg kick #1
                new AttackAnimation.Phase(0.02F, 0.5F, 1.12F, 1.5F, 11.2F, 1.6F,
                        Armatures.BIPED.get().legR, ColliderPreset.HEADBUTT_RAVAGER)
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.SHORT)
                        .addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH.get())
                        .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.BYPASS_DODGE, EpicFightDamageTypeTags.WEAPON_INNATE))
                        .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.AIR_BURST)
                        .addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLUNT_HIT.get())
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(0.9F))
                        .addProperty(AttackPhaseProperty.SOURCE_LOCATION_PROVIDER, LivingEntityPatch::getLastAttackPosition)
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.setter(4)))


                .addProperty(AttackAnimationProperty.CANCELABLE_MOVE, false)
                .addState(EntityState.MOVEMENT_LOCKED, false)
                .addProperty(ActionAnimationProperty.MOVE_TIME, TimePairList.create(0, 70))
                .addProperty(ActionAnimationProperty.DEST_LOCATION_PROVIDER, MoveCoordFunctions.ATTACK_TARGET_LOCATION)
                .addProperty(AnimationProperty.StaticAnimationProperty.FIXED_HEAD_ROTATION, true)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_TARGET_LOCATION_ROTATION)

                .addProperty(ActionAnimationProperty.MOVE_ON_LINK, false)
                .addState(EntityState.CAN_BASIC_ATTACK, false)
                .addState(EntityState.CAN_SKILL_EXECUTION, false)
                .addProperty(AttackAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE));

        TFU3 = builder.nextAccessor("biped/cutscened_attack/true_kung_fu_1/cs3", (accessor) -> new AttackAnimation(0.0F, accessor, Armatures.BIPED,

                new AttackAnimation.Phase(0.1F, 0.2F, 0.25F, 0.9F, 0.95F, 1F,
                        Armatures.BIPED.get().legL, ColliderPreset.FIST)
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.LONG)
                        .addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH.get())
                        .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.AIR_BURST)
                        .addProperty(AttackPhaseProperty.HIT_SOUND, t0001Sounds.HIT_BOOM.get())
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(0.4F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(20.0F)),

                new AttackAnimation.Phase(0.2F, 0.5F, 1.12F, 1.5F, 2.2F, 1.7F,
                        Armatures.BIPED.get().legR, ColliderPreset.FIST)
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.LONG)
                        .addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH.get())
                        .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.AIR_BURST)
                        .addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLUNT_HIT.get())
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(0.4F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(5.0F)))

                .addProperty(AttackAnimationProperty.CANCELABLE_MOVE, false)
                .addState(EntityState.MOVEMENT_LOCKED, true)
                .addProperty(ActionAnimationProperty.MOVE_TIME, TimePairList.create(0, 60))
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_TARGET_LOCATION_ROTATION)
                .addProperty(ActionAnimationProperty.DEST_COORD_YROT_PROVIDER, MoveCoordFunctions.LOOK_DEST)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0, 50))
                .addProperty(ActionAnimationProperty.MOVE_VERTICAL, true)
                .addProperty(ActionAnimationProperty.MOVE_ON_LINK, false)
                .addProperty(AttackAnimationProperty.REACH, 40F)
                .addProperty(AnimationProperty.StaticAnimationProperty.FIXED_HEAD_ROTATION, true)

                .addState(EntityState.LOCKON_ROTATE, true)
                .addState(EntityState.CAN_BASIC_ATTACK, false)
                .addState(EntityState.CAN_SKILL_EXECUTION, false)
                .addState(EntityState.TURNING_LOCKED, false)
                .addProperty(AttackAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE));

        // still using copy, tfu3 animation remake is almost done
        TFU4 = builder.nextAccessor("biped/cutscened_attack/true_kung_fu_1/cs4", (accessor) -> new AttackAnimation(0.0F, accessor, Armatures.BIPED,


                new AttackAnimation.Phase(0.02F, 0.22F, 0.21F, 0.4F, 12.1F, 0.42F,
                        Armatures.BIPED.get().legR, ColliderPreset.FIST)
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
                        .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.AIR_BURST)
                        .addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLUNT_HIT.get())
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(0.5F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(5.0F)),

                new AttackAnimation.Phase(0.1F, 0.0F, 0.32F, 0.7F, 12.2F, 0.5F,
                        Armatures.BIPED.get().legL, ColliderPreset.FIST)
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.SHORT)
                        .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.AIR_BURST)
                        .addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLUNT_HIT_HARD.get())
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(0.2F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(15.0F)))

                .addProperty(AttackAnimationProperty.CANCELABLE_MOVE, false)
                .addState(EntityState.MOVEMENT_LOCKED, true)
                .addProperty(ActionAnimationProperty.MOVE_TIME, TimePairList.create(0, 40))
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0, 50))
                .addProperty(ActionAnimationProperty.MOVE_ON_LINK, false)
                .addProperty(ActionAnimationProperty.MOVE_VERTICAL, true)
                .addProperty(AnimationProperty.StaticAnimationProperty.FIXED_HEAD_ROTATION, true)

                .addProperty(AttackAnimationProperty.FIXED_MOVE_DISTANCE,true)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_TARGET_LOCATION_ROTATION)
                .addProperty(ActionAnimationProperty.DEST_COORD_YROT_PROVIDER, MoveCoordFunctions.LOOK_DEST)
                .addState(EntityState.LOCKON_ROTATE, true)
                .addState(EntityState.CAN_BASIC_ATTACK, false)
                .addState(EntityState.CAN_SKILL_EXECUTION, false)
                .addEvents(AnimationEvent.InTimeEvent.create(0.27F, (entitypatch, animation, params) -> {

                    LivingEntity entity = entitypatch.getOriginal();
                    entity.level().addParticle(t0001Particles.FAST_AFTERIMAGE.get(), entity.getX(), entity.getY(), entity.getZ(), Double.longBitsToDouble(entity.getId()), 0, 0);

                }, AnimationEvent.Side.BOTH))
                .addEvents(InTimeEvent.create(0.10F, (entitypatch, animation, params) -> {

                    LivingEntity entity = entitypatch.getOriginal();
                    entity.level().addParticle(t0001Particles.FAST_AFTERIMAGE.get(), entity.getX(), entity.getY(), entity.getZ(), Double.longBitsToDouble(entity.getId()), 0, 0);

                }, AnimationEvent.Side.BOTH))
                .addProperty(AttackAnimationProperty.PLAY_SPEED_MODIFIER, (anim, entity, elapsed, total, partialTicks) -> 0.85F)
                .addState(EntityState.TURNING_LOCKED, false));

        // TFU5 original (kept for completeness but t0001InnateOne uses the new remade version(which also might get remade again))
        TFU5 = builder.nextAccessor("biped/cutscened_attack/true_kung_fu_1/cs5", (accessor) -> new AttackAnimation(0.0F, accessor, Armatures.BIPED,

                new AttackAnimation.Phase(0.1F, 0.01F, 0.09F, 2F, 3.2F, 1.5F,
                        Armatures.BIPED.get().handR, ColliderPreset.GOLEM_SMASHDOWN)
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
                        .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT)
                        .addProperty(AttackPhaseProperty.HIT_SOUND, t0001Sounds.HIT_BOOM.get())
                        .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.UNBLOCKALBE, EpicFightDamageTypeTags.WEAPON_INNATE, EpicFightDamageTypeTags.FINISHER))
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(6F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.setter(20.0F)))


                .addProperty(AttackAnimationProperty.CANCELABLE_MOVE, false)
                .addState(EntityState.MOVEMENT_LOCKED, true)
                .addProperty(ActionAnimationProperty.MOVE_TIME, TimePairList.create(0, 40))
                .addProperty(ActionAnimationProperty.MOVE_ON_LINK, false)
                .addProperty(AttackAnimationProperty.FIXED_MOVE_DISTANCE,true)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_TARGET_LOCATION_ROTATION)
                .addProperty(ActionAnimationProperty.DEST_COORD_YROT_PROVIDER, MoveCoordFunctions.LOOK_DEST)
                .addState(EntityState.LOCKON_ROTATE, true)
                .addState(EntityState.CAN_BASIC_ATTACK, false)
                .addState(EntityState.CAN_SKILL_EXECUTION, false)
                .addState(EntityState.TURNING_LOCKED, true)
                .addEvents(AnimationEvent.SimpleEvent.create((entitypatch, animation, params) -> {
                    if (entitypatch.isLastAttackSuccess()) {
                        LivingEntity entity = entitypatch.getTarget();
                        if (entity == null || !entity.isAlive()) {
                            return;
                        }
                        entity.setDeltaMovement(entity.getDeltaMovement().x, 0.75, entity.getDeltaMovement().z);
                        entity.hurtMarked = true;
                        entity.addEffect(new MobEffectInstance(LEVITATION, 55, 2, true, false, false));


                        LivingEntity player = entitypatch.getOriginal();
                        if (player == null || !player.isAlive()) {
                            return;
                        }
                        player.level().addParticle(EpicFightParticles.GROUND_SLAM.get(), player.getX(), player.getY(), player.getZ(),
                                Double.longBitsToDouble(player.getId()), -1.2, 1.69);

                    }

                }, AnimationEvent.Side.BOTH))

        );

        // TFU4_COPY - copy of TFU4 with slightly different timings/events
        TFU4_COPY = builder.nextAccessor("biped/cutscened_attack/true_kung_fu_1/cs4_copy", (accessor) -> new AttackAnimation(0.0F, accessor, Armatures.BIPED,
                // WE ARE SO GONNA DELYEET THIS SHET ONCE TFU3 anim gets fixed

                new AttackAnimation.Phase(0.02F, 0.22F, 0.21F, 0.4F, 12.1F, 0.42F,
                        Armatures.BIPED.get().legR, ColliderPreset.FIST)
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
                        .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.AIR_BURST)
                        .addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLUNT_HIT.get())
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(0.5F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(5.0F)),

                new AttackAnimation.Phase(0.1F, 0.0F, 0.32F, 0.7F, 12.2F, 0.5F,
                        Armatures.BIPED.get().legL, ColliderPreset.FIST)
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.LONG)
                        .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.AIR_BURST)
                        .addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLUNT_HIT_HARD.get())
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(0.2F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(5.0F)))

                .addProperty(AttackAnimationProperty.CANCELABLE_MOVE, false)
                .addState(EntityState.MOVEMENT_LOCKED, true)
                .addProperty(AttackAnimationProperty.FIXED_MOVE_DISTANCE,true)
                .addProperty(ActionAnimationProperty.MOVE_TIME, TimePairList.create(0, 40))
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0, 150))
                .addProperty(ActionAnimationProperty.MOVE_ON_LINK, false)
                .addProperty(ActionAnimationProperty.MOVE_VERTICAL, true)
                .addProperty(AnimationProperty.StaticAnimationProperty.FIXED_HEAD_ROTATION,true)
                .addProperty(AttackAnimationProperty.PLAY_SPEED_MODIFIER, (anim, entity, elapsed, total, partialTicks) -> 1.28F)
           //     .addState(EntityState.LOCKON_ROTATE, true)
                .addState(EntityState.CAN_BASIC_ATTACK, false)
                .addProperty(ActionAnimationProperty.DEST_LOCATION_PROVIDER, MoveCoordFunctions.ATTACK_TARGET_LOCATION)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_TARGET_LOCATION_ROTATION)

                .addEvents(InTimeEvent.create(0.65F, (entitypatch, animation, params) -> {
                        LivingEntity entity = entitypatch.getOriginal();
                        entity.level().addParticle(t0001Particles.FAST_AFTERIMAGE.get(), entity.getX(), entity.getY(), entity.getZ(), Double.longBitsToDouble(entity.getId()), 0, 0);
                }, AnimationEvent.Side.BOTH))
                .addEvents(AnimationEvent.InPeriodEvent.create(0.1F, 0.25F, (entitypatch, animation, params) -> {
                        LivingEntity entity = entitypatch.getOriginal();
                        entity.level().addParticle(t0001Particles.FAST_AFTERIMAGE.get(), entity.getX(), entity.getY(), entity.getZ(), Double.longBitsToDouble(entity.getId()), 0, 0);
                }, AnimationEvent.Side.BOTH))
                .addState(EntityState.CAN_SKILL_EXECUTION, false)
               // .addState(EntityState.TURNING_LOCKED, true)
                 );

        TFU5_REMADE = builder.nextAccessor("biped/cutscened_attack/true_kung_fu_1/cs5_remade", (accessor) -> new AttackAnimation(0.3F, accessor, Armatures.BIPED,

                new AttackAnimation.Phase(0.1F, 0.3F, 0.35F, 1F, 1.1F, 1.1F,
                        Armatures.BIPED.get().handR, ColliderPreset.WITHER_CHARGE)
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
                        .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLADE)
                        .addProperty(AttackPhaseProperty.HIT_SOUND, t0001Sounds.HIT_BOOM.get())
                        .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.UNBLOCKALBE))
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(6F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.setter(20.0F))
                        .addProperty(AttackPhaseProperty.HIT_PRIORITY, HitEntityList.Priority.TARGET))


                .addProperty(AttackAnimationProperty.CANCELABLE_MOVE, false)
                .addState(EntityState.MOVEMENT_LOCKED, true)
                .addProperty(ActionAnimationProperty.MOVE_TIME, TimePairList.create(0.1F,1.5F))
                .addProperty(ActionAnimationProperty.MOVE_ON_LINK, false)
                .addProperty(AttackAnimationProperty.FIXED_MOVE_DISTANCE,true)
                .addProperty(AttackAnimationProperty.MOVE_VERTICAL, true)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_TARGET_LOCATION_ROTATION)
                .addProperty(ActionAnimationProperty.DEST_COORD_YROT_PROVIDER, MoveCoordFunctions.LOOK_DEST)
                .addState(EntityState.LOCKON_ROTATE, true)
                .addState(EntityState.CAN_BASIC_ATTACK, false)
                .addState(EntityState.CAN_SKILL_EXECUTION, false)
                .addState(EntityState.PHASE_LEVEL, 0)
                .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
                .addProperty(AnimationProperty.StaticAnimationProperty.FIXED_HEAD_ROTATION,true)
                .addEvents(AnimationProperty.StaticAnimationProperty.TICK_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.RESIZE_BOUNDING_BOX, AnimationEvent.Side.BOTH).params(EntityDimensions.scalable(0.6F, 0.5F)))
                .addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.RESTORE_BOUNDING_BOX, AnimationEvent.Side.BOTH))
        )

        ;

        //I'll finish this later (I'm probably never gonna)IDK what this even does, taken from efm git
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