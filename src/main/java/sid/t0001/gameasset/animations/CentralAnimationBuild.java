package sid.t0001.gameasset.animations;

import sid.t0001.gameasset.t0001Animations;
import yesman.epicfight.api.animation.AnimationManager;

public class CentralAnimationBuild {

    // Made like this to avoid clutter with usage of single animation class for everything
    public static void listen(AnimationManager.AnimationBuilder builder){
        DragonGodSwordAnimations.build(builder);
        t0001Animations.build(builder);
    }

}
