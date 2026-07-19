package sid.base.gameasset.animations;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.InteractionHand;
import sid.base.gameasset.animations.collider.CGSColliderPresets;
import sid.base.gameasset.animations.types.TitleCardAttackAnimation;
import sid.base.utils.ReusableAnimEvents;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.*;
import yesman.epicfight.api.utils.EntitySnapshot;
import yesman.epicfight.api.utils.TimePairList;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.client.particle.EntityAfterimageParticle;
import yesman.epicfight.gameasset.Animations; //ref
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;

import java.util.Set;



public class DragonGodSwordAnimations {


    public static AnimationManager.AnimationAccessor<StaticAnimation> DGS_IDLE;
    public static AnimationManager.AnimationAccessor<MovementAnimation> DGS_RUN;



    // Todo: 4 parry animations and 1 custom parry break animation
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



    public static void build(AnimationManager.AnimationBuilder builder){
        Armatures.ArmatureAccessor<HumanoidArmature> biped = Armatures.BIPED;
        Joint toolR = biped.get().toolR;

        DGS_IDLE = builder.nextAccessor("biped/living/dragon_god_sword_hold", (accessor) -> new StaticAnimation(true,accessor,biped)
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

//        DGS_AUTO_1 = builder.nextAccessor("biped/dgs/dragon_god_sword_auto1" , ac -> new AvalonAttackAnimation(0.01F,ac,biped,1.0F,1.2F,createSimplePhase(23,30,35,
//                InteractionHand.MAIN_HAND,biped.get().toolR, null)));
//
//        DGS_AUTO_2 = builder.nextAccessor("biped/dgs/dragon_god_sword_auto2",ac -> new AvalonAttackAnimation(0.15f,ac,biped,1.0F,1.3f,
//                createSimplePhase(23,33,36,InteractionHand.MAIN_HAND,
//                biped.get().toolR,null))
//                .damageBlock()
//        );

        DGS_AUTO_1 = builder.nextAccessor("biped/dgs/dragon_god_sword_auto1", ac -> new ComboAttackAnimation(0.01f, 0.20f, 0.45F, 1.5f, InteractionHand.MAIN_HAND, null, biped.get().toolR, ac, biped)
                .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED,0.55F)
        );

        DGS_AUTO_2 = builder.nextAccessor("biped/dgs/dragon_god_sword_auto2", ac -> new ComboAttackAnimation(0.0f, 0.22f, 0.42F, 1.5f, InteractionHand.MAIN_HAND, null, biped.get().toolR, ac, biped)
                .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED,0.9F)
        );

        DGS_AUTO_1P2 = builder.nextAccessor("biped/dgs/dragon_god_sword_auto1plus2", ac -> new AttackAnimation(0.0f, 0.2f, 0.65f, 2.5f , 30f,InteractionHand.MAIN_HAND, null, biped.get().toolR,ac,biped)
                .addProperty(AnimationProperty.AttackAnimationProperty.PLAY_SPEED_MODIFIER, (s,p,r,f,g) -> 2.69f)
                .addProperty(AnimationProperty.AttackAnimationProperty.EXTRA_COLLIDERS,5)
                .addProperty(AnimationProperty.AttackAnimationProperty.FIXED_HEAD_ROTATION,true)
        );

        DGS_UN_IN1 = builder.nextAccessor("biped/dgs/un_in/dgsaw1", ac->
                new AttackAnimation(0.65f,0.195f,0.2f,0.5f,50f,null,toolR,ac,biped));

        DGS_UN_IN2 = builder.nextAccessor("biped/dgs/un_in/dgsaw2", ac->
                new AttackAnimation(0.01f,0.195f,0.2f,0.5f,50f,null,toolR,ac,biped));

        DGS_UN_IN3 = builder.nextAccessor("biped/dgs/un_in/dgsaw3", ac->
                new AttackAnimation(0.01f,0.195f,0.2f,0.5f,50f,null,toolR,ac,biped));

        DGS_UN_IN4 = builder.nextAccessor("biped/dgs/un_in/dgsaw4", ac->
                new AttackAnimation(0.01f,0.195f,0.2f,0.5f,50f,null,toolR,ac,biped)
                        .addProperty(AnimationProperty.AttackAnimationProperty.MOVE_VERTICAL,true)
                        .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER,ReusableAnimEvents.ONE25PERCENT)
                        .addProperty(AnimationProperty.AttackAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0f,20f))
                        .addEvents(AnimationEvent.InTimeEvent.create(0.46f,
                                (e,s,p)->{
                                    if (e == null) {
                                        return;
                                    }

                                    EntitySnapshot<?> snapshot =
                                            e.captureEntitySnapshot();

                                    if (snapshot == null) {
                                        return;
                                    }

                                    EntityAfterimageParticle particle =
                                            new EntityAfterimageParticle(
                                                    (ClientLevel) e.getLevel(),
                                                    snapshot.getPosition().x,
                                                    snapshot.getPosition().y,
                                                    snapshot.getPosition().z,
                                                    0.0D,
                                                    0.0D,
                                                    0.0D,
                                                    snapshot,
                                                    afterimage -> {
                                                        afterimage.setColor(
                                                                0.2F,
                                                                0.9F,
                                                                1.0F
                                                        );
                                                    }
                                            );

                                    particle.setLifetime(24);

                                    Minecraft.getInstance()
                                            .particleEngine
                                            .add(particle);


                                }, AnimationEvent.Side.CLIENT
                        ))

        );


        TOO_EASY_RUN = builder.nextAccessor("biped/dgs/tooeasyrun",ac ->
                new InvincibleAnimation(0.2f,ac,biped)
                        .addState(EntityState.TURNING_LOCKED,false)
                        .addState(EntityState.MOVEMENT_LOCKED, false)
                        .addState(EntityState.SKILL_EXECUTABLE,true)
                        .addState(EntityState.INACTION,true)
                        .addProperty(AnimationProperty.ActionAnimationProperty.MOVE_ON_LINK,false)
                        .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)

        );

        TOO_EASY_STRIKE = builder.nextAccessor("biped/dgs/tooeasystrike",(accessor)->
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
                       // .addProperty(AnimationProperty.AttackPhaseProperty.HIT_PRIORITY, HitEntityList.Priority.TARGET)
                        .addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER,ValueModifier.setter(100f))
                        .addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.BYPASS_DODGE))

                        .addProperty(AnimationProperty.AttackAnimationProperty.EXTRA_COLLIDERS,18 * 2)
//                        .addProperty(AnimationProperty.AttackAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.RAW_COORD)
//                        .addProperty(AnimationProperty.AttackAnimationProperty.COORD_SET_TICK, null)
                        .addProperty(AnimationProperty.AttackAnimationProperty.FIXED_MOVE_DISTANCE, true)
                        .addProperty(AnimationProperty.AttackAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
        );

    }




}
