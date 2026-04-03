package sid.base.gameasset.animations;

import sid.base.utils.ReusableAnimEvents;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.EmoteAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.model.armature.HumanoidArmature;

public class EmoteAnimations {

    public static AnimationManager.AnimationAccessor<EmoteAnimation> ALLOW_ME;

    public static AnimationManager.AnimationAccessor<EmoteAnimation> WHOISTHISGUY;



    public static void build(AnimationManager.AnimationBuilder builder){

        Armatures.ArmatureAccessor<HumanoidArmature> biped = Armatures.BIPED;


        ALLOW_ME = builder.nextAccessor("biped/emote/allowme", emoteAnimationAnimationAccessor ->
                new EmoteAnimation(0.0F, false, emoteAnimationAnimationAccessor, biped)
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, AnimationEvent.Side.CLIENT))
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, AnimationEvent.Side.CLIENT))
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create(((entitypatch, animation, params) -> {
                                    ReusableAnimEvents.sendBypassedChatMessage(entitypatch, "You call that Kung fu? Allow me");
                                }), AnimationEvent.Side.BOTH)
                        ));


        WHOISTHISGUY = builder.nextAccessor("biped/emote/who_is_this_guy",accessor ->
                new EmoteAnimation(-0.69F,false,accessor,biped)
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, AnimationEvent.Side.CLIENT))
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, AnimationEvent.Side.CLIENT))

        );



    }
}
