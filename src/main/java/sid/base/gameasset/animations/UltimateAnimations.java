package sid.base.gameasset.animations;

import com.lowdragmc.photon.client.fx.EntityEffectExecutor;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import sid.base.gameasset.animations.collider.CGSColliderPresets;
import sid.base.gameasset.t0001Skills;
import sid.base.network.CustomSynchedAnimationVariablekeys;
import sid.base.skill.t0001SkillDataKeys;
import sid.base.utils.JointTrackedEntityEffect;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.property.MoveCoordFunctions;
import yesman.epicfight.api.animation.types.*;
import yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.HitEntityList;
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
import yesman.epicfight.world.damagesource.StunType;

import java.util.Optional;

import static sid.base.gameasset.ReusableEvents.JointTrack.getJointWithTranslation;

public class UltimateAnimations {

    public static AnimationManager.AnimationAccessor<AttackAnimation> ONE_INCH_COUNTER;
    public static AnimationManager.AnimationAccessor<LongHitAnimation> ONE_INCH_COUNTER_HIT;

    public static AnimationManager.AnimationAccessor<StaticAnimation> ONE_INCH_COUNTER_BAIT;
    public static AnimationManager.AnimationAccessor<StaticAnimation> ONE_INCH_COUNTER_BAIT_FAIL;


