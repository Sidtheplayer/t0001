package sid.t0001.gameasset.animations;

import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.GuardAnimation;
import yesman.epicfight.api.animation.types.MovementAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.model.armature.HumanoidArmature;

public class DragonGodSwordAnimations {


    public static AnimationManager.AnimationAccessor<StaticAnimation> DGS_IDLE;
    public static AnimationManager.AnimationAccessor<MovementAnimation> DGS_RUN;




    public static AnimationManager.AnimationAccessor<StaticAnimation> GUARD;
    public static AnimationManager.AnimationAccessor<GuardAnimation> GUARD_HIT;


    public static void build(AnimationManager.AnimationBuilder builder){
        Armatures.ArmatureAccessor<HumanoidArmature> biped = Armatures.BIPED;

        DGS_IDLE = builder.nextAccessor("biped/living/dragon_god_sword_hold", (accessor) -> new StaticAnimation(true,accessor,biped)
                .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE));

        DGS_RUN = builder.nextAccessor("biped/living/dragon_god_sword_hold_run", (accessor) -> new MovementAnimation(true, accessor, biped));



        GUARD = builder.nextAccessor("biped/skill/dragon_god_sword_guard", (accessor) -> new StaticAnimation(0.27F, true, accessor, biped));

        GUARD_HIT = builder.nextAccessor("biped/skill/dragon_god_sword_guard_hit", (accessor) -> new GuardAnimation(0.02F, accessor, biped));


    }




}
