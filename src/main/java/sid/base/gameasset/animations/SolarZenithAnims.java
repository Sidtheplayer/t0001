package sid.base.gameasset.animations;

import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Armatures;


public class SolarZenithAnims {

    public static AnimationManager.AnimationAccessor<StaticAnimation> NORMAL_IDLE;

    public static void build(AnimationManager.AnimationBuilder builder) {

        NORMAL_IDLE = builder.nextAccessor("biped/living/solar_idle", ac ->
                new StaticAnimation(0.51f, true, ac, Armatures.BIPED)
                );


    }


}
