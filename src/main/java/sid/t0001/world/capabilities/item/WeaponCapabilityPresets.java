package sid.t0001.world.capabilities.item;


import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.xame.t0001;
import sid.t0001.gameasset.*;

import yesman.epicfight.api.forgeevent.WeaponCapabilityPresetRegistryEvent;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.Styles;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

@Mod.EventBusSubscriber(modid = t0001.MODID , bus = Mod.EventBusSubscriber.Bus.MOD)
public class WeaponCapabilityPresets {
    public static final Function<Item, CapabilityItem.Builder> SUPER_KATANA = (item) -> WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.UCHIGATANA) // Updated to use custom category
            .styleProvider((playerpatch) -> Styles.OCHS)
            .collider(ColliderPreset.UCHIGATANA)
            .canBePlacedOffhand(true)
            .newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK)
            .newStyleCombo(Styles.OCHS,  Animations.UCHIGATANA_AUTO1, Animations.UCHIGATANA_AUTO2, Animations.UCHIGATANA_AUTO3, Animations.UCHIGATANA_DASH, Animations.UCHIGATANA_AIR_SLASH)
            .innateSkill(Styles.OCHS, (itemstack) -> t0001Skills.T0001INNATEONE)
            .livingMotionModifier(Styles.OCHS, LivingMotions.IDLE, Animations.BIPED_IDLE)
            .livingMotionModifier(Styles.OCHS, LivingMotions.WALK, Animations.BIPED_WALK_UCHIGATANA)
            .livingMotionModifier(Styles.OCHS, LivingMotions.CHASE, Animations.BIPED_WALK_UCHIGATANA)
            .livingMotionModifier(Styles.OCHS, LivingMotions.RUN,Animations.BIPED_RUN_UCHIGATANA)
            .livingMotionModifier(Styles.OCHS, LivingMotions.SNEAK, Animations.BIPED_WALK_UCHIGATANA)
            .livingMotionModifier(Styles.OCHS, LivingMotions.SWIM, Animations.BIPED_HOLD_UCHIGATANA)
            .livingMotionModifier(Styles.OCHS, LivingMotions.FLOAT, Animations.BIPED_HOLD_UCHIGATANA)
            .livingMotionModifier(Styles.OCHS, LivingMotions.FALL, Animations.BIPED_HOLD_UCHIGATANA)

            .livingMotionModifier(Styles.OCHS, LivingMotions.BLOCK, Animations.UCHIGATANA_GUARD);



    @SubscribeEvent
    public static void registerMovesets(WeaponCapabilityPresetRegistryEvent event) {
        event.getTypeEntry().put(ResourceLocation.fromNamespaceAndPath(t0001.MODID,"superkatana"), SUPER_KATANA);

    }
}