package sid.t0001.world.capabilities.item;


import java.util.function.Function;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import sid.t0001.gameasset.*;

import sid.t0001.gameasset.animations.DragonGodSwordAnimations;

import sid.t0001.main.t0001;
import sid.t0001.world.capabilities.t0001WeaponCategories;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.ColliderPreset;

import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.Styles;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

@EventBusSubscriber(modid = t0001.MODID )
public class WeaponCapabilityPresets {
    public static final Function<Item, WeaponCapability.Builder> SUPER_KATANA = (item) -> {
        WeaponCapability.Builder builder = WeaponCapability.builder()
            .passiveSkill(EpicFightSkills.BATTOJUTSU_PASSIVE.get())
            .styleProvider((entitypatch) -> {
                if (entitypatch instanceof PlayerPatch<?> playerpatch && (playerpatch.getSkill(SkillSlots.WEAPON_PASSIVE).getDataManager().hasData(SkillDataKeys.SHEATH.get()) &&
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
            .innateSkill(Styles.TWO_HAND,   (itemstack) -> EpicFightSkills.SWEEPING_EDGE.get())
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

    public static final Function<Item, WeaponCapability.Builder> DRAGON_GOD_SWORD = (item) -> { CapabilityItem.Builder builder = WeaponCapability.builder()
            .category(t0001WeaponCategories.DRAGON_GOD_SWORD)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .hitParticle(EpicFightParticles.HIT_BLADE.get())
            .styleProvider((entitypatch) -> Styles.TWO_HAND)
            .innateSkill(Styles.TWO_HAND, (itemstack) -> EpicFightSkills.SWEEPING_EDGE)
            .canBePlacedOffhand(false)
            .newStyleCombo(Styles.TWO_HAND, Animations.UCHIGATANA_AUTO1, Animations.UCHIGATANA_AUTO2, Animations.UCHIGATANA_AUTO3, Animations.GRASPING_SPIRAL_FIRST, Animations.UCHIGATANA_AIR_SLASH)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.IDLE, DragonGodSwordAnimations.DGS_IDLE)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.KNEEL, Animations.BIPED_KNEEL)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.WALK, Animations.BIPED_WALK_UCHIGATANA)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.RUN, DragonGodSwordAnimations.DGS_RUN)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.SNEAK, Animations.BIPED_WALK_UCHIGATANA)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.SWIM, Animations.BIPED_SWIM)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.FLOAT, DragonGodSwordAnimations.DGS_IDLE)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.FALL, Animations.BIPED_FALL)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.BLOCK, DragonGodSwordAnimations.GUARD);



    public static final Function<Item,CapabilityItem.Builder> FREE_KATANA = (item) -> WeaponCapability.builder()
            .styleProvider((playerpatch) -> Styles.TWO_HAND)
            .collider(ColliderPreset.TACHI)
            .canBePlacedOffhand(false)
            .newStyleCombo(Styles.TWO_HAND, Animations.TACHI_AUTO1, Animations.TACHI_AUTO2, Animations.TACHI_AUTO3, Animations.TACHI_DASH, Animations.LONGSWORD_AIR_SLASH)
            .newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK)
            .innateSkill(Styles.TWO_HAND, (itemstack) -> EpicFightSkills.RUSHING_TEMPO)
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

    public static void registerMovesets(@NotNull WeaponCapabilityPresetRegistryEvent event) {
        event.getTypeEntry().put(ResourceLocation.fromNamespaceAndPath(t0001.MODID,"dragon_god_sword"),DRAGON_GOD_SWORD);
        event.getTypeEntry().put(ResourceLocation.fromNamespaceAndPath(t0001.MODID,"free_katana"), FREE_KATANA);
        event.getTypeEntry().put(ResourceLocation.fromNamespaceAndPath(t0001.MODID,"superkatana"), SUPER_KATANA);
    }

}