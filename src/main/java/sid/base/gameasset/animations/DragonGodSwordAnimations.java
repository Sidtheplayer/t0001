package sid.base.gameasset.animations;

import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import sid.base.client.photon.executor.FixedXROTPointFXExecutor;
import sid.base.gameasset.ReusableEventsAndUtils;
import sid.base.gameasset.animations.collider.CGSColliderPresets;
import sid.base.gameasset.animations.types.TitleCardAttackAnimation;
import sid.base.network.ClientSideDelegations;
import sid.base.utils.ReusableAnimEvents;
import sid.base.world.ExtraSpecialDamageTypeTags;
import sid.base.world.t0001Sounds;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.*;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.HitEntityList;
import yesman.epicfight.api.utils.TimePairList;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.gameasset.Animations; //ref
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;

import java.util.Set;

import static sid.base.gameasset.ReusableEventsAndUtils.getAnimTimeFromFrame;


public class DragonGodSwordAnimations {


    public static AnimationManager.AnimationAccessor<StaticAnimation> DGS_IDLE;
    public static AnimationManager.AnimationAccessor<MovementAnimation> DGS_RUN;


    // Todo: 1 custom parry break animation
    public static AnimationManager.AnimationAccessor<StaticAnimation> GUARD;
    public static AnimationManager.AnimationAccessor<GuardAnimation> GUARD_HIT;
    public static AnimationManager.AnimationAccessor<GuardAnimation> DGS_PARRY;
    public static AnimationManager.AnimationAccessor<GuardAnimation> DGS_PARRY_2;
    public static AnimationManager.AnimationAccessor<GuardAnimation> DGS_PARRY_3;
    public static AnimationManager.AnimationAccessor<GuardAnimation> DGS_PARRY_4;

    //attack anim -- these auto anims are going to be replaced
    public static AnimationManager.AnimationAccessor<ComboAttackAnimation> DGS_AUTO_1;
    public static AnimationManager.AnimationAccessor<ComboAttackAnimation> DGS_AUTO_2;

    public static AnimationManager.AnimationAccessor<AttackAnimation> DGS_AUTO_1P2;

    public static AnimationManager.AnimationAccessor<AttackAnimation> DGS_UN_IN1;
    public static AnimationManager.AnimationAccessor<AttackAnimation> DGS_UN_IN2;
    public static AnimationManager.AnimationAccessor<AttackAnimation> DGS_UN_IN3;
    public static AnimationManager.AnimationAccessor<AttackAnimation> DGS_UN_IN4;

    public static AnimationManager.AnimationAccessor<InvincibleAnimation> TOO_EASY_RUN;
    public static AnimationManager.AnimationAccessor<TitleCardAttackAnimation> TOO_EASY_STRIKE;

    public static AnimationManager.AnimationAccessor<InvincibleAnimation> SHINJI_DODGES;

    public static AnimationManager.AnimationAccessor<DashAttackAnimation> DGS_GUARD_BREAKER_DASH;


    public static void build(AnimationManager.AnimationBuilder builder) {
        Armatures.ArmatureAccessor<HumanoidArmature> biped = Armatures.BIPED;
        Joint toolR = biped.get().toolR;

        DGS_IDLE = builder.nextAccessor("biped/living/dragon_god_sword_hold", (accessor) -> new StaticAnimation(true, accessor, biped)
                .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE));

        DGS_RUN = builder.nextAccessor("biped/living/dragon_god_sword_hold_run", (accessor) -> new MovementAnimation(true, accessor, biped));


        GUARD = builder.nextAccessor("biped/skill/dragon_god_sword_guard", (accessor) -> new StaticAnimation(0.25F, true, accessor, biped));

        GUARD_HIT = builder.nextAccessor("biped/skill/dragon_god_sword_guard_hit", (accessor) -> new GuardAnimation(0.06F, accessor, biped));

