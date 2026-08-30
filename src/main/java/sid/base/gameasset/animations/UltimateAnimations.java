package sid.base.gameasset.animations;

import com.lowdragmc.photon.client.fx.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import sid.base.gameasset.ReusableEventsAndUtils;
import sid.base.gameasset.animations.collider.CGSColliderPresets;
import sid.base.gameasset.animations.types.ProtectedHitAnimation;
import sid.base.gameasset.animations.types.TitleCardAttackAnimation;
import sid.base.world.t0001Sounds;
import sid.base.particle.t0001Particles;
import sid.base.utils.GroundWaveUtil;
import sid.base.world.ExtraSpecialDamageTypeTags;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.property.MoveCoordFunctions;
import yesman.epicfight.api.animation.types.*;
import yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.HitEntityList;
import yesman.epicfight.api.utils.LevelUtil;
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
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.ExtraDamageInstance;
import yesman.epicfight.world.damagesource.StunType;

import java.util.*;

import static sid.base.gameasset.ReusableEventsAndUtils.JointTrack.getJointWithTranslation;
import static sid.base.gameasset.ReusableEventsAndUtils.getAnimTimeFromFrame;
import static sid.base.utils.ReusableAnimEvents.*;


public class UltimateAnimations {

    public static AnimationManager.AnimationAccessor<TitleCardAttackAnimation> IGNITION_STOMP;
    public static AnimationManager.AnimationAccessor<TitleCardAttackAnimation> SON_SUN;

    public static AnimationManager.AnimationAccessor<TitleCardAttackAnimation> ONE_INCH_COUNTER;
    public static AnimationManager.AnimationAccessor<LongHitAnimation> ONE_INCH_COUNTER_HIT;
    public static AnimationManager.AnimationAccessor<StaticAnimation> ONE_INCH_COUNTER_BAIT;
    public static AnimationManager.AnimationAccessor<StaticAnimation> ONE_INCH_COUNTER_BAIT_FAIL;

    public static AnimationManager.AnimationAccessor<TitleCardAttackAnimation> FSK;

    public static AnimationManager.AnimationAccessor<TitleCardAttackAnimation> SPEED_PUNCH;
    public static AnimationManager.AnimationAccessor<TitleCardAttackAnimation> NO_MORE_GAMES;
    public static AnimationManager.AnimationAccessor<ProtectedHitAnimation> NO_MORE_GAMES_HIT;

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


