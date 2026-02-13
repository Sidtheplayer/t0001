package sid.base.gameasset.animations;

import com.google.common.collect.HashBiMap;
import com.lowdragmc.photon.client.fx.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import sid.base.gameasset.ReusableEvents;
import sid.base.gameasset.animations.collider.CGSColliderPresets;
import sid.base.gameasset.animations.types.TitleCardAttackAnimation;
import sid.base.gameasset.t0001Sounds;
import sid.base.utils.ReusableAnimEvents;
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
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;

import java.util.Objects;
import java.util.Set;

import static sid.base.gameasset.ReusableEvents.JointTrack.getJointWithTranslation;


public class UltimateAnimations {


    @OnlyIn(Dist.CLIENT) // this is probably unsafe
    public static final HashBiMap<Integer, FXRuntime> fxRuntimeHashBiMap = HashBiMap.create();
    //HashBiMap to map entityId and runtimes to destroy or manage outside the origin, I should really also add an identifier for fx

    public static AnimationManager.AnimationAccessor<TitleCardAttackAnimation> ONE_INCH_COUNTER;
    public static AnimationManager.AnimationAccessor<LongHitAnimation> ONE_INCH_COUNTER_HIT;
    public static AnimationManager.AnimationAccessor<StaticAnimation> ONE_INCH_COUNTER_BAIT;
    public static AnimationManager.AnimationAccessor<StaticAnimation> ONE_INCH_COUNTER_BAIT_FAIL;

    public static AnimationManager.AnimationAccessor<TitleCardAttackAnimation> FSK;


    public static void build(AnimationManager.AnimationBuilder builder) {
        Armatures.ArmatureAccessor<HumanoidArmature> biped = Armatures.BIPED;


        ONE_INCH_COUNTER = builder.nextAccessor("biped/skill/one_inch_counter/one_inch_counter", (accessor) -> new TitleCardAttackAnimation(0.01F, accessor, biped,
                       //prev predlay - 5.468
                        new AttackAnimation.Phase(0.01F, 0.01F, 5.68F, 5.9F, Float.MAX_VALUE, 6.91F,
                                biped.get().handR, CGSColliderPresets.ULTIMATE_KNOCKBACK_AREABOX)
                                .addProperty(AnimationProperty.AttackPhaseProperty.SWING_SOUND, EpicFightSounds.BLUNT_HIT_HARD.get())
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
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create((e, s, p) ->
                                        e.playSound(t0001Sounds.TESTONE_INCH, 150f, 1.55f, 1.58f)
                                , AnimationEvent.Side.LOCAL_CLIENT))


                        .addEvents(AnimationProperty.AttackAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create(((entitypatch, animation, params) -> entitypatch.getOriginal().setInvulnerable(false)), AnimationEvent.Side.SERVER))
                        .addEvents(AnimationEvent.InTimeEvent.create(5.0F, (entitypatch, animation, params) -> {
                            if (entitypatch != null) {
                                LivingEntity entity = entitypatch.getOriginal();
                                BlockPos blockpos = new BlockPos((int) entitypatch.getOriginal().getX(), (int) entitypatch.getOriginal().getY(), (int) entitypatch.getOriginal().getZ());
                                entity.level().playSound(null, blockpos,
                                        EpicFightSounds.LASER_BLAST.get(),
                                        SoundSource.PLAYERS,
                                        1.0F,
                                        0.45F
                                );

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
                        }, AnimationEvent.Side.BOTH))
                        .addEvents(AnimationEvent.InTimeEvent.create(5.10F,
                                (e, s, p) ->
                                        new EntityEffectExecutor(
                                                FXHelper.getFX(ResourceLocation.parse("photon:angled2linedsmoke")),
                                                e.getLevel(),
                                                e.getOriginal(),
                                                EntityEffectExecutor.AutoRotate.XROT).start()
                                , AnimationEvent.Side.CLIENT))

                        .addProperty(AnimationProperty.AttackAnimationProperty.SYNC_CAMERA, true)
                        .addProperty(AnimationProperty.AttackAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE) //self explanatory
        );


        ONE_INCH_COUNTER_HIT = builder.nextAccessor("biped/skill/one_inch_counter/one_inch_counter_hit", (accessor) -> new LongHitAnimation(0.01F, accessor, biped)
                .addProperty(ActionAnimationProperty.CANCELABLE_MOVE, false)
                .addState(EntityState.ATTACK_RESULT, (damagesource) -> damagesource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && damagesource.is(EpicFightDamageTypeTags.EXECUTION) ? AttackResult.ResultType.SUCCESS : AttackResult.ResultType.MISSED)
                .addEvents(
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
                        }, AnimationEvent.Side.BOTH),


                        AnimationEvent.InTimeEvent.create(0.0f, ReusableAnimEvents.CAM_ANIM, AnimationEvent.Side.CLIENT)

                )
                .addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create((e, s, p) ->
                                e.playSound(t0001Sounds.TESTONE_INCH, 150f, 1.2f, 1.25f)
                        , AnimationEvent.Side.LOCAL_CLIENT))


                //kill entity and credit the killer
                .addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS,
                        AnimationEvent.SimpleEvent.create(ReusableEvents.KillandCredit, AnimationEvent.Side.SERVER))

                .addProperty(AnimationProperty.AttackAnimationProperty.PLAY_SPEED_MODIFIER, (s,f,t,k,r)-> 0.833333f) //self explanatory
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
                        .addState(EntityState.ATTACK_RESULT, damageSource -> damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)? AttackResult.ResultType.SUCCESS : AttackResult.ResultType.BLOCKED)
                        .addEvents(ActionAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create((e, s, p) -> ReusableAnimEvents.SpawnRootJointTrackFX(e, "photon:menacingcounter", true), AnimationEvent.Side.CLIENT))
                        .addEvents(ActionAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create(
                                (e, s, p) -> {
                                    if (e.isLogicalClient()) {
                                        FXRuntime toDestroy = fxRuntimeHashBiMap.remove(e.getId());
                                        Objects.requireNonNull(toDestroy).destroy(true);
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
                        .addProperty(AnimationProperty.AttackAnimationProperty.MOVE_VERTICAL, true)
                        .addProperty(AnimationProperty.AttackAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0f, 150f))
        );


    }


}
