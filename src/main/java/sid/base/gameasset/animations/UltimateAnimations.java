package sid.base.gameasset.animations;

import com.lowdragmc.photon.client.fx.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import sid.base.client.events.CameraAnimator;
import sid.base.gameasset.ReusableEvents;
import sid.base.gameasset.animations.collider.CGSColliderPresets;
import sid.base.gameasset.animations.types.TitleCardAttackAnimation;
import sid.base.gameasset.t0001Sounds;
import sid.base.main.t0001;
import sid.base.particle.t0001Particles;
import sid.base.utils.ReusableAnimEvents;
import sid.base.utils.VideoRendererUtil;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.property.MoveCoordFunctions;
import yesman.epicfight.api.animation.types.*;
import yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.HitEntityList;
import yesman.epicfight.api.utils.TimePairList;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.ExtraDamageInstance;
import yesman.epicfight.world.damagesource.StunType;

import java.util.*;

import static sid.base.gameasset.ReusableEvents.JointTrack.getJointWithTranslation;
import static sid.base.utils.ReusableAnimEvents.*;


public class UltimateAnimations {

    public static AnimationManager.AnimationAccessor<TitleCardAttackAnimation> ONE_INCH_COUNTER;
    public static AnimationManager.AnimationAccessor<LongHitAnimation> ONE_INCH_COUNTER_HIT;
    public static AnimationManager.AnimationAccessor<StaticAnimation> ONE_INCH_COUNTER_BAIT;
    public static AnimationManager.AnimationAccessor<StaticAnimation> ONE_INCH_COUNTER_BAIT_FAIL;

    public static AnimationManager.AnimationAccessor<TitleCardAttackAnimation> FSK;
    public static AnimationManager.AnimationAccessor<TitleCardAttackAnimation> NO_MORE_GAMES;

    public static AnimationManager.AnimationAccessor<LongHitAnimation> NO_MORE_GAMES_HIT;

    public static AnimationManager.AnimationAccessor<StaticAnimation> TOOEASYTES2;

    private static final ExtraDamageInstance.ExtraDamage TARGET_MAX_HEALTH_NON_LETHAL = new ExtraDamageInstance.ExtraDamage(
            (attacker, itemstack, target, baseDamage, params) -> {

                float damage = params[0] + target.getMaxHealth() * params[1];

                return Math.max(0.0F, Math.min(damage, target.getHealth() - 1.0F));
            },
            (levelReader, itemstack, tooltips, baseDamage, params) -> {
            }
    );


