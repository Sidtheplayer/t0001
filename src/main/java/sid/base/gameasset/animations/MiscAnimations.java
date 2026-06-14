package sid.base.gameasset.animations;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.player.Player;
import sid.base.client.events.CameraAnimator;
import sid.base.gameasset.ReusableEventsAndUtils;
import sid.base.utils.ReusableAnimEvents;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.EmoteAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.LongHitAnimation;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;

public class MiscAnimations {

    public static AnimationManager.AnimationAccessor<EmoteAnimation> ALLOW_ME;

    public static AnimationManager.AnimationAccessor<EmoteAnimation> WHOISTHISGUY;

    public static AnimationManager.AnimationAccessor<LongHitAnimation> RAG_DOLL_UP_HIGH;
    public static AnimationManager.AnimationAccessor<LongHitAnimation> RAG_DOLL_STUN_UP;
    public static AnimationManager.AnimationAccessor<LongHitAnimation> RAG_DOLL_BACK;



    public static void build(AnimationManager.AnimationBuilder builder){

        Armatures.ArmatureAccessor<HumanoidArmature> biped = Armatures.BIPED;

        //Generated rag dolls from blender
        RAG_DOLL_BACK = builder.nextAccessor("biped/simulated/funny_ragdoll", ac->
                new LongHitAnimation(0.2f,ac,biped)
                        .addState(EntityState.ATTACK_RESULT , damageSource ->
                                damageSource.is(EpicFightDamageTypeTags.FINISHER) || damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) ? AttackResult.ResultType.SUCCESS : AttackResult.ResultType.MISSED
                        )
                        .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, ReusableAnimEvents.ONE50PERCENT)
        );

        RAG_DOLL_UP_HIGH = builder.nextAccessor("biped/simulated/ragdoll_parabola_u", ac->
                new LongHitAnimation(0.1f,ac,biped)
                        .addState(EntityState.ATTACK_RESULT , damageSource ->
                                damageSource.is(EpicFightDamageTypeTags.FINISHER) || damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) ? AttackResult.ResultType.SUCCESS : AttackResult.ResultType.MISSED
                        )
                        .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
        );

        RAG_DOLL_STUN_UP = builder.nextAccessor("biped/simulated/ragdoll_stun", ac->
                new LongHitAnimation(0.1f,ac,biped)
                        .addState(EntityState.ATTACK_RESULT , damageSource ->
                            damageSource.is(EpicFightDamageTypeTags.FINISHER) || damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) ? AttackResult.ResultType.SUCCESS : AttackResult.ResultType.MISSED
                        )
                        .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
        );


        ALLOW_ME = builder.nextAccessor("biped/emote/allowme", emoteAnimationAnimationAccessor ->
                new EmoteAnimation(0.0F, false, emoteAnimationAnimationAccessor, biped)
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, AnimationEvent.Side.CLIENT))
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, AnimationEvent.Side.CLIENT))
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create(((entitypatch, animation, params) -> {
                                    ReusableEventsAndUtils.sendBypassedChatMessage(entitypatch, "You call that Kung fu? Allow me");
                                }), AnimationEvent.Side.SERVER)
                        ));


        WHOISTHISGUY = builder.nextAccessor("biped/emote/who_is_this_guy",accessor ->
                new EmoteAnimation(-0.69F,false,accessor,biped)
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create((e, s, p) ->
                                        {
                                            if(!(e.getOriginal() instanceof Player player))return;
                                            if(!player.isCreative())return;
                                            System.out.println("[TEST] Animation event triggered!");

                                            CameraAnimator.getInstance().playWithOption("test", false, false);

                                        }
                                        , AnimationEvent.Side.LOCAL_CLIENT)
                        )
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, AnimationEvent.Side.CLIENT))
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, AnimationEvent.Side.CLIENT))

        );



    }
}
