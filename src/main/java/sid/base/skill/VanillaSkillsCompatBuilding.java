package sid.base.skill;


import net.neoforged.bus.api.IEventBus;
import sid.base.gameasset.animations.DragonGodSwordAnimations;
import sid.base.world.capabilities.t0001WeaponCategories;
import sid.base.world.item.t0001Items;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.registry.SkillBuilderModificationEvent;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.gameasset.Animations;

import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.skill.guard.GuardSkill;
import yesman.epicfight.skill.passive.SwordmasterSkill;

import java.util.List;



//Needs to be loaded in main mod class
public class VanillaSkillsCompatBuilding implements ICompatModule{

    public static void onGuardSkillCreation(SkillBuilderModificationEvent event) {

        if (event.getRegistryName().equals(EpicFightSkills.GUARD.getId())) {
            if ((event.getSkillBuilder() instanceof GuardSkill.Builder builder)) {
                builder
                        .addGuardMotion(
                                t0001WeaponCategories.DRAGON_GOD_SWORD,
                                (item, player) -> DragonGodSwordAnimations.GUARD_HIT
                        )
                        .addGuardBreakMotion(
                                t0001WeaponCategories.DRAGON_GOD_SWORD,
                                (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED
                        );
            }
        }
    }



    public static void onParrySkillCreation(SkillBuilderModificationEvent evt) {

        if (evt.getRegistryName().equals(EpicFightSkills.PARRYING.getId())) {
            if (evt.getSkillBuilder() instanceof GuardSkill.Builder builder) {

                builder
                        .addGuardMotion(t0001WeaponCategories.DRAGON_GOD_SWORD, (item, player) -> DragonGodSwordAnimations.GUARD_HIT)
                        .addGuardBreakMotion(t0001WeaponCategories.DRAGON_GOD_SWORD, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED)
                        .addAdvancedGuardMotion(t0001WeaponCategories.DRAGON_GOD_SWORD, (item, player) ->
                                List.of(DragonGodSwordAnimations.PARRY_SCRAP_1, DragonGodSwordAnimations.PARRY_SCRAP_2));

                //will add parry motion later
                // because there will be more than 2-3 parry motions
                // and special parry motions for projectiles
            }

        }

    }
    


    public static void onSwordMasterSkillCreation(SkillBuilderModificationEvent evt) {

        if (evt.getRegistryName().equals(EpicFightSkills.SWORD_MASTER.getId())) {
            if (evt.getSkillBuilder() instanceof SwordmasterSkill.Builder builder) {
                builder.addAvailableWeaponCategory(t0001WeaponCategories.DRAGON_GOD_SWORD);
            }
        }

    }




    @Override
    public void onModEventBus(IEventBus eventBus) {

    }

    @Override
    public void onGameEventBus(IEventBus eventBus) {
        //make sure guard is on higher priority or only guard wont work properly
        EpicFightEventHooks.Registry.MODIFY_SKILL_BUILDER.registerEvent(VanillaSkillsCompatBuilding::onGuardSkillCreation,1);
        EpicFightEventHooks.Registry.MODIFY_SKILL_BUILDER.registerEvent(VanillaSkillsCompatBuilding::onParrySkillCreation,2);

        EpicFightEventHooks.Registry.MODIFY_SKILL_BUILDER.registerEvent(VanillaSkillsCompatBuilding::onSwordMasterSkillCreation);

    }

    @Override
    public void onModEventBusClient(IEventBus eventBus) {

    }

    @Override
    public void onGameEventBusClient(IEventBus eventBus) {
        //universal, just do it for once to affect every skill the category is compatible with
        EpicFightClientEventHooks.Registry.WEAPON_CATEGORY_ICON.registerEvent(
                event -> {
                        event.registerCategory(t0001WeaponCategories.DRAGON_GOD_SWORD,t0001Items.DRAGON_GOD_SWORD.get());
                    }
        );

    }
}