    public static void build(AnimationManager.AnimationBuilder builder) {
        Armatures.ArmatureAccessor<HumanoidArmature> biped = Armatures.BIPED;


        TOOEASYTES2 = builder.nextAccessor("biped/dgs/bladetest", ac ->
                new StaticAnimation(0.0f, true, ac, biped)
                        .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
        );


        ONE_INCH_COUNTER = builder.nextAccessor("biped/skill/one_inch_counter/one_inch_counter", (accessor) -> new TitleCardAttackAnimation(0.01F, accessor, biped,
                //prev predlay - 5.468
                new AttackAnimation.Phase(0.01F, 0.01F, 5.68F, 5.9F, Float.MAX_VALUE, 6.91F,
                        biped.get().handR, CGSColliderPresets.ULTIMATE_KNOCKBACK_AREABOX)
                        .addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, EpicFightSounds.LASER_BLAST.get())
                        .addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(0))
                        .addProperty(AnimationProperty.AttackPhaseProperty.HIT_PRIORITY, HitEntityList.Priority.TARGET)
                        .addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(DamageTypeTags.BYPASSES_INVULNERABILITY, EpicFightDamageTypeTags.EXECUTION))
                        .addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.LONG)
                        .addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.adder(5F))
                        .addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.adder(500F)))


                //bind entity to target, synched_target variable needs to be manually set, for ref look inside FangCounterSkill
                .addProperty(AnimationProperty.AttackAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.RAW_COORD)
                .addProperty(AnimationProperty.AttackAnimationProperty.DEST_LOCATION_PROVIDER, MoveCoordFunctions.SYNCHED_TARGET_ENTITY_LOCATION_VARIABLE)
                .addProperty(AnimationProperty.AttackAnimationProperty.ENTITY_YROT_PROVIDER, MoveCoordFunctions.LOOK_DEST)
                .addEvents(
                        renderImpactFrame(288, "impact_frames/one_inch/impact_", 0),
                        //make the method throw an exception to stop video
                        renderImpactFrame(292, "stop video", 2),

                        renderImpactFrame(292, "impact_frames/one_inch/impact_", 1),

                        renderImpactFrame(296, "stop video", 2),

                        renderImpactFrame(296, "impact_frames/one_inch/impact_", 2),

                        renderImpactFrame(300, "stop video", 2)
                )


                .addEvents(AnimationProperty.AttackAnimationProperty.ON_END_EVENTS,

                        AnimationEvent.SimpleEvent.create((e, s, p) -> {
                            // Minecraft.getInstance().options.fov().set(70);
                        }, AnimationEvent.Side.LOCAL_CLIENT),

                        AnimationEvent.SimpleEvent.create(((entitypatch, animation, params) ->
                                entitypatch.getOriginal().setInvulnerable(false)), AnimationEvent.Side.SERVER)

                )

                .addEvents(


                        AnimationEvent.InTimeEvent.create(5.0F, (entitypatch, animation, params) -> {
                            if (entitypatch != null) {

                                Vec3 vecPos = getJointWithTranslation(Minecraft.getInstance().player, entitypatch.getOriginal(), new Vec3f(1.5, 0, 0), Armatures.BIPED.get().rootJoint);
                                assert vecPos != null;
                                Particle particle = Minecraft.getInstance().particleEngine.createParticle(
                                        EpicFightParticles.AIR_BURST.get(),
                                        vecPos.x,
                                        vecPos.y,
                                        vecPos.z,
                                        0,
                                        0,
                                        0
                                );
                                if (particle != null) {
                                    particle.scale(0.92f);
                                    particle.setLifetime(18);
                                    particle.scale(3.5F);
                                }


                            }
                        }, AnimationEvent.Side.CLIENT),

                        AnimationEvent.InTimeEvent.create(0.025f, (e, s, p) ->
                                {
                                    e.playSound(t0001Sounds.TESTONE_INCH, 250f, 0.95f, 1.0f);
                                    System.out.println("[TEST] Animation event triggered!");

                                    CameraAnimator.getInstance().play("counter", false, true);
                                }
                                , AnimationEvent.Side.LOCAL_CLIENT),

                        spawnDirectionalBlockEffect("photon:angled2linedsmoke", 5.10f, 0, 0f, 0, 0, 1, 0,
                                0, 90, 0
                        )

                )


                .addProperty(AnimationProperty.AttackAnimationProperty.PLAY_SPEED_MODIFIER, (l, s, p, k, f) -> 0.832333f) //self-explanatory
        );


        ONE_INCH_COUNTER_HIT = builder.nextAccessor("biped/skill/one_inch_counter/one_inch_counter_hit", (accessor) -> new LongHitAnimation(0.01F, accessor, biped)
                .addProperty(ActionAnimationProperty.CANCELABLE_MOVE, false)
                .addEvents(
                        AnimationEvent.InTimeEvent.create(0.05f, (e, s, p) ->
                                {

                                    CameraAnimator.getInstance().playWithOption("counter", false, true);

                                }
                                , AnimationEvent.Side.LOCAL_CLIENT),


                        AnimationEvent.InTimeEvent.create(5.9F, (entitypatch, animation, params) -> {

                            Vec3 vecpos = getJointWithTranslation(Minecraft.getInstance().player, entitypatch.getOriginal(), new Vec3f(1.5, 0, 0), Armatures.BIPED.get().rootJoint);
                            {
                                assert vecpos != null;
                                Particle particle = Minecraft.getInstance().particleEngine.createParticle(
                                        EpicFightParticles.AIR_BURST.get(),
                                        vecpos.x,
                                        vecpos.y,
                                        vecpos.z,
                                        0,
                                        0,
                                        0);
                                if (particle != null) {
                                    particle.scale(0.92f);
                                    particle.setLifetime(9);
                                    particle.scale(3.5F);
                                    particle.setColor(214, 181, 136);
                                }

                            }
                        }, AnimationEvent.Side.CLIENT),

                        AnimationEvent.InTimeEvent.create(6.0f, (e, s, p) -> {
                            e.getOriginal().removeAllEffects();
                        }, AnimationEvent.Side.SERVER)


                )
                .addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create((e, s, p) ->
                                e.playSound(t0001Sounds.TESTONE_INCH, 150f, 1.2f, 1.25f)
                        , AnimationEvent.Side.LOCAL_CLIENT))


                //kill entity and credit the killer
                .addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS,
                        AnimationEvent.SimpleEvent.create(ReusableEvents.KillandCredit, AnimationEvent.Side.SERVER))

                .addProperty(AnimationProperty.AttackAnimationProperty.PLAY_SPEED_MODIFIER, (s, f, t, k, r) -> 0.833333f) //self explanatory
                .addProperty(ActionAnimationProperty.MOVE_VERTICAL, true)
                .addProperty(ActionAnimationProperty.SYNC_CAMERA, true)
                .addProperty(ActionAnimationProperty.CANCELABLE_MOVE, false)
                .addProperty(ActionAnimationProperty.IS_DEATH_ANIMATION, true)
                .addProperty(ActionAnimationProperty.FIXED_HEAD_ROTATION, true)
        );

        ONE_INCH_COUNTER_BAIT = builder.nextAccessor("biped/skill/one_inch_counter/one_inch_bait", (accessor) ->
                new StaticAnimation(false, accessor, biped)
                        .addProperty(ActionAnimationProperty.CANCELABLE_MOVE, false)
                        .addProperty(ActionAnimationProperty.STOP_MOVEMENT, true)
                        .addState(EntityState.MOVEMENT_LOCKED, true)
                        .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
                        //the attackresult might be faulty?
                        .addState(EntityState.ATTACK_RESULT, damageSource -> damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) ? AttackResult.ResultType.SUCCESS : AttackResult.ResultType.BLOCKED)
                        .addEvents(ActionAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create((e, s, p) -> ReusableAnimEvents.SpawnRootJointTrackFX(e, "photon:menacingcounter", true), AnimationEvent.Side.CLIENT))
                        .addEvents(ActionAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create(
                                (e, s, p) -> {
                                    if (e.isLogicalClient()) {
                                        FXRuntime toDestroy = fxRuntimeTable.remove(e.getId(), "photon:menacingcounter");
                                        if (toDestroy != null) {
                                            toDestroy.destroy(true);
                                        }
                                    }
                                }, AnimationEvent.Side.CLIENT
                        ), AnimationEvent.SimpleEvent.create((e, s, p) -> {
                            if (e.getOriginal().getLastDamageSource() == null) {
                                e.reserveAnimation(UltimateAnimations.ONE_INCH_COUNTER_BAIT_FAIL);
                            }
                        }, AnimationEvent.Side.SERVER))
        );

        ONE_INCH_COUNTER_BAIT_FAIL = builder.nextAccessor("biped/skill/one_inch_counter/one_inch_bait_fail", (accessor) -> new StaticAnimation(false, accessor, biped)
                .addProperty(ActionAnimationProperty.CANCELABLE_MOVE, false)
        );


        FSK = builder.nextAccessor("biped/cutscened_attack/five_seasons/goofydevastator", (accessor) ->
                new TitleCardAttackAnimation(0.1f, 0.2f, 60.5f, 75.5f, 250.5f, InteractionHand.MAIN_HAND, null, biped.get().handR, accessor, biped)
                        .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
                        .addProperty(AnimationProperty.AttackAnimationProperty.FIXED_MOVE_DISTANCE, true)
                        .addProperty(AnimationProperty.AttackAnimationProperty.MOVE_VERTICAL, true)
                        .addProperty(AnimationProperty.AttackAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0f, 150f))
        );

        Vec3 var1vec = new Vec3(0, -0.25, 0);
        Vec3 var2vec = new Vec3(0.1, -0.45, 0);


        //todo: complete vfx(85%) and multiphase(95%) no more games
        NO_MORE_GAMES = builder.nextAccessor("biped/cutscened_attack/nomoregames/nomoregames", (accessor) ->
                        new TitleCardAttackAnimation(0.01f, accessor, biped,
                                new AttackAnimation.Phase(0.1f, 0.12f, 1.6f, 2.4f, 1f, 2.45f, biped.get().kneeR, ColliderPreset.DRAGON_LEG)
                                        .addProperty(AnimationProperty.AttackPhaseProperty.HIT_PRIORITY, HitEntityList.Priority.DISTANCE)
                                        .addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, EpicFightSounds.WHOOSH.get())
                                        .addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1f))
                                        .addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.adder(25))
                                        .addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.setter(100)),

                                new AttackAnimation.Phase(2.45f, 8.98f, 8.99f, 8.99f, 1f, 8.991f, biped.get().kneeR, ColliderPreset.DRAGON_LEG)
                                        .addProperty(AnimationProperty.AttackPhaseProperty.HIT_PRIORITY, HitEntityList.Priority.DISTANCE)
                                        .addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, EpicFightSounds.WHOOSH.get())
                                        .addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(0f))
                                        .addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.adder(25))
                                        .addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.setter(100)),

                                getSimpleUltimateAttackPhase(biped, 503, 513, t0001Particles.BLOODY_CUT, null),
                                getSimpleUltimateAttackPhase(biped, 513, 524, t0001Particles.BLOODY_CUT, null),
                                getSimpleUltimateAttackPhase(biped, 524, 548, t0001Particles.BLOODY_CUT, null),
                                getSimpleUltimateAttackPhase(biped, 548, 561, t0001Particles.BLOODY_CUT, null),
                                getSimpleUltimateAttackPhase(biped, 561, 569, t0001Particles.BLOODY_CUT, null),
                                getSimpleUltimateAttackPhase(biped, 569, 590, t0001Particles.BLOODY_CUT, null),
                                getSimpleUltimateAttackPhase(biped, 590, 620, t0001Particles.BLOODY_CUT, null),
                                getSimpleUltimateAttackPhase(biped, 620, 658, t0001Particles.BLOODY_CUT, null),
                                getSimpleUltimateAttackPhase(biped, 658, 665, t0001Particles.BLOODY_CUT, null)
                                ,
                                new AttackAnimation.Phase(ReusableAnimEvents.getAnimTimeFromFrame(800), 0.12f, 15.5f, 20.4f, 1000f, ReusableAnimEvents.getAnimTimeFromFrame(990), biped.get().rootJoint, CGSColliderPresets.ULTIMATE_KNOCKBACK_AREABOX)
                                        .addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(20f))
                                        .addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.UNBLOCKALBE, EpicFightDamageTypeTags.COUNTER))
                                        .addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN)
                                        .addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(60f))

                        )


                                //1
                                .addEvents(
                                        //claws
                                        spawnClawFX(180, var1vec, new Quaternionf().rotationXYZ(120, 0, 0)),
                                        spawnClawFX(182, var2vec, new Quaternionf().rotationXYZ(50, 0, 0)),
                                        spawnClawFX(186, var2vec, new Quaternionf().rotationXYZ(-60, 0, 0)),
                                        spawnClawFX(188, var2vec, new Quaternionf().rotationXYZ(90, 0, 0)),
                                        spawnClawFX(190, var1vec, new Quaternionf().rotationXYZ(120, 0, 0)),
                                        spawnClawFX(191, var1vec, new Quaternionf().rotationXYZ(0, 50, 20)),
                                        spawnClawFX(192, var1vec, new Quaternionf().rotationXYZ(50, 0, 0)),
                                        spawnClawFX(195, var1vec, new Quaternionf().rotationXYZ(120, 0, 0)),
                                        spawnClawFX(200, var2vec, new Quaternionf().rotationXYZ(-60, 0, 0)),
                                        spawnClawFX(218, new Vec3(0.03, -0.25, 0), new Quaternionf().rotationXYZ(-50, 40, 20)),
                                        spawnClawFX(218, new Vec3(0.03, -0.15, 0), new Quaternionf().rotationXYZ(50, 20, 20)),

                                        spawnDirectionalBlockEffect("photon:angled2linedsmoke", ReusableAnimEvents.getAnimTimeFromTickTime(300), 0, 0f, 0,
                                                0, 1, 0, 0, 90f, 0
                                        ),
                                        spawnDirectionalEntityEffect("photon:rndwind", ReusableAnimEvents.getAnimTimeFromTickTime(245), 1.5f, 0.25f, 0, 0f, 0, 0, 0, 0, 0, EntityEffectExecutor.AutoRotate.XROT),
                                        spawnDirectionalEntityEffect("photon:shiddysphericalshockwave", ReusableAnimEvents.getAnimTimeFromTickTime(298), 0, 0.25f, 0, 3, 0, 0, 0, 0, 0, EntityEffectExecutor.AutoRotate.XROT),
                                        spawnDirectionalEntityEffect("photon:someaura", ReusableAnimEvents.getAnimTimeFromTickTime(100), 0, 0.05f, 0, 0, 0, 0, 0, 0, 0, EntityEffectExecutor.AutoRotate.XROT),
                                        spawnDirectionalEntityEffect("photon:wolffangstrikeflip", ReusableAnimEvents.getAnimTimeFromTickTime(305), 3, 1.25f, 0, 0, 0, 0, 0, 0, 0, EntityEffectExecutor.AutoRotate.NONE)
                                )


                                .addProperty(AnimationProperty.AttackAnimationProperty.FIXED_MOVE_DISTANCE, true)
                                .addProperty(AnimationProperty.AttackAnimationProperty.MOVE_TIME, TimePairList.create(0f, ReusableAnimEvents.getAnimTimeFromFrame(1200)))