        IGNITION_STOMP = builder.nextAccessor("biped/skill/ignition_stomp", accessor ->
                new TitleCardAttackAnimation(
                        0.1f,
                        0.1f,
                        getAnimTimeFromFrame(30),
                        getAnimTimeFromFrame(42),
                        getAnimTimeFromFrame(75),
                        CGSColliderPresets.ULTIMATE_KNOCKBACK_BOX,
                        biped.get().rootJoint,
                        accessor,
                        biped
                )
                        .addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.NONE)
                        .addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, t0001Sounds.HARD_KICK.get())
                        .addProperty(AnimationProperty.AttackPhaseProperty.PARTICLE, t0001Particles.BUZZ_HIT)
                        .addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER,new ValueModifier.Multiplier(3.0f))
                        .addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(
                                DamageTypeTags.IS_FIRE,
                                DamageTypeTags.BYPASSES_RESISTANCE,
                                ExtraSpecialDamageTypeTags.RAG_DOLL_LAUNCH_UP_RAND,
                                EpicFightDamageTypeTags.IS_MAGIC,
                                EpicFightDamageTypeTags.FINISHER
                        ))
                        .addEvents(
                                AnimationEvent.InTimeEvent.create(getAnimTimeFromFrame(40), (e, s, p) ->
                                {
                                    LivingEntity entity = e.getOriginal();

                                    spawnJointEffect("photon:ignition_stomp", entity, biped.get().legR, false, true);


                                }, AnimationEvent.Side.CLIENT),

                                AnimationEvent.InTimeEvent.create(getAnimTimeFromFrame(40), (e, s, p) ->
                                {
                                    LivingEntity entity = e.getOriginal();
                                    Vec3 slamPos = entity.position();
                                    BlockPos blockPos = BlockPos.containing(slamPos.x, slamPos.y - 0.1, slamPos.z);

                                    if (!LevelUtil.canTransferShockWave(entity.level(), blockPos, entity.level().getBlockState(blockPos))) {
                                        blockPos = blockPos.below();
                                    }


                                    Vec3 fracturePos = Vec3.atCenterOf(blockPos);

                                    LevelUtil.circleSlamFracture(
                                            entity,
                                            entity.level(),
                                            fracturePos,
                                            1.399D,
                                            false,
                                            true,
                                            false
                                    );

                                    GroundWaveUtil.trigger_wave(
                                            e.getOriginal(),
                                            e.getOriginal().getOnPos().getCenter(),
                                            6, 1, 10, 20, 4, true, 2,
                                            GroundWaveUtil.WaveMode.CIRCLE
                                    );

                                    entity.level().playSound(
                                            null,
                                            entity.getOnPos(),
                                            t0001Sounds.HARD_KICK.value(),
                                            SoundSource.BLOCKS,
                                            1.0f,
                                            1.3f
                                    );

                                }, AnimationEvent.Side.SERVER)
                        )
                        .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)

        );

        SON_SUN = builder.nextAccessor("biped/skill/son_sun", accessor ->
                new TitleCardAttackAnimation(
                        0.1f,
                        0.1f,
                        getAnimTimeFromFrame(350),
                        getAnimTimeFromFrame(360),
                        getAnimTimeFromFrame(560),
                        CGSColliderPresets.ULTIMATE_KNOCKBACK_BOX,
                        biped.get().rootJoint,
                        accessor,
                        biped
                )
                        .addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN)
                        .addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, t0001Sounds.HARD_KICK.get())
                        .addProperty(AnimationProperty.AttackPhaseProperty.PARTICLE, t0001Particles.BUZZ_HIT)
                        .addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(DamageTypeTags.IS_FIRE, DamageTypeTags.BYPASSES_RESISTANCE, EpicFightDamageTypeTags.IS_MAGIC, EpicFightDamageTypeTags.FINISHER))
                        .addEvents(
                                AnimationEvent.InTimeEvent.create(getAnimTimeFromFrame(195), (e, s, p) ->
                                {
                                    LivingEntity entity = e.getOriginal();
                                    spawnJointEffect("photon:sun_blade", entity, biped.get().toolR, true, false);
                                }, AnimationEvent.Side.CLIENT),

                                AnimationEvent.InTimeEvent.create(getAnimTimeFromFrame(0), (e, s, p)-> {
                                    LivingEntity entity = e.getOriginal();

                                    spawnJointEffect("photon:solar_awaken",entity,biped.get().rootJoint ,true,true, new Vec3f(0,-1.5,3));

                                }, AnimationEvent.Side.CLIENT),


                                igniteLastHitenemies(getAnimTimeFromFrame(360))

                        )






                        .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)

        );


        TOOEASYTES2 = builder.nextAccessor("biped/dgs/bladetest", ac ->
                new StaticAnimation(0.0f, true, ac, biped)
                        .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
        );


        ONE_INCH_COUNTER = builder.nextAccessor("biped/skill/one_inch_counter/one_inch_counter", (accessor) -> new TitleCardAttackAnimation(0.01F, accessor, biped,
                //prev predlay - 5.468
                new AttackAnimation.Phase(0.01F, 0.01F, 5.68F, 5.9F, Float.MAX_VALUE, 6.91F,
                        biped.get().handR, CGSColliderPresets.ULTIMATE_KNOCKBACK_BOX)
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

                        playCamAnim("counter", 2),
                        AnimationEvent.InTimeEvent.create(0.0f, Animations.ReusableSources.PLAY_SOUND, AnimationEvent.Side.LOCAL_CLIENT)
                                .params(t0001Sounds.TESTONE_INCH.get()),
                        renderVideoIfCamAnim(270, "impact_frames/one_inch/frame0impact", ".mp4", 1.1f)
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

                        AnimationEvent.InTimeEvent.create(4.95F, (entitypatch, animation, params) -> {
                            if (entitypatch != null) {

                                spawnJointEffect("photon:angled2linedsmokecounter", entitypatch.getOriginal(), biped.get().rootJoint, true, true, new Vec3f(-1, 0.45f, -3.5f));


                            }
                        }, AnimationEvent.Side.CLIENT)


                )


                .addProperty(AnimationProperty.AttackAnimationProperty.PLAY_SPEED_MODIFIER, (l, s, p, k, f) -> 0.832333f) //self-explanatory
        );


        ONE_INCH_COUNTER_HIT = builder.nextAccessor("biped/skill/one_inch_counter/one_inch_counter_hit", (accessor) -> new LongHitAnimation(0.01F, accessor, biped)
                .addProperty(ActionAnimationProperty.CANCELABLE_MOVE, false)
                .addEvents(
                        playCamAnimMirrored("counter", 2),

                        renderVideoIfCamAnim(260, "impact_frames/one_inch/frame0impact", ".mp4", 1.0f),

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
                        AnimationEvent.SimpleEvent.create(ReusableEventsAndUtils.KillandCredit, AnimationEvent.Side.SERVER))

                .addProperty(AnimationProperty.AttackAnimationProperty.PLAY_SPEED_MODIFIER, (s, f, t, k, r) -> 0.833333f) //self explanatory
                .addProperty(ActionAnimationProperty.MOVE_VERTICAL, true)
                .addProperty(ActionAnimationProperty.SYNC_CAMERA, false)
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
                        .addEvents(ActionAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create((e, s, p) -> SpawnRootJointTrackFX(e, "photon:menacingcounter", true), AnimationEvent.Side.CLIENT))
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
        //DON'T LEAVE SPACES INSIDE ANIMATION IF AN ANIMATION SHOULD BE MOVING AND IS MULTIPHASED, MAKE PHASES COVER THE ANIMATION COMPLETELY WITH VERY LONG PREDELAY AND ANTIC AS FILLER
        NO_MORE_GAMES = builder.nextAccessor("biped/cutscened_attack/nomoregames/nomoregames", (accessor) ->
                        new TitleCardAttackAnimation(0.05f, accessor, biped,
                                //prev start 0.1f
                                new AttackAnimation.Phase(0.001f, 0.12f, 1.6f, 2.4f, 520f, 2.45f, biped.get().kneeR, ColliderPreset.DRAGON_LEG)
                                        .addProperty(AnimationProperty.AttackPhaseProperty.HIT_PRIORITY, HitEntityList.Priority.TARGET)
                                        .addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, EpicFightSounds.NO_SOUND.get())
                                        .addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.EXECUTION,  EpicFightDamageTypeTags.NO_STUN, ExtraSpecialDamageTypeTags.SPECIAL_EXECUTION))
                                        .addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1f))
                                        .addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(0.005f))
                                        .addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.setter(0.0f)) //set impact to 0 because impact also does damage
                                        .addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.setter(100)),

                                //Fake phase to prevent player or animation movement from stalling
                                new AttackAnimation.Phase(2.4501f, 8.98f, 8.99f, 8.99f, 831f, 8.991f, biped.get().kneeR, ColliderPreset.DRAGON_LEG)
                                        .addProperty(AnimationProperty.AttackPhaseProperty.HIT_PRIORITY, HitEntityList.Priority.DISTANCE)
                                        .addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, EpicFightSounds.NO_SOUND.get())
                                        .addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.EXECUTION, ExtraSpecialDamageTypeTags.SPECIAL_EXECUTION))
                                        .addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(0f))
                                        .addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.adder(25))
                                        .addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.setter(100)),

                                getSimpleUltimateAttackPhase(biped, 503, 513, t0001Particles.BLOODY_CUT, EpicFightSounds.NO_SOUND.get()),
                                getSimpleUltimateAttackPhase(biped, 513, 524, t0001Particles.BLOODY_CUT, EpicFightSounds.NO_SOUND.get()),
                                getSimpleUltimateAttackPhase(biped, 524, 548, t0001Particles.BLOODY_CUT, EpicFightSounds.NO_SOUND.get()),
                                getSimpleUltimateAttackPhase(biped, 548, 561, t0001Particles.BLOODY_CUT, EpicFightSounds.NO_SOUND.get()),
                                getSimpleUltimateAttackPhase(biped, 561, 569, t0001Particles.BLOODY_CUT, EpicFightSounds.NO_SOUND.get()),
                                getSimpleUltimateAttackPhase(biped, 569, 590, t0001Particles.BLOODY_CUT, EpicFightSounds.NO_SOUND.get()),
                                getSimpleUltimateAttackPhase(biped, 590, 620, t0001Particles.BLOODY_CUT, EpicFightSounds.NO_SOUND.get()),
                                getSimpleUltimateAttackPhase(biped, 620, 658, t0001Particles.BLOODY_CUT, EpicFightSounds.NO_SOUND.get()),
                                getSimpleUltimateAttackPhase(biped, 658, 665, t0001Particles.BLOODY_CUT, EpicFightSounds.NO_SOUND.get())
                                ,
                                new AttackAnimation.Phase(getAnimTimeFromFrame(800), 0.12f, 15.5f, 20.4f, 1000f, getAnimTimeFromFrame(990), biped.get().rootJoint, CGSColliderPresets.ULTIMATE_KNOCKBACK_BOX)
                                        .addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(20f))
                                        .addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.BYPASS_DODGE, EpicFightDamageTypeTags.COUNTER))
                                        .addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN)
                                        .addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(2.420f))
                                        .addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(60.69f))

                        )


                                //1
                                .addEvents(
                                        //sound
                                        AnimationEvent.InTimeEvent.create(0.101F, Animations.ReusableSources.PLAY_SOUND, AnimationEvent.Side.SERVER)
                                                .params(t0001Sounds.NO_MORE_GAMES.get()),

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

                                        spawnJointEffect_f(900,"photon:angled2linedsmoke", biped.get().rootJoint, true, new Vec3f(60, 15, 5), new Vec3f(0.25 ,0.25f,0)),
                                        spawnJointEffect_t(305,"photon:wolffangstrikeflip",  biped.get().rootJoint, true, new Vec3f(-7, 0, 2)),
                                        spawnEntityEffect_t(100, "photon:someaura",EntityEffectExecutor.AutoRotate.NONE, true,false,null,null),
                                        spawnJointEffect_t(262,"photon:rndwind",biped.get().handR,true,new Vec3f(0.35, 0.65 ,0),new Vec3f(80 ,-15 ,0)),
                                        spawnJointEffect_f(265,"photon:ki_hand",biped.get().handR,false, new Vec3f(0,0,0)),
                                        spawnJointEffect_f(220,"photon:animeyelloweye",biped.get().head,true,new Vec3f(0.05, 0.10 ,-0.27)),
                                        spawnDirectionalJointBlockEffect("photon:shiddysphericalshockwave",ReusableEventsAndUtils.getAnimTimeFromTickTime(298),3,0.25f,0, biped.get().head, true)
                                )


                                .addProperty(AnimationProperty.AttackAnimationProperty.FIXED_MOVE_DISTANCE, true)
                                .addProperty(AnimationProperty.AttackAnimationProperty.MOVE_TIME, TimePairList.create(0f, getAnimTimeFromFrame(1300)))
