package sid.base.skill;


import net.minecraft.resources.ResourceLocation;
import sid.base.gameasset.animations.DragonGodSwordAnimations;
import sid.base.world.capabilities.t0001WeaponCategories;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.event.types.registry.SkillBuilderModificationEvent;
import yesman.epicfight.gameasset.Animations;

import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.skill.guard.GuardSkill;
import yesman.epicfight.skill.passive.SwordmasterSkill;

import java.util.List;




public class OtherSkillsCompatBuilding {


    public static void onGuardSkillCreation(SkillBuilderModificationEvent event) {
        if (!event.getRegistryName().equals(
                ResourceLocation.fromNamespaceAndPath(EpicFight.MODID, String.valueOf(EpicFightSkills.GUARD.getKey())))) {
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

                builder
                        .addAvailableWeaponCategory(t0001WeaponCategories.DRAGON_GOD_SWORD);

            }
        }

    }


}
