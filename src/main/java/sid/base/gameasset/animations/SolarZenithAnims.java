package sid.base.gameasset.animations;

import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.MovementAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Armatures;

import static sid.base.gameasset.animations.UltimateAnimations.biped;


public class SolarZenithAnims {

    public static AnimationManager.AnimationAccessor<StaticAnimation> NORMAL_IDLE;

    public static AnimationManager.AnimationAccessor<MovementAnimation> RUN_AWKWARD;

    public static void build(AnimationManager.AnimationBuilder builder) {

        NORMAL_IDLE = builder.nextAccessor("biped/living/solar_idle", ac ->
                new StaticAnimation(0.51f, true, ac, Armatures.BIPED)
                );

        RUN_AWKWARD = builder.nextAccessor("biped/living/solar_run_top", ac ->
                new MovementAnimation(0.2f, true, ac, biped)

                );

    }


}
