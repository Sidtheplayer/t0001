package sid.t0001.gameasset.animations;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import reascer.wom.gameasset.WOMSounds;
import sid.t0001.utils.CGSColliderPresets;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.utils.HitEntityList;
import yesman.epicfight.api.utils.TimePairList;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.effect.EpicFightMobEffects;

import javax.swing.*;
import java.util.Set;

import static sid.t0001.gameasset.ReusableEvents.JointTrack.getJointWithTranslation;

public class UltimateAnimations {
    public static AnimationManager.AnimationAccessor<AttackAnimation> ONE_INCH_COUNTER;
    public static AnimationManager.AnimationAccessor<ActionAnimation> ONE_INCH_COUNTER_HIT;


    public static void build(AnimationManager.AnimationBuilder builder) {
        Armatures.ArmatureAccessor<HumanoidArmature> biped = Armatures.BIPED;


        ONE_INCH_COUNTER = builder.nextAccessor("biped/skill/one_inch_counter/one_inch_counter", (accessor) -> new AttackAnimation(0.01F, accessor, biped
                        ,
                        new AttackAnimation.Phase(0.01F, 1.2F, 4.8F, 4.99F, 1.0F, 5.1F,
                                biped.get().rootJoint, CGSColliderPresets.ONE_INCH_COUNTER)
                                .addProperty(AnimationProperty.AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH.get())
                                .addProperty(AnimationProperty.AttackPhaseProperty.HIT_PRIORITY, HitEntityList.Priority.DISTANCE)
                                .addProperty(AnimationProperty.AttackPhaseProperty.PARTICLE, EpicFightParticles.AIR_BURST)
                                .addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.LONG)
                                .addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.UNBLOCKALBE, EpicFightDamageTypeTags.COUNTER))
                                .addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.setter((float) Math.pow(10, 3)))

                )
                .addProperty(AnimationProperty.AttackAnimationProperty.FIXED_HEAD_ROTATION, true)
                .addState(EntityState.PHASE_LEVEL, 3)
                .addEvents(AnimationProperty.AttackAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create(((entitypatch, animation, params) -> entitypatch.getOriginal().setInvulnerable(false)), AnimationEvent.Side.SERVER))
                .addEvents(AnimationEvent.InTimeEvent.create(4.9F, (entitypatch, animation, params) -> {
                    int part_count = 1;
                    if (entitypatch.isLastAttackSuccess()) {
                        LivingEntity entity = entitypatch.getOriginal();
                        BlockPos blockpos = new BlockPos((int) entitypatch.getOriginal().getX(), (int) entitypatch.getOriginal().getY(), (int) entitypatch.getOriginal().getZ());
                        entity.level().playSound(null,blockpos,
                                EpicFightSounds.LASER_BLAST.get(),
                                SoundSource.PLAYERS,
                                1.0F,
                                0.45F
                        );
                        entity.level().playSound(null,blockpos,
                                WOMSounds.SOLAR_HIT.get(),
                                SoundSource.PLAYERS,
                                1.0F,
                                0.75F
                        );
                        Vec3 vecpos = getJointWithTranslation(Minecraft.getInstance().player, entitypatch.getOriginal(), new Vec3f(1.5, 0, 0), Armatures.BIPED.get().rootJoint);

                        while (part_count==1) {
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
                            }
                            part_count=0;
                        }

                    }
                }, AnimationEvent.Side.BOTH))
                .addProperty(AnimationProperty.AttackAnimationProperty.CANCELABLE_MOVE, false)
                .addProperty(AnimationProperty.AttackAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)


        );



        ONE_INCH_COUNTER_HIT = builder.nextAccessor("biped/skill/one_inch_counter/one_inch_counter_hit", (accessor) -> new ActionAnimation(0.12F, accessor,biped)
                .addProperty(AnimationProperty.ActionAnimationProperty.CANCELABLE_MOVE,false)
                .addEvents(AnimationProperty.ActionAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create((
                                (entitypatch, animation, params) -> {
                                 entitypatch.getOriginal().deathTime = 50;
                                 var dmgsrc = entitypatch.getOriginal().damageSources();
                                 entitypatch.getOriginal().hurt(dmgsrc.generic(), (float) Math.pow(10, 30));
                                }
                        )
                        , AnimationEvent.Side.SERVER
                ))
                .addEvents(AnimationProperty.ActionAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create(
                        (entitypatch, animation, params) ->
                        {   entitypatch.getOriginal().addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY.get(), 225, 5, false, false, false));
                            if(!(entitypatch.getOriginal() instanceof ServerPlayer)){
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
                        while (part_count==1) {
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
                                particle.setColor(214,181,136);
                            }
                            part_count=0;
                        }


                }, AnimationEvent.Side.BOTH))
                .addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL,true)
                .addState(EntityState.TURNING_LOCKED,true)
                .addState(EntityState.INACTION,true)
                .addState(EntityState.CAN_SKILL_EXECUTION,false)
                .addState(EntityState.ATTACKING,false)
                .addState(EntityState.CAN_BASIC_ATTACK,false)
                .addState(EntityState.CAN_SWITCH_HAND_ITEM,false)
                .addState(EntityState.CAN_USE_ITEM,false)
                .addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(5.1F, 7.5F))
                .addProperty(AnimationProperty.ActionAnimationProperty.FIXED_HEAD_ROTATION, true)
        );
    }
}
