package sid.t0001.gameasset.animations;

import com.lowdragmc.photon.client.fx.EntityEffectExecutor;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import sid.t0001.utils.JointTrackedEntityEffect;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.property.MoveCoordFunctions;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.LongHitAnimation;
import yesman.epicfight.api.animation.types.grappling.GrapplingAttackAnimation;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.TimePairList;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.registry.entries.EpicFightMobEffects;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.registry.entries.EpicFightSounds;

import static sid.t0001.gameasset.ReusableEvents.JointTrack.getJointWithTranslation;

public class UltimateAnimations {

    public static AnimationManager.AnimationAccessor<GrapplingAttackAnimation> ONE_INCH_COUNTER;
    public static AnimationManager.AnimationAccessor<LongHitAnimation> ONE_INCH_COUNTER_HIT;

    public static AnimationManager.AnimationAccessor<ActionAnimation> ONE_INCH_COUNTER_BAIT;
    public static AnimationManager.AnimationAccessor<ActionAnimation> ONE_INCH_COUNTER_BAIT_FAIL;


    public static void build(AnimationManager.AnimationBuilder builder) {
        Armatures.ArmatureAccessor<HumanoidArmature> biped = Armatures.BIPED;


        ONE_INCH_COUNTER = builder.nextAccessor("biped/skill/one_inch_counter/one_inch_counter", (accessor) -> new GrapplingAttackAnimation(0.51F, 1.5F, accessor, biped)

                        .addProperty(AnimationProperty.AttackAnimationProperty.FIXED_HEAD_ROTATION, true)
                        .addState(EntityState.PHASE_LEVEL, 3)

                        .addProperty(ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.RAW_COORD)
                        .addProperty(ActionAnimationProperty.COORD_SET_TICK, null)
                        .addProperty(ActionAnimationProperty.DEST_LOCATION_PROVIDER, MoveCoordFunctions.SYNCHED_TARGET_ENTITY_LOCATION_VARIABLE)
                        .addProperty(AnimationProperty.AttackAnimationProperty.ENTITY_YROT_PROVIDER, MoveCoordFunctions.LOOK_DEST)


                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create((e, s, p) ->
                                e.setGrapplingTarget(e.getOriginal().getLastAttacker()), AnimationEvent.Side.SERVER))

                        .addEvents(AnimationProperty.AttackAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create(((entitypatch, animation, params) -> entitypatch.getOriginal().setInvulnerable(false)), AnimationEvent.Side.SERVER))
                        .addEvents(AnimationEvent.InTimeEvent.create(4.95F, (entitypatch, animation, params) -> {
                            int part_count = 1;
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
                                Vec3 vecpos = getJointWithTranslation(Minecraft.getInstance().player, entitypatch.getOriginal(), new Vec3f(1.5, 0, 0), Armatures.BIPED.get().rootJoint);

                                while (part_count == 1) {
                                    assert vecpos != null;
                                    Particle particle = Minecraft.getInstance().particleEngine.createParticle(
                                            EpicFightParticles.AIR_BURST.get(),
                                            vecpos.x,
                                            vecpos.y,
                                            vecpos.z,
                                            0,
                                            0,
                                            0
                                    );
                                    if (particle != null) {
                                        particle.scale(0.92f);
                                        particle.setLifetime(9);
                                        particle.scale(3.5F);
                                    }
                                    part_count = 0;
                                }

                            }
                        }, AnimationEvent.Side.BOTH))
                        .addProperty(AnimationProperty.AttackAnimationProperty.CANCELABLE_MOVE, false)
//                .addEvents(
//                        AnimationEvent.InTimeEvent.create(0.1F, (entitypatch, animation, params) -> {
//                            LivingEntity grapplingTarget = entitypatch.getGrapplingTarget();
//
//                            if (grapplingTarget != null) {
//                                entitypatch.playSound(EpicFightSounds.BLADE_HIT.get(), 0.0F, 0.0F);
//                            }
//                        }, AnimationEvent.Side.CLIENT)
//                )
                        .addProperty(AnimationProperty.AttackAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)


        );


        ONE_INCH_COUNTER_HIT = builder.nextAccessor("biped/skill/one_inch_counter/one_inch_counter_hit", (accessor) -> new LongHitAnimation(0.12F, accessor, biped)
                .addProperty(AnimationProperty.ActionAnimationProperty.CANCELABLE_MOVE, false)
                .addEvents(AnimationProperty.ActionAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create((
                                (entitypatch, animation, params) -> {
                                    entitypatch.getOriginal().deathTime = 50;
                                    var dmgsrc = entitypatch.getOriginal().damageSources();
                                    entitypatch.getOriginal().hurt(dmgsrc.playerAttack((Player) entitypatch.getOriginal().getLastAttacker()), (float) Math.pow(10, 30));
                                }
                        )
                        , AnimationEvent.Side.SERVER
                ))
                .addEvents(AnimationProperty.ActionAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create(
                        (entitypatch, animation, params) ->
                        {
                            entitypatch.getOriginal().addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY, 225, 5, false, false, false));
                            if (!(entitypatch.getOriginal() instanceof ServerPlayer)) {
                                if (entitypatch.getOriginal() instanceof Mob mob) {
                                    mob.setNoAi(true);
                                }
                            }
                        },
                        AnimationEvent.Side.SERVER
                ))
                .addEvents(AnimationEvent.InTimeEvent.create(5.9F, (entitypatch, animation, params) -> {
                    int part_count = 1;
                    Vec3 vecpos = getJointWithTranslation(Minecraft.getInstance().player, entitypatch.getOriginal(), new Vec3f(1.5, 0, 0), Armatures.BIPED.get().rootJoint);
                    while (part_count == 1) {
                        assert vecpos != null;
                        Particle particle = Minecraft.getInstance().particleEngine.createParticle(
                                EpicFightParticles.AIR_BURST.get(),
                                vecpos.x + 0.1,
                                vecpos.y,
                                vecpos.z,
                                0,
                                0,
                                0
                        );
                        if (particle != null) {
                            particle.scale(0.92f);
                            particle.setLifetime(9);
                            particle.scale(3.5F);
                            particle.setColor(214, 181, 136);
                        }
                        part_count = 0;
                    }


                }, AnimationEvent.Side.BOTH))
                .addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
                .addProperty(AnimationProperty.ActionAnimationProperty.CANCELABLE_MOVE, false)
                .addProperty(AnimationProperty.ActionAnimationProperty.IS_DEATH_ANIMATION, true)
                .addProperty(AnimationProperty.ActionAnimationProperty.FIXED_HEAD_ROTATION, true)
        );

        ONE_INCH_COUNTER_BAIT = builder.nextAccessor("biped/skill/one_inch_counter/one_inch_bait", (accessor) ->
                new ActionAnimation(
                        0.09F,accessor,biped
                )
                        .addProperty(ActionAnimationProperty.CANCELABLE_MOVE, false)
                        .addProperty(ActionAnimationProperty.STOP_MOVEMENT,true)
                        .addState(EntityState.MOVEMENT_LOCKED,true)
                        .addState(EntityState.SKILL_EXECUTABLE,false)
                        .addState(EntityState.HURT_LEVEL,0) // what does this do????
                        .addEvents(ActionAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create((e, s, p) -> {
                            FX menacing = FXHelper.getFX(ResourceLocation.parse("photon:menacingcounter"));
                            Level l = e.getOriginal().level();
                            Entity eo = e.getOriginal();
                            Armature ea = e.getArmature();
                            new JointTrackedEntityEffect(menacing, l, eo, ea.rootJoint, Vec3f.ZERO, EntityEffectExecutor.AutoRotate.NONE, false).start();
                        }, AnimationEvent.Side.CLIENT))

        );

        ONE_INCH_COUNTER_BAIT_FAIL = builder.nextAccessor("biped/skill/one_inch_counter/one_inch_bait_fail", (accessor) -> new ActionAnimation(0.09F, accessor, biped)
                .addProperty(AnimationProperty.ActionAnimationProperty.CANCELABLE_MOVE, false)
        );


    }


}