        DGS_PARRY = builder.nextAccessor("biped/skill/dragon_god_sword_parry_1", (accessor) -> new GuardAnimation(0.06F, accessor, biped)
                .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, ReusableAnimEvents.ONE50PERCENT));

        DGS_PARRY_2 = builder.nextAccessor("biped/skill/dragon_god_sword_parry_2", (accessor) -> new GuardAnimation(0.03F, accessor, biped));

        DGS_PARRY_3 = builder.nextAccessor("biped/skill/dragon_god_sword_parry_3", (accessor) -> new GuardAnimation(0.16F, accessor, biped)
                .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, ReusableAnimEvents.ONE50PERCENT));

        DGS_PARRY_4 = builder.nextAccessor("biped/skill/dragon_god_sword_parry_4", (accessor) -> new GuardAnimation(0.16F, accessor, biped)
                .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, ReusableAnimEvents.ONE50PERCENT));

        DGS_GUARD_BREAKER_DASH = builder.nextAccessor("biped/dgs/dgs_guardbreakingdash", ac ->
                new DashAttackAnimation(0.1f,
                        getAnimTimeFromFrame(27),
                        getAnimTimeFromFrame(63),
                        getAnimTimeFromFrame(90),
                        getAnimTimeFromFrame(130),
                        null,
                        biped.get().toolR,
                        ac,
                        biped
                )
                        .addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, t0001Sounds.SLASH_HIT.value())
                        .addProperty(AnimationProperty.AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH.value())
                        .addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.NONE)
                        .addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.UNBLOCKALBE, EpicFightDamageTypeTags.GUARD_PUNCTURE, ExtraSpecialDamageTypeTags.GUARD_BREAK_S))
                        .addProperty(AnimationProperty.AttackAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
        );


        DGS_AUTO_1 = builder.nextAccessor("biped/dgs/dragon_god_sword_auto1", ac -> new ComboAttackAnimation(0.01f, 0.20f, 0.45F, 1.5f, InteractionHand.MAIN_HAND, null, biped.get().toolR, ac, biped)
                .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 0.55F)
        );

        DGS_AUTO_2 = builder.nextAccessor("biped/dgs/dragon_god_sword_auto2", ac -> new ComboAttackAnimation(0.0f, 0.22f, 0.42F, 1.5f, InteractionHand.MAIN_HAND, null, biped.get().toolR, ac, biped)
                .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 0.9F)
        );

        DGS_AUTO_1P2 = builder.nextAccessor("biped/dgs/dragon_god_sword_auto1plus2", ac -> new AttackAnimation(0.0f, 0.2f, 0.65f, 2.5f, 30f, InteractionHand.MAIN_HAND, null, biped.get().toolR, ac, biped)
                .addProperty(AnimationProperty.AttackAnimationProperty.PLAY_SPEED_MODIFIER, (s, p, r, f, g) -> 2.69f)
                .addProperty(AnimationProperty.AttackAnimationProperty.EXTRA_COLLIDERS, 5)
                .addProperty(AnimationProperty.AttackAnimationProperty.FIXED_HEAD_ROTATION, true)
        );

        DGS_UN_IN1 = builder.nextAccessor("biped/dgs/un_in/dgsaw1", ac ->
                new AttackAnimation(0.65f, 0.195f, 0.2f, 0.5f, 50f, null, toolR, ac, biped)
        );

        DGS_UN_IN2 = builder.nextAccessor("biped/dgs/un_in/dgsaw2", ac ->
                new AttackAnimation(0.01f, 0.195f, 0.2f, 0.5f, 50f, null, toolR, ac, biped)
        );

        DGS_UN_IN3 = builder.nextAccessor("biped/dgs/un_in/dgsaw3", ac ->
                new AttackAnimation(0.01f, 0.195f, 0.2f, 0.5f, 50f, null, toolR, ac, biped)
        );

        DGS_UN_IN4 = builder.nextAccessor("biped/dgs/un_in/dgsaw4", ac ->
                new AttackAnimation(0.01f, 0.195f, 0.2f, 0.5f, 50f, null, toolR, ac, biped)
                        .addProperty(AnimationProperty.AttackAnimationProperty.MOVE_VERTICAL, true)
                        .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, ReusableAnimEvents.ONE25PERCENT)
                        .addProperty(AnimationProperty.AttackAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0f, 20f))
        );


        TOO_EASY_RUN = builder.nextAccessor("biped/dgs/tooeasyrun", ac ->
                new InvincibleAnimation(0.2f, ac, biped)
                        .addState(EntityState.TURNING_LOCKED, false)
                        .addState(EntityState.MOVEMENT_LOCKED, false)
                        .addState(EntityState.SKILL_EXECUTABLE, true)
                        .addState(EntityState.INACTION, true)
                        .addProperty(AnimationProperty.ActionAnimationProperty.MOVE_ON_LINK, false)
                        .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)

        );

        TOO_EASY_STRIKE = builder.nextAccessor("biped/dgs/tooeasystrike", (accessor) ->
                        new TitleCardAttackAnimation(
                                0.1f,
                                0.01f,
                                0.03f,
                                0.95f,
                                Float.MAX_VALUE,
                                InteractionHand.MAIN_HAND,
                                CGSColliderPresets.PHANTOM_SEVERANCE,
                                biped.get().rootJoint,
                                accessor,
                                biped)


                                .addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1f))
                                 .addProperty(AnimationProperty.AttackPhaseProperty.HIT_PRIORITY, HitEntityList.Priority.TARGET)
                                .addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.setter(100f))
                                .addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.BYPASS_DODGE))

                                .addEvents(AnimationEvent.InTimeEvent.create(getAnimTimeFromFrame(35), (e, s, p) -> {
                                    if (!e.getCurrentlyActuallyHitEntities().isEmpty() && e.isLastAttackSuccess()) {
                                        LivingEntity victim = e.getCurrentlyActuallyHitEntities().getFirst();
                                        LivingEntityPatch<?> victimPatch = EpicFightCapabilities.getEntityPatch(victim, LivingEntityPatch.class);
                                        if (victimPatch instanceof ServerPlayerPatch) {
                                            victimPatch.playAnimationSynchronized(SHINJI_DODGES, 0.0f);
                                        } else {
                                            EpicFightDamageSource damageSource = e.getOriginal() instanceof Player enemy ? EpicFightDamageSources.playerAttack(enemy) : EpicFightDamageSources.mobAttack(e.getOriginal());
                                            damageSource.addRuntimeTag(DamageTypeTags.BYPASSES_INVULNERABILITY);

                                            victim.hurt(damageSource, Float.MAX_VALUE);
                                            if (victimPatch instanceof HumanoidMobPatch<?> humanoidMobPatch) {
                                                Mob mob = humanoidMobPatch.getOriginal();
                                                mob.stopInPlace();
                                                victimPatch.playAnimationSynchronized(MiscAnimations.RAG_DOLL_STUN_UP, 0.0f);
                                            }
                                        }
                                    } else e.playAnimationSynchronized(DGS_IDLE, 1.69f);
                                }, AnimationEvent.Side.SERVER))

                                .addProperty(AnimationProperty.AttackAnimationProperty.EXTRA_COLLIDERS, 18 * 2)
                                .addProperty(AnimationProperty.AttackAnimationProperty.FIXED_MOVE_DISTANCE, true)
                                .addProperty(AnimationProperty.AttackAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
        );

        SHINJI_DODGES = builder.nextAccessor("biped/dgs/shinji_dodges", ac ->
                new InvincibleAnimation(0.12f, ac, biped
                )
                        .addEvents(
                                requestPhantomQuickTime(50),
                                requestPhantomQuickTime(120),
                                requestPhantomQuickTime(150),
                                requestPhantomQuickTime(180),
                                requestPhantomQuickTime(215)
                        )
                        .addEvents( AnimationEvent.InTimeEvent.create(getAnimTimeFromFrame(6),
                                (e, s, p) -> {
                                    FX fx = FXHelper.getFX(ResourceLocation.parse("photon:phantom_realm"));
                                    Vec3 spawnPos = ReusableEventsAndUtils.JointTrack.getjointpos(e.getOriginal(),e.getArmature().rootJoint, new Vec3f(-0.0,0.0,0 ));
                                    if(spawnPos == null)return;

                                    var pointEffect = new FixedXROTPointFXExecutor(fx, e.getLevel(), spawnPos.toVector3f(), e.getOriginal());
                                    pointEffect.setOffset(0,0,0);
                                    pointEffect.setRotation(0,0,0);
                                    pointEffect.setScale(1,1,1);
                                    pointEffect.setAllowMulti(true);
                                    pointEffect.setForcedDeath(false);
                                    pointEffect.setDelay(0);
                                    pointEffect.start();

                                    if (pointEffect.getRuntime() != null) {
                                        ReusableAnimEvents.putRuntime(e.getId(), "photon:phantom_realm", pointEffect.getRuntime());
                                    }


                                }, AnimationEvent.Side.LOCAL_CLIENT)
                        )
                        .addState(EntityState.TURNING_LOCKED, true)
                        .addState(EntityState.MOVEMENT_LOCKED, true)
                        .addState(EntityState.UPDATE_LIVING_MOTION, false)
                        .addState(EntityState.COMBO_ATTACKS_DOABLE, false)
                        .addState(EntityState.SKILL_EXECUTABLE, false)
                        .addState(EntityState.INACTION, true)
                        .addState(EntityState.ATTACK_RESULT, src -> src.is(DamageTypeTags.BYPASSES_INVULNERABILITY) ? AttackResult.ResultType.SUCCESS : AttackResult.ResultType.MISSED)
        );

    }

    private static AnimationEvent.@NotNull InTimeEvent<AnimationEvent.Event<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> requestPhantomQuickTime(int BlenderFrame) {
        return AnimationEvent.InTimeEvent.create(getAnimTimeFromFrame(BlenderFrame), (e, s, p) -> {
            int keyCode = GLFW.GLFW_KEY_SPACE;

            float time = e.getOriginal().getRandom().nextBoolean() ? 16f : 13.5f;

            ClientSideDelegations.addQuickTime(keyCode, time, "phantom_severance");

        }, AnimationEvent.Side.LOCAL_CLIENT);
    }


}