//                        .addProperty(AnimationProperty.AttackAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.RAW_COORD)
//                        .addProperty(AnimationProperty.AttackAnimationProperty.COORD_SET_TICK, null)
                                .addProperty(AnimationProperty.AttackAnimationProperty.MOVE_ON_LINK, false)
                                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0, 45))
                                .addProperty(AnimationProperty.AttackAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)


        );


        NO_MORE_GAMES_HIT = builder.nextAccessor("biped/cutscened_attack/nomoregames/nomoregamesvictim", accesor ->
                new LongHitAnimation(0.01f, accesor, biped)
                        //   .addProperty(ActionAnimationProperty.MOVE_VERTICAL, true)
                        .addState(EntityState.TURNING_LOCKED, true)
                        .addState(EntityState.MOVEMENT_LOCKED, true)
                        .addState(EntityState.UPDATE_LIVING_MOTION, false)
                        .addState(EntityState.COMBO_ATTACKS_DOABLE, false)
                        .addState(EntityState.SKILL_EXECUTABLE, false)
                        .addState(EntityState.INACTION, true)
                        .addProperty(ActionAnimationProperty.SYNC_CAMERA, true)
                        .addProperty(ActionAnimationProperty.CANCELABLE_MOVE, false)
                        .addProperty(ActionAnimationProperty.IS_DEATH_ANIMATION, true)
                        .addProperty(ActionAnimationProperty.FIXED_HEAD_ROTATION, true)
                        .addState(EntityState.ATTACK_RESULT, damageSource -> {
                            if (damageSource instanceof EpicFightDamageSource epicFightDamageSource) {
                                epicFightDamageSource.setStunType(StunType.NONE);
                                epicFightDamageSource.addRuntimeTag(EpicFightDamageTypeTags.NO_STUN);
                            }
                            return damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || damageSource.is(EpicFightDamageTypeTags.EXECUTION) ?
                                    AttackResult.ResultType.SUCCESS : AttackResult.ResultType.MISSED;
                        })
                        .addProperty(ActionAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)


        );


    }

    @SuppressWarnings("SameParameterValue")
    private static AttackAnimation.Phase getSimpleUltimateAttackPhase(Armatures.ArmatureAccessor<HumanoidArmature> biped, int startFrame, int endFrame,
                                                                      @Nullable DeferredHolder<ParticleType<?>, HitParticleType> hitParticle, @Nullable SoundEvent hitsound) {

        float start = ReusableAnimEvents.getAnimTimeFromFrame(startFrame);
        float antic = ReusableAnimEvents.getAnimTimeFromFrame(startFrame);
        float preDelay = ReusableAnimEvents.getAnimTimeFromFrame(startFrame + 1);
        float contact = ReusableAnimEvents.getAnimTimeFromFrame(endFrame - 1);
        float end = ReusableAnimEvents.getAnimTimeFromFrame(endFrame);

        return new AttackAnimation.Phase(
                start, antic, preDelay, contact, 0f, end,
                InteractionHand.MAIN_HAND,
                biped.get().rootJoint,
                ColliderPreset.BATTOJUTSU_DASH
        )
                .addProperty(AnimationProperty.AttackPhaseProperty.HIT_PRIORITY, HitEntityList.Priority.DISTANCE)
                .addProperty(AnimationProperty.AttackPhaseProperty.PARTICLE, hitParticle)
                .addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, hitsound)
                .addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.NONE)
                .addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1f))
                .addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(0.01f))
                .addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.setter(0.0f))
                .addProperty(AnimationProperty.AttackPhaseProperty.EXTRA_DAMAGE, Set.of(TARGET_MAX_HEALTH_NON_LETHAL.create(15, 0.5f)))
                .addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.EXECUTION, EpicFightDamageTypeTags.NO_STUN))
                .addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.setter(100));
    }


    private static AnimationEvent.@NotNull InTimeEvent<AnimationEvent.Event<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> renderImpactFrame(int blenderFrame, String FrameLocation, int frameNumber) {
        return AnimationEvent.InTimeEvent.create(ReusableAnimEvents.getAnimTimeFromFrame(blenderFrame), (e, s, p) -> {
            try {
                ResourceLocation location = ResourceLocation.fromNamespaceAndPath(t0001.MODID, FrameLocation + frameNumber + ".png");
                System.out.println(location);
                VideoRendererUtil.playVideo(location.toString(), e.getId(), 1.0f);
            } catch (Exception exception) {
                VideoRendererUtil.stopVideo(e.getOriginal().getUUID());
            }
        }, AnimationEvent.Side.LOCAL_CLIENT);
    }


    private static AnimationEvent.@NotNull InTimeEvent<AnimationEvent.Event<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> spawnClawFX(int TickTime, Vec3 offset, Quaternionf RotationOffset) {
        return AnimationEvent.InTimeEvent.create(ReusableAnimEvents.getAnimTimeFromTickTime(TickTime), (e, s, p) ->
                {
                    FX clawfx = FXHelper.getFX(ResourceLocation.parse("photon:nmgclawright"));
                    EntityEffectExecutor Claw = new EntityEffectExecutor(clawfx, e.getLevel(), e.getOriginal(), EntityEffectExecutor.AutoRotate.XROT);
                    Claw.setOffset(offset.toVector3f());
                    Claw.setRotation(RotationOffset);
                    Claw.setScale(1, 1, 1);
                    Claw.setAllowMulti(true);
                    Claw.setForcedDeath(true);
                    Claw.setDelay(0);
                    Claw.start();

                }

                , AnimationEvent.Side.CLIENT);
    }


}
