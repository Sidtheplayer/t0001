package sid.t0001.skill;


import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import sid.t0001.gameasset.animations.DragonGodSwordAnimations;
import sid.t0001.main.t0001;
import sid.t0001.world.capabilities.t0001WeaponCategories;
import yesman.epicfight.api.neoevent.BuilderModificationEvent;
import yesman.epicfight.api.neoevent.SkillLootTableRegistryEvent;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillEvent;
import yesman.epicfight.skill.guard.GuardSkill;
import yesman.epicfight.skill.guard.ParryingSkill;
import yesman.epicfight.skill.passive.HyperVitalitySkill;
import yesman.epicfight.skill.passive.SwordmasterSkill;

import java.util.List;


@EventBusSubscriber(modid = t0001.MODID)
public class OtherSkillsCompatBuilding {
    public static void forceGuard(Skill.SkillEventSubscriber bus) {}

    @SubscribeEvent
    public static void onGuardSkillCreation(BuilderModificationEvent event) {
        if (!event.getRegistryName().equals(
                ResourceLocation.fromNamespaceAndPath("epicfight", "guard"))) {
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


    @SubscribeEvent
    public static void onParrySkillCreation(BuilderModificationEvent evt) {

        if (evt.getRegistryName().equals(ResourceLocation.fromNamespaceAndPath("epicfight", "parrying"))) {
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

    @SubscribeEvent
    public static void onSwordMasterSkillCreation(BuilderModificationEvent evt) {

        if (evt.getRegistryName().equals(ResourceLocation.fromNamespaceAndPath("epicfight", "swordmaster"))) {
            if (evt.getSkillBuilder() instanceof SwordmasterSkill.Builder builder) {

                builder
                        .addAvailableWeaponCategory(t0001WeaponCategories.DRAGON_GOD_SWORD);

            }
        }

    }


}