    public static void build(AnimationManager.AnimationBuilder builder) {
        Armatures.ArmatureAccessor<HumanoidArmature> biped = Armatures.BIPED;


        ONE_INCH_COUNTER = builder.nextAccessor("biped/skill/one_inch_counter/one_inch_counter", (accessor) -> new AttackAnimation(0.01F, accessor, biped,


                        new AttackAnimation.Phase(0.01F, 0.01F, 5.48F, 5.9F, 5.7F, 6.91F,
                                biped.get().handR, CGSColliderPresets.ULTIMATE_KNOCKBACK_AREABOX)
                                .addProperty(AnimationProperty.AttackPhaseProperty.SWING_SOUND, EpicFightSounds.BLUNT_HIT_HARD.get())
                                .addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, EpicFightSounds.LASER_BLAST.get())
                                .addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER,ValueModifier.setter(0))
                                .addProperty(AnimationProperty.AttackPhaseProperty.HIT_PRIORITY, HitEntityList.Priority.TARGET)
                                .addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.LONG)
                                .addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.adder(5F))
                                .addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.adder(500F)))

                        .addProperty(AnimationProperty.AttackAnimationProperty.FIXED_HEAD_ROTATION, true)
                        .addProperty(AnimationProperty.AttackAnimationProperty.REMOVE_DELTA_MOVEMENT, true)
                        .addProperty(AnimationProperty.AttackAnimationProperty.STOP_MOVEMENT, true)
                        .addState(EntityState.TURNING_LOCKED,true)
                        .addState(EntityState.MOVEMENT_LOCKED, true)

                        .addProperty(AnimationProperty.AttackAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.RAW_COORD)
                        .addProperty(AnimationProperty.AttackAnimationProperty.DEST_LOCATION_PROVIDER, MoveCoordFunctions.SYNCHED_TARGET_ENTITY_LOCATION_VARIABLE)
                        .addProperty(AnimationProperty.AttackAnimationProperty.ENTITY_YROT_PROVIDER, MoveCoordFunctions.LOOK_DEST)

                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create((e, s, p) -> {
                            if (e instanceof ServerPlayerPatch serverPlayerPatch) {
                                if (serverPlayerPatch.getSkill(SkillSlots.IDENTITY).hasSkill(t0001Skills.FANG_COUNTER.get())) {
                                    serverPlayerPatch.getSkill(SkillSlots.IDENTITY).getDataManager().setDataSync(t0001SkillDataKeys.ULTIMATE_MOVE_MODE_SET, 1);
                                }

                            }
                        }, AnimationEvent.Side.SERVER))

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
//                                entity.level().playSound(null, blockpos,
//                                        WOMSounds.SOLAR_HIT.get(),
//                                        SoundSource.PLAYERS,
//                                        1.0F,
//                                        0.75F
//                                );
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
                        .addProperty(AnimationProperty.AttackAnimationProperty.CANCELABLE_MOVE, false)
                        .addProperty(AnimationProperty.AttackAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
        );


        ONE_INCH_COUNTER_HIT = builder.nextAccessor("biped/skill/one_inch_counter/one_inch_counter_hit", (accessor) -> new LongHitAnimation(0.091F, accessor, biped)
                        .addProperty(ActionAnimationProperty.CANCELABLE_MOVE, false)
                        .addEvents(AnimationEvent.InTimeEvent.create(5.9F, (entitypatch, animation, params) -> {

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
                        }, AnimationEvent.Side.BOTH))
                .addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create((e,s,p)->
                        { //TODO:make this a util
                            Optional<Integer> killerId = e.getAnimator().getVariables().get(CustomSynchedAnimationVariablekeys.KILLER_ENTITY.get(), s.get().getRealAnimation());
                            if (killerId.isEmpty()) {return;}
                            Entity attackerEntity = e.getLevel().getEntity(killerId.get());
                            if (!(attackerEntity instanceof LivingEntity attacker)) {return;}
                            LivingEntity target = e.getOriginal();
                            if (target.level().isClientSide()) {return;}
                            if (!target.isAlive()) {return;}
                            if (target.getPersistentData().getBoolean("execution_complete")) {return;}
                            target.getPersistentData().putBoolean("execution_complete", true);
                            float damage = target.getMaxHealth() * 2.0F;
                            MinecraftServer server = target.getServer();
                            if (server == null) {return;}
                            server.execute(() -> {
                                if (!target.isAlive()) {return;}
                                if (attacker instanceof ServerPlayer player) {
                                    target.hurt(target.damageSources().playerAttack(player), damage);
                                } else {
                                    target.hurt(target.damageSources().mobAttack(attacker), damage);
                                }
                            });
                        }
                        , AnimationEvent.Side.SERVER))
                        .addProperty(ActionAnimationProperty.MOVE_VERTICAL, true)
                        .addProperty(ActionAnimationProperty.CANCELABLE_MOVE, false)
                        .addProperty(ActionAnimationProperty.IS_DEATH_ANIMATION, true)
                        .addProperty(ActionAnimationProperty.FIXED_HEAD_ROTATION, true)
        );

        ONE_INCH_COUNTER_BAIT = builder.nextAccessor("biped/skill/one_inch_counter/one_inch_bait", (accessor) ->
                new StaticAnimation(
                        false, accessor, biped
                )
                        .addProperty(ActionAnimationProperty.CANCELABLE_MOVE, false)
                        .addProperty(ActionAnimationProperty.STOP_MOVEMENT, true)
                        .addState(EntityState.MOVEMENT_LOCKED, true)
                        .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
                        .addEvents(ActionAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create((e, s, p) -> SpawnRootJointTrackFX(e, "photon:menacingcounter", true), AnimationEvent.Side.CLIENT))
        );

        ONE_INCH_COUNTER_BAIT_FAIL = builder.nextAccessor("biped/skill/one_inch_counter/one_inch_bait_fail", (accessor) -> new StaticAnimation(false, accessor, biped)
                .addProperty(ActionAnimationProperty.CANCELABLE_MOVE, false)
        );

    }

    private static void SpawnRootJointTrackFX(LivingEntityPatch<?> e, @SuppressWarnings("SameParameterValue") String FxResourceLocationString, boolean setmulti) {
        FX menacing = FXHelper.getFX(ResourceLocation.parse(FxResourceLocationString));
        Level l = e.getOriginal().level();
        Entity eo = e.getOriginal();
        Armature ea = e.getArmature();
        JointTrackedEntityEffect jtef = new JointTrackedEntityEffect(menacing, l, eo, ea.rootJoint, Vec3f.ZERO, EntityEffectExecutor.AutoRotate.NONE, false);
        jtef.setOffset(0, 0, 0);
        jtef.setRotation(0, 0, 0);
        jtef.setScale(1, 1, 1);
        jtef.setAllowMulti(setmulti);
        jtef.setForcedDeath(true);
        jtef.setDelay(0);
        jtef.start();
    }


}
