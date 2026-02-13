package sid.base.world.capabilities.item;


import java.util.function.Function;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import org.jetbrains.annotations.NotNull;

import sid.base.gameasset.animations.DragonGodSwordAnimations;

import sid.base.gameasset.animations.collider.CGSColliderPresets;
import sid.base.gameasset.t0001Skills;
import sid.base.main.t0001;
import sid.base.world.capabilities.t0001WeaponCategories;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.event.types.registry.WeaponCapabilityPresetRegistryEvent;
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
            .newStyleCombo(Styles.TWO_HAND,  Animations.UCHIGATANA_AUTO1, Animations.UCHIGATANA_AUTO2, Animations.UCHIGATANA_AUTO3, Animations.UCHIGATANA_DASH, Animations.UCHIGATANA_AIR_SLASH)
            .innateSkill(Styles.SHEATH, (itemstack) -> (EpicFightSkills.BATTOJUTSU.get()))
            .innateSkill(Styles.TWO_HAND,   (itemstack) -> t0001Skills.T0001_INNATE_ONE.get())
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


    public static final Function<Item, WeaponCapability.Builder> DRAGON_GOD_SWORD = (item) -> {  return WeaponCapability.builder()
            .category(t0001WeaponCategories.DRAGON_GOD_SWORD)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(CGSColliderPresets.DRAGON_GOD_SWORD_COLLIDER)
            .hitParticle(EpicFightParticles.HIT_BLADE.get())
            .styleProvider((entitypatch) -> Styles.TWO_HAND)
            .innateSkill(Styles.TWO_HAND, (itemstack) ->  t0001Skills.EDGINGSWORDINTENT.get())
            .passiveSkill(t0001Skills.DGSPASSIVE_SKILL.get())
            .canBePlacedOffhand(false)
            .newStyleCombo(Styles.TWO_HAND, Animations.UCHIGATANA_AUTO1,Animations.LONGSWORD_AUTO2, Animations.UCHIGATANA_AUTO3, Animations.LONGSWORD_DASH, Animations.UCHIGATANA_AIR_SLASH)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.IDLE, DragonGodSwordAnimations.DGS_IDLE)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.KNEEL, Animations.BIPED_KNEEL)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.WALK, Animations.BIPED_WALK_LONGSWORD)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.RUN, DragonGodSwordAnimations.DGS_RUN)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.SNEAK, Animations.BIPED_SNEAK)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.SWIM, Animations.BIPED_SWIM)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.FLOAT, DragonGodSwordAnimations.DGS_IDLE)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.FALL, Animations.BIPED_FALL)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.BLOCK, DragonGodSwordAnimations.GUARD);

    };


    public static final Function<Item, WeaponCapability.Builder> FREE_KATANA = (item) -> { return WeaponCapability.builder()
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


    public static void registerMovesets(@NotNull WeaponCapabilityPresetRegistryEvent event) {
        event.getTypeEntry().put(ResourceLocation.fromNamespaceAndPath(t0001.MODID,"dragon_god_sword"),DRAGON_GOD_SWORD);
        event.getTypeEntry().put(ResourceLocation.fromNamespaceAndPath(t0001.MODID,"free_katana"), FREE_KATANA);
        event.getTypeEntry().put(ResourceLocation.fromNamespaceAndPath(t0001.MODID,"superkatana"), SUPER_KATANA);
    }


}