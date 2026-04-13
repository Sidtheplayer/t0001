package sid.base.gameasset.animations;

import net.minecraft.world.entity.player.Player;
import sid.base.client.events.CameraAnimator;
import sid.base.gameasset.ReusableEvents;
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
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create((e, s, p) ->
                                        {
                                            if(!(e.getOriginal() instanceof Player))return;
                                            System.out.println("[TEST] Animation event triggered!");
                                            CameraAnimator.getInstance().setFollowPlayer(false);
                                            CameraAnimator.getInstance().setRotateWithPlayer(true);

                                            CameraAnimator.getInstance().play("test");

                                        }
                                        , AnimationEvent.Side.LOCAL_CLIENT)
                        )
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, AnimationEvent.Side.CLIENT))
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, AnimationEvent.Side.CLIENT))
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create(((entitypatch, animation, params) -> {
                                    ReusableEvents.sendBypassedChatMessage(entitypatch, "You call that Kung fu? Allow me");
                                }), AnimationEvent.Side.SERVER)
                        ));


        WHOISTHISGUY = builder.nextAccessor("biped/emote/who_is_this_guy",accessor ->
                new EmoteAnimation(-0.69F,false,accessor,biped)
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create((e, s, p) ->
                                        {
                                            if(!(e.getOriginal() instanceof Player))return;
                                            System.out.println("[TEST] Animation event triggered!");
                                            CameraAnimator.getInstance().setFollowPlayer(true);
                                            CameraAnimator.getInstance().setRotateWithPlayer(true);

                                            CameraAnimator.getInstance().play("test");

                                        }
                                        , AnimationEvent.Side.LOCAL_CLIENT)
                        )
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, AnimationEvent.Side.CLIENT))
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, AnimationEvent.Side.CLIENT))

        );



    }
}
