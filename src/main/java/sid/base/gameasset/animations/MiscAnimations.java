package sid.base.gameasset.animations;

import sid.base.gameasset.ReusableEventsAndUtils;
import sid.base.utils.ReusableAnimEvents;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.property.MoveCoordFunctions;
import yesman.epicfight.api.animation.types.EmoteAnimation;
import yesman.epicfight.api.animation.types.LongHitAnimation;
import yesman.epicfight.api.utils.TimePairList;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.model.armature.HumanoidArmature;

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
                new LongHitAnimation(0.2f, ac, biped)
                        .addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.2f,0.9f))
                        .addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL,true)
                        .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, ReusableAnimEvents.ONE50PERCENT)
        );

        RAG_DOLL_UP_HIGH = builder.nextAccessor("biped/simulated/ragdoll_parabola_u", ac->
                new LongHitAnimation(0.1f, ac, biped)
                        .addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.0f,2.90f))
                        .addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.RAW_COORD)
                        .addProperty(AnimationProperty.ActionAnimationProperty.COORD_GET, MoveCoordFunctions.MODEL_COORD)
                        .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, ReusableAnimEvents.DOUBLE)
        );

        RAG_DOLL_STUN_UP = builder.nextAccessor("biped/simulated/ragdoll_stun", ac->
                new LongHitAnimation(0.1f, ac, biped)
                        .addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.0f,1.50f))
                        .addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL,true)
                        .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, ReusableAnimEvents.ONE25PERCENT)
        );


        ALLOW_ME = builder.nextAccessor("biped/emote/allowme", emoteAnimationAnimationAccessor ->
                new EmoteAnimation(0.0F, false, emoteAnimationAnimationAccessor, biped)
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, AnimationEvent.Side.CLIENT))
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, AnimationEvent.Side.CLIENT))
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create(((entitypatch, animation, params) -> {
                                    ReusableEventsAndUtils.sendBypassedChatMessage(entitypatch, "You call that Kung fu? Allow me");
                                }), AnimationEvent.Side.BOTH)
                        ));


        WHOISTHISGUY = builder.nextAccessor("biped/emote/who_is_this_guy",accessor ->
                new EmoteAnimation(-0.69F,false,accessor,biped)
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, AnimationEvent.Side.CLIENT))
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, AnimationEvent.Side.CLIENT))

        );



    }
}
