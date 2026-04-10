package sid.base.world.capabilities.item;

import sid.base.gameasset.animations.t0001Animations;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.ex_cap.modules.core.events.*;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import static yesman.epicfight.api.ex_cap.modules.assets.Movesets.glove;

public class ExCapEventHooks
{
    public static void onRegisterWeaponBuilder(ExCapBuilderCreationEvent event)
    {
        event.addBuilder(ExCapBuilders.DRAGON_GOD_SWORD);
    }

    public static void onRegisterDataSet(ExCapDataRegistrationEvent event)
    {
        event.addData(ExCapDataSets.DRAGON_GOD_SWORD);
        yesman.epicfight.api.ex_cap.modules.assets.ExCapDataSets.FIST.data().addMoveset(CapabilityItem.Styles.OCHS,ExCapMovesets.amatuerKicker.id());
        yesman.epicfight.api.ex_cap.modules.assets.ExCapDataSets.FIST.data().addConditional(ExCapConditionals.AmatuerKicker.id());
    }

    public static void onRegisterMoveset(ExCapMovesetRegistryEvent event) {
        glove.builder().addComboAttacks(t0001Animations.SWEEP, Animations.FIST_DASH,
                Animations.FIST_AIR_SLASH);
        glove.builder().addLivingMotionModifier(LivingMotions.BLOCK, t0001Animations.UNARMEDBLOCKFULL);

        event.addMoveSet(ExCapMovesets.DRAGON_GOD_SWORD_NORMAL,
                ExCapMovesets.DRAGON_GOD_SWORD_AWAKENED,
                ExCapMovesets.amatuerKicker);

    }

    public static void onPopulateData(ExCapabilityBuilderPopulationEvent event)
    {
        event.registerData(ExCapBuilders.DRAGON_GOD_SWORD.id(), ExCapDataSets.DRAGON_GOD_SWORD.id());
    }

    public static void onRegisterConditional(ConditionalRegistryEvent event){
        event.addConditional(
                ExCapConditionals.DRAGON_GOD_SWORD_AWAKENED,
                ExCapConditionals.AmatuerKicker
        );
    }


}