//                        .addProperty(AnimationProperty.AttackAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.RAW_COORD)
//                        .addProperty(AnimationProperty.AttackAnimationProperty.COORD_SET_TICK, null)
                                .addProperty(AnimationProperty.AttackAnimationProperty.MOVE_ON_LINK, false)
                                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0, 95))
                                .addProperty(CustomAnimationProperties.SSSpecialAnimationProperty.NO_PHYSICS_TIME, TimePairList.create(0, 95))
                                .addProperty(AnimationProperty.AttackAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)



        );


        //TODO: Add Delta Movement On Launch
        NO_MORE_GAMES_HIT = builder.nextAccessor("biped/cutscened_attack/nomoregames/nomoregamesvictim", accesor ->
                new ProtectedHitAnimation(0.01f, accesor, biped)
                        //.addProperty(ActionAnimationProperty.MOVE_VERTICAL, true)
                        .addProperty(ActionAnimationProperty.SYNC_CAMERA, false)
                        .addProperty(CustomAnimationProperties.SSSpecialAnimationProperty.NO_PHYSICS_TIME, TimePairList.create(0, 95))
                        .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0, 95))
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS,
                                AnimationEvent.SimpleEvent.create(ReusableEventsAndUtils.killIfHealthTooLowAndCredit, AnimationEvent.Side.SERVER))

                        .addProperty(ActionAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)

        );

        SPEED_PUNCH = builder.nextAccessor("biped/cutscened_attack/nomoregames/speedpunch", ac ->
                new TitleCardAttackAnimation(
                        0.1f,
                        getAnimTimeFromFrame(19),
                        getAnimTimeFromFrame(30),
                        getAnimTimeFromFrame(40),
                        69f,
                        ColliderPreset.BIPED_BODY_COLLIDER,
                        biped.get().rootJoint,
                        ac,
                        biped
                        )
                        .addProperty(AnimationProperty.AttackAnimationProperty.FIXED_MOVE_DISTANCE, true)
                        .addProperty(ActionAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
                        .addProperty(CustomAnimationProperties.SSSpecialAnimationProperty.NO_PHYSICS_TIME, TimePairList.create(getAnimTimeFromFrame(8), getAnimTimeFromFrame(67)))

                );





    }



    @SuppressWarnings("SameParameterValue")
    private static AttackAnimation.Phase getSimpleUltimateAttackPhase(Armatures.ArmatureAccessor<HumanoidArmature> biped, int startFrame, int endFrame,
                                                                      @Nullable DeferredHolder<ParticleType<?>, HitParticleType> hitParticle, @Nullable SoundEvent hitsound) {

        float start = getAnimTimeFromFrame(startFrame);
        float antic = getAnimTimeFromFrame(startFrame);
        float preDelay = getAnimTimeFromFrame(startFrame + 1);
        float contact = getAnimTimeFromFrame(endFrame - 1);
        float end = getAnimTimeFromFrame(endFrame);

        return new AttackAnimation.Phase(
                start, antic, preDelay, contact, 930f, end,
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
                .addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.EXECUTION, EpicFightDamageTypeTags.NO_STUN, ExtraSpecialDamageTypeTags.SPECIAL_EXECUTION))
                .addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.setter(100));
    }


    private static AnimationEvent.@NotNull InTimeEvent<AnimationEvent.Event<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> spawnClawFX(int TickTime, Vec3 offset, Quaternionf RotationOffset) {
        return AnimationEvent.InTimeEvent.create(ReusableEventsAndUtils.getAnimTimeFromTickTime(TickTime), (e, s, p) ->
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
