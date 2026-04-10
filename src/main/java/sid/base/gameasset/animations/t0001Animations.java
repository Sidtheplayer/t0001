package sid.base.gameasset.animations;


import com.lowdragmc.photon.client.fx.EntityEffectExecutor;
import com.lowdragmc.photon.client.fx.FXHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import sid.base.client.model.t0001Armatures;
import sid.base.gameasset.t0001Skills;
import sid.base.gameasset.t0001Sounds;
import sid.base.skill.t0001SkillDataKeys;
import sid.base.utils.ReusableAnimEvents;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationManager.AnimationBuilder;
import yesman.epicfight.api.animation.LivingMotions;
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

import yesman.epicfight.model.armature.HumanoidArmature;

import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;


import java.util.Set;


import static net.minecraft.world.effect.MobEffects.LEVITATION;
import static sid.base.gameasset.ReusableEvents.*;

//this fucking took ages, fuck coding, thank god, I switched to intellij otherwise I would have died on VS Code
// i should have practised math a bit more back then now i struggle


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

    //-DARKNESS_ENTITY ANIMS
    public static AnimationAccessor<StaticAnimation> DARKNESS_IDLE;
    public static AnimationAccessor<LongHitAnimation> DARKNESS_DEATH;

    public static AnimationAccessor<StaticAnimation> UNARMEDBLOCKFULL;
    public static AnimationAccessor<GuardAnimation> UNARMEDBLOCKFULL_HIT;

    public static AnimationAccessor<ComboAttackAnimation> SWEEP;
    public static AnimationAccessor<ComboAttackAnimation> I_SWEEP;
    public static AnimationAccessor<DashAttackAnimation> FW_KICK;
    public static AnimationAccessor<ComboAttackAnimation> UP_KICK_L;
    public static AnimationAccessor<ComboAttackAnimation> UP_KICK_R;




    // Tight, Tight, Tight, TIGHT
    public static void build(AnimationBuilder builder) {
        Armatures.ArmatureAccessor<HumanoidArmature> biped = Armatures.BIPED;


        UNARMEDBLOCKFULL = builder.nextAccessor("biped/skill/unarmedfullblock",(accessor)-> new StaticAnimation(
                0.2f,true,accessor,biped
        )

        );

        UNARMEDBLOCKFULL_HIT = builder.nextAccessor("biped/skill/unarmedfullblock_hit", (accessor) -> new GuardAnimation(
                        0.01f, accessor, biped
                )
                        .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, ReusableAnimEvents.ONE50PERCENT)
        );

        I_SWEEP = builder.nextAccessor("biped/combat/unarmed/i_sweep",ac-> new ComboAttackAnimation(-0.1f,
                0.02f,0.12f,0.4f,1.9f,null,biped.get().legL,ac,biped
        )
                        .addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 0.92f)
                        //.addProperty(AttackPhaseProperty.STUN_TYPE, ExtStunDamageTypeTags.BLOW_AWAY)
        );

        SWEEP = builder.nextAccessor("biped/combat/unarmed/sweep",ac-> new ComboAttackAnimation(0.1f,
                0.02f,
                        0.21f,
                        1.6f,
                        5.9f,
                        null,
                        biped.get().legL,
                        ac,
                        biped
        )
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN)
                        .addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 3.32f)
                        //.addProperty(AttackPhaseProperty.STUN_TYPE, ExtStunDamageTypeTags.BLOW_AWAY)
        );

        FW_KICK = builder.nextAccessor("biped/combat/unarmed/forwardkick", ac -> new DashAttackAnimation(0.1f,
                ReusableAnimEvents.getAnimTimeFromFrame(10),
                ReusableAnimEvents.getAnimTimeFromFrame(20),
                ReusableAnimEvents.getAnimTimeFromFrame(40),
                ReusableAnimEvents.getAnimTimeFromFrame(60),
                ColliderPreset.FIST,
                biped.get().legR,
                ac,
                biped
        )
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.LONG)
                        .addProperty(AttackPhaseProperty.HIT_SOUND, t0001Sounds.HARD_KICK.value())
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.adder(8f))
                .addProperty(AttackAnimationProperty.PLAY_SPEED_MODIFIER, (e,s,p,l,r)-> 1.25f)

        );

        UP_KICK_R = builder.nextAccessor("biped/combat/unarmed/kick_up_one", ac -> new ComboAttackAnimation(0.1f,
                ReusableAnimEvents.getAnimTimeFromFrame(5),
                ReusableAnimEvents.getAnimTimeFromFrame(10),
                ReusableAnimEvents.getAnimTimeFromFrame(30),
                ReusableAnimEvents.getAnimTimeFromFrame(40),
                ColliderPreset.FIST,
                biped.get().legR,
                ac,
                biped
        )
                .addProperty(AttackPhaseProperty.STUN_TYPE,StunType.SHORT)
                .addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLUNT_HIT.get())
                .addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.82f)

        );

        UP_KICK_L = builder.nextAccessor("biped/combat/unarmed/kick_up_two", ac -> new ComboAttackAnimation(0.1f,
                ReusableAnimEvents.getAnimTimeFromFrame(5),
                ReusableAnimEvents.getAnimTimeFromFrame(10),
                ReusableAnimEvents.getAnimTimeFromFrame(30),
                ReusableAnimEvents.getAnimTimeFromFrame(40),
                ColliderPreset.FIST,
                biped.get().legL,
                ac,
                biped
        )
                .addProperty(AttackPhaseProperty.STUN_TYPE,StunType.SHORT)
                .addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLUNT_HIT.get())
                .addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.82f)
        );




        // will not work normally for other entities because of custom armature
        DARKNESS_IDLE = builder.nextAccessor("unnatural/darkness_idle", (accessor) -> new StaticAnimation(true, accessor, t0001Armatures.DARKNESSARMATURE));
        DARKNESS_DEATH = builder.nextAccessor("unnatural/darkness_death", (accessor) -> new LongHitAnimation(0.16F, accessor, t0001Armatures.DARKNESSARMATURE));

        ACCELERATE = builder.nextAccessor("biped/skill/accelerate_dodge", (accessor) ->
                new DodgeAnimation(0.1F, accessor, 0.2F, 0.4F, biped)
                        // Go GO gadget fps reduceR!
                        .addEvents(InTimeEvent.create(0.14F, FASTER_AFTERIMAGE, AnimationEvent.Side.CLIENT))
                        .addEvents(InTimeEvent.create(0.27F, FASTER_AFTERIMAGE, AnimationEvent.Side.CLIENT))
                        .addEvents(InTimeEvent.create(0.36F, FASTER_AFTERIMAGE, AnimationEvent.Side.CLIENT))
                        .addEvents(InTimeEvent.create(0.44F, FASTER_AFTERIMAGE, AnimationEvent.Side.CLIENT))
                        .addEvents(InTimeEvent.create(0.51F, FASTER_AFTERIMAGE, AnimationEvent.Side.CLIENT))
                        .addEvents(AnimationEvent.InTimeEvent.create( 0.0f,Animations.ReusableSources.PLAY_SOUND, AnimationEvent.Side.CLIENT)
                                .params(t0001Sounds.SMOOTH_DODGE.get()))
        );


        ACCELERATE_BACK = builder.nextAccessor("biped/skill/accelerate_dodge_back", (accessor) ->
                new DodgeAnimation(0.1F, accessor, 0.4F, 0.8F, biped)
                        .addEvents(InTimeEvent.create(0.0F, Animations.ReusableSources.PLAY_SOUND, AnimationEvent.Side.CLIENT)
                                .params(t0001Sounds.SLAM_SFX.get()))
                        .addEvents(InTimeEvent.create(0.0F, Animations.ReusableSources.FRACTURE_GROUND_SIMPLE, AnimationEvent.Side.SERVER)
                                .params(new Vec3f(0.0F, 0.0F, -0.01F),
                                       biped.get().legL, 1.5D, .15F))
                        //.addEvents(ReusableEvents.MyFxHelpers.blockFX(new ResourceLocation("photon:ara"),0.0F))
                        .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, (anim, entity, elapsed, total, partialTicks) ->
                                1.45F)
        );

        // Most of the anims here will have references to COMBAT GODS 1 and 2 by Jhanzou
        FANG_COUNTER = builder.nextAccessor("biped/skill/jun_take_43", (accessor) -> new AttackAnimation(0.0F, accessor, biped,

                new AttackAnimation.Phase(0.01F, 0.2F, 0.01F, 0.3F, 1.0F, 1.2F,
                        biped.get().toolR, ColliderPreset.FIST)
                        .addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_ROD.get())
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
                        .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.AIR_BURST)
                        .addProperty(AttackPhaseProperty.HIT_SOUND, t0001Sounds.HIT_BOOM.get())
                        .addProperty(AttackPhaseProperty.HIT_PRIORITY, HitEntityList.Priority.TARGET)
                        .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.IS_MELEE, EpicFightDamageTypeTags.UNBLOCKALBE))
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(0.2F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(9.0F))
                ,

                new AttackAnimation.Phase(1.45F, 0.55F, 1.60F, 2.1F, 1.0F, 2.0F,
                        biped.get().legL, ColliderPreset.FIST)
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(2.0F))
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.NEUTRALIZE)
                        .addProperty(AttackPhaseProperty.SWING_SOUND, SoundEvents.GRASS_STEP)
                        .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.AIR_BURST)
                        .addProperty(AttackPhaseProperty.HIT_PRIORITY, HitEntityList.Priority.TARGET)
                        .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.FINISHER, EpicFightDamageTypeTags.UNBLOCKALBE))
                        .addProperty(AttackPhaseProperty.HIT_SOUND, t0001Sounds.HIT_BOOM.get())
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1)))

                /* Elbow follow-up disabled due to anim problem in blender :C SKibiddi Toilet is bad for your Health
                new AttackAnimation.Phase(0.0F, 2.4F, 2.90F, 3.0F, 3.0F, 5.0F,
                        biped.get().elbowL, ColliderPreset.FIST)
                        .addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN)
                        .addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.2F))
                        .addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, EpicFightSounds.NEUTRALIZE_MOBS.get())
                        .addProperty(AnimationProperty.AttackPhaseProperty.HIT_PRIORITY, HitEntityList.Priority.TARGET)*/

                .addProperty(AttackAnimationProperty.CANCELABLE_MOVE, false)
                .addState(EntityState.MOVEMENT_LOCKED, true)
                .addState(EntityState.LOOK_TARGET, false)
                .addState(EntityState.SKILL_EXECUTABLE, false)
                .addState(EntityState.TURNING_LOCKED, true)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0, 45))
                .addProperty(AttackAnimationProperty.MOVE_VERTICAL, true)
                .addProperty(AttackAnimationProperty.PLAY_SPEED_MODIFIER, (anim, entity, elapsed, total, partialTicks) ->
                        1.35F)
                .addEvents(InTimeEvent.create(0.027F, (entitypatch, animation, params) -> {
                    try {
                        new EntityEffectExecutor(FXHelper.getFX(ResourceLocation.parse("photon:ara")), entitypatch.getTarget().level(), entitypatch.getOriginal(), EntityEffectExecutor.AutoRotate.NONE).start();
                    } catch (Exception ignored) {}

                }, AnimationEvent.Side.CLIENT))
                .addEvents(
                        InTimeEvent.create(0.35F, (entitypatch, animation, params) -> {
                            if (!entitypatch.isLastAttackSuccess()) {
                                entitypatch.playAnimationSynchronized(entitypatch.getAnimator().getLivingAnimation(LivingMotions.IDLE, Animations.BIPED_IDLE), 0.6F);
                            }
                        }, AnimationEvent.Side.BOTH) // to auto-idle if 1st phase had no hit
                )
                .addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create((e, s, p) ->{
                    if(e instanceof ServerPlayerPatch serverPlayerPatch){
                        if(serverPlayerPatch.getSkill(SkillSlots.IDENTITY).hasSkill(t0001Skills.FANG_COUNTER.get())){
                            serverPlayerPatch.getSkill(SkillSlots.IDENTITY).getDataManager().setDataSync(t0001SkillDataKeys.ULTIMATE_MOVE_MODE_SET,0);
                        }

                    }
                }, AnimationEvent.Side.SERVER))

        );


        TFU1 = builder.nextAccessor("biped/cutscened_attack/true_kung_fu_1/cs1", (accessor) -> new AttackAnimation(0.0F, accessor, biped,

                new AttackAnimation.Phase(0.1F, 0.35F, 0.4F, 0.6F, 200.2F, 0.7F,
                        biped.get().handR, ColliderPreset.FIST)
                        .addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH.get())
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
                        .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.AIR_BURST)
                        .addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLUNT_HIT.get())
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(0.1F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.adder(5.0F)),

                //left
                new AttackAnimation.Phase(0.71F, 0.5F, 0.7F, 1F, 100F, 1.2F,
                        biped.get().handL, ColliderPreset.FIST)
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
                        .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT)
                        .addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLUNT_HIT_HARD.get())
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
                        .addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.setter(100))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(0.5F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.setter(100.0F)))

                .addProperty(AttackAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)

                .addProperty(AttackAnimationProperty.CANCELABLE_MOVE, false)
                .addProperty(AttackAnimationProperty.MOVE_TIME, TimePairList.create(0.0F, 1.5F))
                .addProperty(AttackAnimationProperty.FIXED_HEAD_ROTATION, true)
                .addState(EntityState.MOVEMENT_LOCKED, false)
                .addState(EntityState.LOOK_TARGET, true)
                .addState(EntityState.SKILL_EXECUTABLE, false)
                .addState(EntityState.TURNING_LOCKED, true)

                .addEvents(AttackAnimationProperty.ON_BEGIN_EVENTS,
                        AnimationEvent.SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, AnimationEvent.Side.CLIENT))
                .addEvents(AttackAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, AnimationEvent.Side.CLIENT))
        );


        TFU2 = builder.nextAccessor("biped/cutscened_attack/true_kung_fu_1/cs2", (accessor) -> new AttackAnimation(0.0F, accessor, biped,

                new AttackAnimation.Phase(0.01F, 0.2F, 0.25F, 0.9F, 69F, 1F,
                        biped.get().legL, ColliderPreset.HEADBUTT_RAVAGER)
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
                        .addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH.get())
                        .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.UNBLOCKALBE))
                        .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT)
                        .addProperty(AttackPhaseProperty.HIT_SOUND, t0001Sounds.HIT_BOOM.get())
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(0.9F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.setter(10))
                ,
                //right leg kick #1
                new AttackAnimation.Phase(0.02F, 0.5F, 1.12F, 1.5F, 420.2F, 1.6F,
                        biped.get().legR, ColliderPreset.HEADBUTT_RAVAGER)
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.SHORT)
                        .addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH.get())
                        .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.BYPASS_DODGE))
                        .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT)
                        .addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLUNT_HIT.get())
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(0.9F))
                        .addProperty(AttackPhaseProperty.SOURCE_LOCATION_PROVIDER, LivingEntityPatch::getLastAttackPosition)
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.setter(4)))


                .addProperty(AttackAnimationProperty.CANCELABLE_MOVE, false)
                .addState(EntityState.MOVEMENT_LOCKED, true)
                .addProperty(AttackAnimationProperty.FIXED_MOVE_DISTANCE, false)
                //  .addProperty(ActionAnimationProperty.MOVE_TIME, TimePairList.create(0, 70))
                .addProperty(AttackAnimationProperty.FIXED_HEAD_ROTATION, true)
                .addProperty(AttackAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_TARGET_LOCATION_ROTATION)
                .addProperty(AttackAnimationProperty.DEST_COORD_YROT_PROVIDER, MoveCoordFunctions.LOOK_DEST)
                .addProperty(ActionAnimationProperty.MOVE_ON_LINK, false)
                .addState(EntityState.SKILL_EXECUTABLE, false)
                .addProperty(AttackAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)

                .addEvents(AttackAnimationProperty.ON_BEGIN_EVENTS,
                        AnimationEvent.SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, AnimationEvent.Side.CLIENT))
                .addEvents(AttackAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, AnimationEvent.Side.CLIENT))

        );

        TFU3 = builder.nextAccessor("biped/cutscened_attack/true_kung_fu_1/cs3", (accessor) -> new AttackAnimation(0.0F, accessor, biped,

                new AttackAnimation.Phase(0.1F, 0.2F, 0.25F, 0.9F, 95.95F, 1F,
                        biped.get().legL, ColliderPreset.FIST)
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.LONG)
                        .addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH.get())
                        .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.AIR_BURST)
                        .addProperty(AttackPhaseProperty.HIT_SOUND, t0001Sounds.HIT_BOOM.get())
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(0.4F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(20.0F)),

                new AttackAnimation.Phase(0.2F, 0.5F, 1.12F, 1.5F, 212.2F, 1.7F,
                        biped.get().legR, ColliderPreset.FIST)
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
                .addProperty(AttackAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0, 50))
                .addProperty(AttackAnimationProperty.MOVE_VERTICAL, true)
                .addProperty(ActionAnimationProperty.MOVE_ON_LINK, false)
                .addProperty(AttackAnimationProperty.REACH, 40F)
                .addProperty(AttackAnimationProperty.FIXED_HEAD_ROTATION, true)

                .addState(EntityState.LOOK_TARGET, true)
                .addState(EntityState.SKILL_EXECUTABLE, false)
                .addState(EntityState.TURNING_LOCKED, false)
                .addProperty(AttackAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE));

        // still using copy, tfu3 animation remake is almost done
        TFU4 = builder.nextAccessor("biped/cutscened_attack/true_kung_fu_1/cs4", (accessor) -> new AttackAnimation(0.0F, accessor, biped,

                // CUSTOM COLLIDERS TO BE DONE LATER
                new AttackAnimation.Phase(0.02F, 0.22F, 0.21F, 0.4F, 142.1F, 0.42F,
                        biped.get().legR, ColliderPreset.FIST)
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
                        .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT)
                        .addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLUNT_HIT.get())
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(0.5F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(5.0F)),

                new AttackAnimation.Phase(0.1F, 0.0F, 0.32F, 0.7F, 124.2F, 0.5F,
                        biped.get().legL, ColliderPreset.FIST)
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.SHORT)
                        .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT)
                        .addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLUNT_HIT_HARD.get())
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(0.2F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(15.0F)))

                .addProperty(AttackAnimationProperty.CANCELABLE_MOVE, false)
                .addState(EntityState.MOVEMENT_LOCKED, true)
                .addProperty(ActionAnimationProperty.MOVE_TIME, TimePairList.create(0, 40))
                .addProperty(AttackAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0, 50))
                .addProperty(ActionAnimationProperty.MOVE_ON_LINK, false)
                .addProperty(AttackAnimationProperty.MOVE_VERTICAL, true)
                .addProperty(AttackAnimationProperty.FIXED_HEAD_ROTATION, true)

                .addProperty(AttackAnimationProperty.FIXED_MOVE_DISTANCE, true)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_TARGET_LOCATION_ROTATION)
                .addProperty(ActionAnimationProperty.DEST_COORD_YROT_PROVIDER, MoveCoordFunctions.LOOK_DEST)
                .addState(EntityState.LOOK_TARGET, true)
                .addProperty(AttackAnimationProperty.PLAY_SPEED_MODIFIER, (anim, entity, elapsed, total, partialTicks) -> 0.85F)
                .addState(EntityState.TURNING_LOCKED, false)

                .addEvents(AttackAnimationProperty.ON_BEGIN_EVENTS,
                        AnimationEvent.SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, AnimationEvent.Side.CLIENT))
                .addEvents(AttackAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, AnimationEvent.Side.CLIENT))


        );

        // TFU5 original (kept for completeness but t0001InnateOne uses the new remade version(which also might get remade again))
        TFU5 = builder.nextAccessor("biped/cutscened_attack/true_kung_fu_1/cs5", (accessor) -> new AttackAnimation(0.0F, accessor, biped,

                new AttackAnimation.Phase(0.1F, 0.01F, 0.09F, 2F, 39.2F, 1.5F,
                        biped.get().handR, ColliderPreset.GOLEM_SMASHDOWN)
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
                .addProperty(AttackAnimationProperty.FIXED_MOVE_DISTANCE, true)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_TARGET_LOCATION_ROTATION)
                .addProperty(ActionAnimationProperty.DEST_COORD_YROT_PROVIDER, MoveCoordFunctions.LOOK_DEST)
                .addState(EntityState.LOOK_TARGET, true)
                .addState(EntityState.TURNING_LOCKED, true)
                .addEvents(AnimationEvent.SimpleEvent.create((entitypatch, animation, params) -> {
                    if (entitypatch.isLastAttackSuccess()) {
                        LivingEntity entity = entitypatch.getTarget();
                        if (entity == null || !entity.isAlive()) {
                            return;
                        }
                        entity.setDeltaMovement(entity.getDeltaMovement().x, 0.75, entity.getDeltaMovement().z);
                        entity.move(MoverType.PLAYER,new Vec3(entity.getX(),entity.getY() + 6,entity.getZ()));
                        entity.hurtMarked = true;
                        entity.addEffect(new MobEffectInstance(LEVITATION, 55, 20, true, false, false));


                        LivingEntity player = entitypatch.getOriginal();
                        if (!player.isAlive()) {
                            return;
                        }
                        player.level().addParticle(EpicFightParticles.GROUND_SLAM.get(), player.getX(), player.getY(), player.getZ(),
                                Double.longBitsToDouble(player.getId()), -1.2, 1.69);

                    }

                }, AnimationEvent.Side.BOTH))

        );

        // TFU4_COPY - copy of TFU4 with slightly different timings/events
        TFU4_COPY = builder.nextAccessor("biped/cutscened_attack/true_kung_fu_1/cs4_copy", (accessor) -> new AttackAnimation(0.0F, accessor, biped,
                // WE ARE SO GONNA DELYEET THIS SHET ONCE TFU3 anim gets fixed

                new AttackAnimation.Phase(0.02F, 0.22F, 0.21F, 0.4F, 132.1F, 0.42F,
                        biped.get().legR, ColliderPreset.FIST)
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
                        .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT)
                        .addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLUNT_HIT.get())
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(0.5F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(5.0F)),

                new AttackAnimation.Phase(0.1F, 0.0F, 0.32F, 0.7F, 122.2F, 0.5F,
                        biped.get().legL, ColliderPreset.FIST)
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.LONG)
                        .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT)
                        .addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLUNT_HIT_HARD.get())
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(0.2F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(5.0F)))

                .addProperty(AttackAnimationProperty.CANCELABLE_MOVE, false)
                .addState(EntityState.MOVEMENT_LOCKED, true)
                .addProperty(AttackAnimationProperty.FIXED_MOVE_DISTANCE, true)
                .addProperty(ActionAnimationProperty.MOVE_TIME, TimePairList.create(0, 40))
                .addProperty(AttackAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0, 150))
                .addProperty(ActionAnimationProperty.MOVE_ON_LINK, false)
                .addProperty(AttackAnimationProperty.MOVE_VERTICAL, true)
                .addProperty(AttackAnimationProperty.FIXED_HEAD_ROTATION, true)
                .addProperty(AttackAnimationProperty.PLAY_SPEED_MODIFIER, (anim, entity, elapsed, total, partialTicks) -> 1.28F)
                .addState(EntityState.LOOK_TARGET, true)
                .addProperty(ActionAnimationProperty.DEST_LOCATION_PROVIDER, MoveCoordFunctions.ATTACK_TARGET_LOCATION)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_TARGET_LOCATION_ROTATION)
                .addState(EntityState.SKILL_EXECUTABLE, false)
                .addState(EntityState.TURNING_LOCKED, true)

                .addEvents(AttackAnimationProperty.ON_BEGIN_EVENTS,
                        AnimationEvent.SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, AnimationEvent.Side.CLIENT))
                .addEvents(AttackAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, AnimationEvent.Side.CLIENT))

        );

        TFU5_REMADE = builder.nextAccessor("biped/cutscened_attack/true_kung_fu_1/cs5_remade", (accessor) -> new AttackAnimation(0.3F, accessor, biped,

                new AttackAnimation.Phase(0.1F, 0.3F, 0.35F, 1F, 123.1F, 1.1F,
                        biped.get().handR, ColliderPreset.WITHER_CHARGE)
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
                        .addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLADE)
                        .addProperty(AttackPhaseProperty.HIT_SOUND, t0001Sounds.HIT_BOOM.get())
                        .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.UNBLOCKALBE, EpicFightDamageTypeTags.WEAPON_INNATE))
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(6F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.setter(20.0F))
                        .addProperty(AttackPhaseProperty.HIT_PRIORITY, HitEntityList.Priority.TARGET))


                .addProperty(AttackAnimationProperty.CANCELABLE_MOVE, false)
                .addState(EntityState.MOVEMENT_LOCKED, true)
                .addProperty(AttackAnimationProperty.FIXED_MOVE_DISTANCE, false)
                .addProperty(AttackAnimationProperty.MOVE_VERTICAL, true)
                .addProperty(AttackAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_TARGET_LOCATION_ROTATION)
                .addState(EntityState.LOOK_TARGET, true)
                .addState(EntityState.SKILL_EXECUTABLE, false)
                .addState(EntityState.PHASE_LEVEL, 0)
                .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
                .addProperty(AttackAnimationProperty.FIXED_HEAD_ROTATION, true)
                .addEvents(AnimationProperty.StaticAnimationProperty.TICK_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.RESIZE_BOUNDING_BOX, AnimationEvent.Side.BOTH).params(EntityDimensions.scalable(0.6F, 0.5F)))
                .addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.RESTORE_BOUNDING_BOX, AnimationEvent.Side.BOTH))

                .addEvents(AttackAnimationProperty.ON_BEGIN_EVENTS,
                        AnimationEvent.SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, AnimationEvent.Side.CLIENT))
                .addEvents(AttackAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, AnimationEvent.Side.CLIENT))

        );


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
