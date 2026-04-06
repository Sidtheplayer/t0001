package sid.base.world.capabilities.item;

import yesman.epicfight.api.ex_cap.modules.core.events.ExCapBuilderCreationEvent;
import yesman.epicfight.api.ex_cap.modules.core.events.ExCapDataRegistrationEvent;
import yesman.epicfight.api.ex_cap.modules.core.events.ExCapMovesetRegistryEvent;
import yesman.epicfight.api.ex_cap.modules.core.events.ExCapabilityBuilderPopulationEvent;

public class ExCapEventHooks
{
    public static void onRegisterWeaponBuilder(ExCapBuilderCreationEvent event)
    {
        event.addBuilder(ExCapBuilders.DRAGON_GOD_SWORD);
    }

    public static void onRegisterDataSet(ExCapDataRegistrationEvent event)
    {
        event.addData(ExCapDataSets.DRAGON_GOD_SWORD);
    }

    public static void onRegisterMoveset(ExCapMovesetRegistryEvent event)
    {
        event.addMoveSet(ExCapMovesets.DRAGON_GOD_SWORD_NORMAL);
    }

    public static void onPopulateData(ExCapabilityBuilderPopulationEvent event)
    {
        event.registerData(ExCapBuilders.DRAGON_GOD_SWORD.id(), ExCapDataSets.DRAGON_GOD_SWORD.id());
    }
}
