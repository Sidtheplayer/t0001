package sid.base.world.capabilities.item;


import java.util.function.Function;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import org.jetbrains.annotations.NotNull;

import sid.base.gameasset.animations.DragonGodSwordAnimations;

import sid.base.gameasset.animations.collider.CGSColliderPresets;
import sid.base.gameasset.animations.t0001Animations;
import sid.base.gameasset.t0001Skills;
import sid.base.main.t0001;
import sid.base.skill.t0001SkillDataKeys;
import sid.base.world.capabilities.t0001WeaponCategories;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.event.types.registry.WeaponCapabilityPresetRegistryEvent;
import yesman.epicfight.api.ex_cap.modules.assets.MainConditionals;
import yesman.epicfight.api.ex_cap.modules.core.data.ExCapData;
import yesman.epicfight.api.ex_cap.modules.core.data.ExCapDataEntry;
import yesman.epicfight.api.ex_cap.modules.core.data.MoveSet;
import yesman.epicfight.api.ex_cap.modules.core.data.MoveSetEntry;
import yesman.epicfight.api.ex_cap.modules.core.events.ExCapBuilderCreationEvent;
import yesman.epicfight.api.ex_cap.modules.core.events.ExCapDataRegistrationEvent;
import yesman.epicfight.api.ex_cap.modules.core.events.ExCapMovesetRegistryEvent;
import yesman.epicfight.api.ex_cap.modules.core.events.ExCapabilityBuilderPopulationEvent;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.ColliderPreset;

import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.Styles;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

import static yesman.epicfight.api.ex_cap.modules.assets.ExCapDataSets.FIST;


public class t0001WeaponCapabilityPresets {

