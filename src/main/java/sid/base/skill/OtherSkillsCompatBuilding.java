package sid.base.skill;


import net.neoforged.bus.api.IEventBus;
import sid.base.gameasset.animations.DragonGodSwordAnimations;
import sid.base.main.t0001;
import sid.base.world.capabilities.t0001WeaponCategories;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.registry.SkillBuilderModificationEvent;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.gameasset.Animations;

import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.skill.guard.GuardSkill;
import yesman.epicfight.skill.passive.SwordmasterSkill;

import java.util.List;




public class OtherSkillsCompatBuilding implements ICompatModule{


    public static void onGuardSkillCreation(SkillBuilderModificationEvent event) {
        t0001.LOGGER.debug("SKILLBUILDSTARTED GUARD");

        if (!event.getRegistryName().equals(
        EpicFightSkills.GUARD.getId())){
            return;
        }

        if (!(event.getSkillBuilder() instanceof GuardSkill.Builder builder)) {
            return;
        }

        builder
                .addGuardMotion(
                        t0001WeaponCategories.DRAGON_GOD_SWORD,
                        (item, player) -> DragonGodSwordAnimations.GUARD_HIT
                )
                .addGuardBreakMotion(
                        t0001WeaponCategories.DRAGON_GOD_SWORD,
                        (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED
                );
        System.out.println("GUARDCOMPAT IMPLEMENTED");

    }



    public static void onParrySkillCreation(SkillBuilderModificationEvent evt) {
        t0001.LOGGER.debug("SKILLBUILDSTARTED PARRY");

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
                t0001.LOGGER.debug("PARRY COMPAT HAS BEEN IMPLEMENTED");
                System.out.println("PARRYCOMPAT IMPLEMENTED");
            }

        }

    }
    


    public static void onSwordMasterSkillCreation(SkillBuilderModificationEvent evt) {

        if (evt.getRegistryName().equals(EpicFightSkills.SWORD_MASTER.getId())) {
            if (evt.getSkillBuilder() instanceof SwordmasterSkill.Builder builder) {

                builder
                        .addAvailableWeaponCategory(t0001WeaponCategories.DRAGON_GOD_SWORD);

            }
        }

    }


    @Override
    public void onModEventBus(IEventBus eventBus) {

    }

    @Override
    public void onGameEventBus(IEventBus eventBus) {
        EpicFightEventHooks.Registry.MODIFY_SKILL_BUILDER.registerEvent(OtherSkillsCompatBuilding::onGuardSkillCreation);
        EpicFightEventHooks.Registry.MODIFY_SKILL_BUILDER.registerEvent(OtherSkillsCompatBuilding::onSwordMasterSkillCreation);
        EpicFightEventHooks.Registry.MODIFY_SKILL_BUILDER.registerEvent(OtherSkillsCompatBuilding::onParrySkillCreation);

    }

    @Override
    public void onModEventBusClient(IEventBus eventBus) {

    }

    @Override
    public void onGameEventBusClient(IEventBus eventBus) {

    }
}
