package sid.base.gameasset.animations;

//import com.merlin204.avalon.api.collider.AvalonColliderUtil;
//import com.merlin204.avalon.epicfight.animations.AvalonAttackAnimation;
//import com.merlin204.avalon.util.AvalonAnimationUtils;
//import com.merlin204.avalon.util.AvalonEventUtils;
//import com.merlin204.avalon.util.AvalonSyncUtils;
import net.minecraft.world.InteractionHand;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.*;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.model.armature.HumanoidArmature;

//import static com.merlin204.avalon.util.AvalonAnimationUtils.createSimplePhase;

public class DragonGodSwordAnimations {


    public static AnimationManager.AnimationAccessor<StaticAnimation> DGS_IDLE;
    public static AnimationManager.AnimationAccessor<MovementAnimation> DGS_RUN;



    // Todo: 4 parry animations and 1 custom parry break animation
    public static AnimationManager.AnimationAccessor<StaticAnimation> GUARD;
    public static AnimationManager.AnimationAccessor<GuardAnimation> GUARD_HIT;
    public static AnimationManager.AnimationAccessor<GuardAnimation> DGS_PARRY;
    public static AnimationManager.AnimationAccessor<GuardAnimation> DGS_PARRY_2;

    //attack anim
    public static AnimationManager.AnimationAccessor<ComboAttackAnimation> DGS_AUTO_1;
    public static AnimationManager.AnimationAccessor<ComboAttackAnimation> DGS_AUTO_2;

    public static AnimationManager.AnimationAccessor<AttackAnimation> DGS_AUTO_1P2;



    public static void build(AnimationManager.AnimationBuilder builder){
        Armatures.ArmatureAccessor<HumanoidArmature> biped = Armatures.BIPED;

        DGS_IDLE = builder.nextAccessor("biped/living/dragon_god_sword_hold", (accessor) -> new StaticAnimation(true,accessor,biped)
                .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE));

        DGS_RUN = builder.nextAccessor("biped/living/dragon_god_sword_hold_run", (accessor) -> new MovementAnimation(true, accessor, biped));



        GUARD = builder.nextAccessor("biped/skill/dragon_god_sword_guard", (accessor) -> new StaticAnimation(0.25F, true, accessor, biped));

        GUARD_HIT = builder.nextAccessor("biped/skill/dragon_god_sword_guard_hit", (accessor) -> new GuardAnimation(0.06F, accessor, biped));

        DGS_PARRY = builder.nextAccessor("biped/skill/dragon_god_sword_parry", (accessor) -> new GuardAnimation(0.06F, accessor, biped));
        DGS_PARRY_2 = builder.nextAccessor("biped/skill/dragon_god_sword_parry_2", (accessor) -> new GuardAnimation(0.06F, accessor, biped));

//        DGS_AUTO_1 = builder.nextAccessor("biped/dgs/dragon_god_sword_auto1" , ac -> new AvalonAttackAnimation(0.01F,ac,biped,1.0F,1.2F,createSimplePhase(23,30,35,
//                InteractionHand.MAIN_HAND,biped.get().toolR, CGSColliderPresets.DRAGON_GOD_SWORD_COLLIDER)));
//
//        DGS_AUTO_2 = builder.nextAccessor("biped/dgs/dragon_god_sword_auto2",ac -> new AvalonAttackAnimation(0.15f,ac,biped,1.0F,1.3f,
//                createSimplePhase(23,33,36,InteractionHand.MAIN_HAND,
//                biped.get().toolR,CGSColliderPresets.DRAGON_GOD_SWORD_COLLIDER))
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

    }




}