    public static final Function<Item, WeaponCapability.Builder> SUPER_KATANA = (item) -> {
        return WeaponCapability.builder()
                .passiveSkill(EpicFightSkills.BATTOJUTSU_PASSIVE.get())
                .styleProvider((entitypatch) -> {
                    if (entitypatch instanceof PlayerPatch<?> playerpatch && (playerpatch.getSkill(SkillSlots.WEAPON_PASSIVE).getDataManager().hasData(EpicFightSkillDataKeys.SHEATH) &&
                            playerpatch.getSkill(SkillSlots.WEAPON_PASSIVE).getDataManager().getDataValue(EpicFightSkillDataKeys.SHEATH))) {
                        return Styles.SHEATH;
                    }
                    return Styles.TWO_HAND;
                })

                .category(CapabilityItem.WeaponCategories.UCHIGATANA)
                .hitSound(EpicFightSounds.BLADE_HIT.get())
                .hitParticle(EpicFightParticles.HIT_BLADE.get())
                .collider(ColliderPreset.UCHIGATANA)
                .canBePlacedOffhand(false)
                .newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK)
                .newStyleCombo(Styles.SHEATH, Animations.UCHIGATANA_SHEATHING_AUTO, Animations.UCHIGATANA_SHEATHING_DASH, Animations.UCHIGATANA_SHEATH_AIR_SLASH)
                .newStyleCombo(Styles.TWO_HAND, Animations.UCHIGATANA_AUTO1, Animations.UCHIGATANA_AUTO2, Animations.UCHIGATANA_AUTO3, Animations.UCHIGATANA_DASH, Animations.UCHIGATANA_AIR_SLASH)
                .innateSkill(Styles.SHEATH, (itemstack) -> (EpicFightSkills.BATTOJUTSU.get()))
                .innateSkill(Styles.TWO_HAND, (itemstack) -> t0001Skills.T0001_INNATE_ONE.get())
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.IDLE, Animations.BIPED_HOLD_UCHIGATANA)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.KNEEL, Animations.BIPED_HOLD_UCHIGATANA)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.WALK, Animations.BIPED_WALK_UCHIGATANA)
                //  .livingMotionModifier(Styles.TWO_HAND, LivingMotions.CHASE, Animations.BIPED_WALK_UCHIGATANA)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.RUN, Animations.BIPED_RUN_UCHIGATANA)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.SNEAK, Animations.BIPED_WALK_UCHIGATANA)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.SWIM, Animations.BIPED_HOLD_UCHIGATANA)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.FLOAT, Animations.BIPED_HOLD_UCHIGATANA)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.FALL, Animations.BIPED_HOLD_UCHIGATANA)
                .livingMotionModifier(Styles.SHEATH, LivingMotions.IDLE, Animations.BIPED_HOLD_UCHIGATANA_SHEATHING)
                .livingMotionModifier(Styles.SHEATH, LivingMotions.KNEEL, Animations.BIPED_HOLD_UCHIGATANA_SHEATHING)
                .livingMotionModifier(Styles.SHEATH, LivingMotions.WALK, Animations.BIPED_WALK_UCHIGATANA_SHEATHING)
                .livingMotionModifier(Styles.SHEATH, LivingMotions.CHASE, Animations.BIPED_HOLD_UCHIGATANA_SHEATHING)
                .livingMotionModifier(Styles.SHEATH, LivingMotions.RUN, Animations.BIPED_RUN_UCHIGATANA_SHEATHING)
                .livingMotionModifier(Styles.SHEATH, LivingMotions.SNEAK, Animations.BIPED_HOLD_UCHIGATANA_SHEATHING)
                .livingMotionModifier(Styles.SHEATH, LivingMotions.SWIM, Animations.BIPED_HOLD_UCHIGATANA_SHEATHING)
                .livingMotionModifier(Styles.SHEATH, LivingMotions.FLOAT, Animations.BIPED_HOLD_UCHIGATANA_SHEATHING)
                .livingMotionModifier(Styles.SHEATH, LivingMotions.FALL, Animations.BIPED_HOLD_UCHIGATANA_SHEATHING)

                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.BLOCK, Animations.UCHIGATANA_GUARD);
    };

    @Deprecated(forRemoval = true)
    public static final Function<Item, WeaponCapability.Builder> DRAGON_GOD_SWORD = (item) -> {
        return WeaponCapability.builder()
                .category(t0001WeaponCategories.DRAGON_GOD_SWORD)
                .hitSound(EpicFightSounds.BLADE_HIT.get())
                .collider(CGSColliderPresets.DRAGON_GOD_SWORD_COLLIDER)
                .hitParticle(EpicFightParticles.HIT_BLADE.get())
                .passiveSkill(t0001Skills.DGSPASSIVE_SKILL.get())
                .styleProvider((entitypatch) -> {
                    if (entitypatch instanceof PlayerPatch<?> playerpatch && (playerpatch.getSkill(SkillSlots.WEAPON_PASSIVE).getDataManager().hasData(t0001SkillDataKeys.IS_AWAKENED) &&
                            playerpatch.getSkill(SkillSlots.WEAPON_PASSIVE).getDataManager().getDataValue(t0001SkillDataKeys.IS_AWAKENED))) {
                        return Styles.TWO_HAND;
                    }

                    return Styles.OCHS;

                })
                .innateSkill(Styles.TWO_HAND, (itemstack) -> t0001Skills.PHANTOM_SEVERANCE.get())
                .innateSkill(Styles.OCHS, (itemStack) -> t0001Skills.T0001_INNATE_ONE.get())
                .canBePlacedOffhand(false)
                .newStyleCombo(Styles.COMMON, Animations.UCHIGATANA_AUTO1, Animations.LONGSWORD_AUTO2, Animations.UCHIGATANA_AUTO3, Animations.LONGSWORD_DASH, Animations.UCHIGATANA_AIR_SLASH)
                .livingMotionModifier(Styles.COMMON, LivingMotions.IDLE, DragonGodSwordAnimations.DGS_IDLE)
                .livingMotionModifier(Styles.COMMON, LivingMotions.WALK, Animations.BIPED_WALK_LONGSWORD)
                .livingMotionModifier(Styles.COMMON, LivingMotions.RUN, DragonGodSwordAnimations.DGS_RUN)
                .livingMotionModifier(Styles.COMMON, LivingMotions.FLOAT, DragonGodSwordAnimations.DGS_IDLE)
                .livingMotionModifier(Styles.COMMON, LivingMotions.BLOCK, DragonGodSwordAnimations.GUARD);

    };

    public static final MoveSetEntry DRAGON_GOD_SWORD_NORMAL = new MoveSetEntry(
            t0001.identifier("dgs_n"),
            MoveSet.builder()
                    .addLivingMotionsRecursive(DragonGodSwordAnimations.DGS_IDLE,LivingMotions.IDLE,LivingMotions.FLOAT)
                    .addLivingMotionModifier(LivingMotions.RUN, DragonGodSwordAnimations.DGS_RUN)
                    .addLivingMotionModifier(LivingMotions.BLOCK, DragonGodSwordAnimations.GUARD)
                    .addLivingMotionModifier(LivingMotions.WALK, Animations.BIPED_WALK_LONGSWORD)
                    .setPassiveSkill(t0001Skills.DGSPASSIVE_SKILL.get())
                    .shouldRenderSheath(LivingEntityPatch -> true)
                    .addComboAttacks(
                            Animations.UCHIGATANA_AUTO1,
                            Animations.LONGSWORD_AUTO2,
                            Animations.UCHIGATANA_AUTO3,
                            Animations.LONGSWORD_DASH,
                            Animations.UCHIGATANA_AIR_SLASH
                    )
                    .addInnateSkill(((itemStack, playerPatch) -> {
                        if(playerPatch.getSkill(SkillSlots.WEAPON_PASSIVE).getDataManager().hasData(t0001SkillDataKeys.IS_AWAKENED) &&
                                playerPatch.getSkill(SkillSlots.WEAPON_PASSIVE).getDataManager().getDataValue(t0001SkillDataKeys.IS_AWAKENED)){
                            return t0001Skills.PHANTOM_SEVERANCE.get();
                        }
                        return t0001Skills.EDGINGSWORDINTENT.get();
                    }))
    );


    public static final Function<Item, WeaponCapability.Builder> FREE_KATANA = (item) -> {
        return WeaponCapability.builder()
                .styleProvider((livingEntityPatch) -> Styles.TWO_HAND)
                .collider(ColliderPreset.TACHI)
                .canBePlacedOffhand(false)
                .newStyleCombo(Styles.TWO_HAND, Animations.TACHI_AUTO1, Animations.TACHI_AUTO2, Animations.TACHI_AUTO3, Animations.TACHI_DASH, Animations.LONGSWORD_AIR_SLASH)
                .newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK)
                .innateSkill(Styles.TWO_HAND, (itemstack) -> EpicFightSkills.RUSHING_TEMPO.get())
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.IDLE, Animations.BIPED_HOLD_TACHI)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.KNEEL, Animations.BIPED_HOLD_TACHI)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.WALK, Animations.BIPED_HOLD_TACHI)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.CHASE, Animations.BIPED_HOLD_TACHI)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.RUN, Animations.BIPED_HOLD_TACHI)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.SNEAK, Animations.BIPED_HOLD_TACHI)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.SWIM, Animations.BIPED_HOLD_TACHI)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.FLOAT, Animations.BIPED_HOLD_TACHI)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.FALL, Animations.BIPED_HOLD_TACHI)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.BLOCK, Animations.LONGSWORD_GUARD);

    };

    public static final ExCapDataEntry DGS_DATA_ENTRY = new ExCapDataEntry(t0001.identifier("dragon_god_sword"),
            ExCapData.builder()
                    .addConditional(MainConditionals.DEFAULT_2H_WIELD_STYLE.id())
                    .addMoveset(Styles.TWO_HAND, t0001WeaponCapabilityPresets.DRAGON_GOD_SWORD_NORMAL.id())
    );


    public static void registerMovesets(@NotNull WeaponCapabilityPresetRegistryEvent event) {
        // event.getTypeEntry().put(ResourceLocation.fromNamespaceAndPath(t0001.MODID, "dragon_god_sword"), DRAGON_GOD_SWORD);
        event.getTypeEntry().put(ResourceLocation.fromNamespaceAndPath(t0001.MODID, "free_katana"), FREE_KATANA);
        event.getTypeEntry().put(ResourceLocation.fromNamespaceAndPath(t0001.MODID, "superkatana"), SUPER_KATANA);
    }

    public static void registerExCapMethods(ExCapabilityBuilderPopulationEvent event) {
        event.registerData(ExCapBuilders.DRAGON_GOD_SWORD.id(), t0001WeaponCapabilityPresets.DGS_DATA_ENTRY.id());
    }

    public static void registerExcapMoveset(@NotNull ExCapMovesetRegistryEvent event) {
        event.getMovesets().get(FIST.id()).addLivingMotionModifier(LivingMotions.BLOCK, t0001Animations.UNARMEDBLOCKFULL);
        event.getMovesets().get(FIST.id()).addComboAttacks(t0001Animations.SWEEP, t0001Animations.I_SWEEP);

        event.addMoveSet(
                t0001WeaponCapabilityPresets.DRAGON_GOD_SWORD_NORMAL
        );

    }

    public static void registerExCapBuilders(ExCapBuilderCreationEvent event) {
        event.addBuilder(
                ExCapBuilders.DRAGON_GOD_SWORD
        );

    }


    public static void registerExCapData(@NotNull ExCapDataRegistrationEvent event) {

        event.addData(
                t0001WeaponCapabilityPresets.DGS_DATA_ENTRY
        );

    }


}