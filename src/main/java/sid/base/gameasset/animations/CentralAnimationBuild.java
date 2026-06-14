package sid.base.gameasset.animations;

import yesman.epicfight.api.animation.AnimationManager;

public class CentralAnimationBuild {

    // Made like this to avoid clutter with usage of single animation class for everything
    public static void listen(AnimationManager.AnimationBuilder builder){
        DragonGodSwordAnimations.build(builder);
        t0001Animations.build(builder);
        UltimateAnimations.build(builder);
        MiscAnimations.build(builder);
    }
    //--Now you need to register "listen" in main mod class--

}
