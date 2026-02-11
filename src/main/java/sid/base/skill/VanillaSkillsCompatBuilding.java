package sid.base.skill;


import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.NotNull;
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
import yesman.epicfight.world.damagesource.EpicFightDamageTypes;

import java.util.List;
import java.util.Set;


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

    public static void onImpactGuardSkillCreation(SkillBuilderModificationEvent event) {

        if (event.getRegistryName().equals(EpicFightSkills.IMPACT_GUARD.getId())) {
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

        Set<@NotNull ResourceKey<DamageType>> Predicate = Set.of(
                DamageTypes.ARROW,
                DamageTypes.FIREBALL,
                DamageTypes.MOB_PROJECTILE,
                EpicFightDamageTypes.WITHER_BEAM
        );

        if (evt.getRegistryName().equals(EpicFightSkills.PARRYING.getId())) {
            if (evt.getSkillBuilder() instanceof GuardSkill.Builder builder) {

                builder
                        .addGuardMotion(t0001WeaponCategories.DRAGON_GOD_SWORD, (item, player) -> DragonGodSwordAnimations.GUARD_HIT)
                        .addGuardBreakMotion(t0001WeaponCategories.DRAGON_GOD_SWORD, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED)
                        .addAdvancedGuardMotion(t0001WeaponCategories.DRAGON_GOD_SWORD, (item, player) ->

                        {  DamageSource source = player.getOriginal().getLastDamageSource();
                            return (source != null && Predicate.contains(source.typeHolder().unwrapKey().orElse(null))) ?
                                    List.of(DragonGodSwordAnimations.DGS_PARRY,
                                            DragonGodSwordAnimations.DGS_PARRY_2,
                                            DragonGodSwordAnimations.DGS_PARRY_3,
                                            DragonGodSwordAnimations.DGS_PARRY_4)
                                    : List.of(Animations.LONGSWORD_GUARD_ACTIVE_HIT1, Animations.LONGSWORD_GUARD_ACTIVE_HIT2); });


                       //todo: projectile special is done, make melee special

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
        //make sure guard is on higher priority or guard won't work properly
        EpicFightEventHooks.Registry.MODIFY_SKILL_BUILDER.registerEvent(VanillaSkillsCompatBuilding::onGuardSkillCreation,1);
        EpicFightEventHooks.Registry.MODIFY_SKILL_BUILDER.registerEvent(VanillaSkillsCompatBuilding::onImpactGuardSkillCreation,2);
        EpicFightEventHooks.Registry.MODIFY_SKILL_BUILDER.registerEvent(VanillaSkillsCompatBuilding::onParrySkillCreation,3);

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
