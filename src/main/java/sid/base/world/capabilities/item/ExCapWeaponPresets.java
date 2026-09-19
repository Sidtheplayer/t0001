package sid.base.world.capabilities.item;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import reascer.wom.gameasset.colliders.WOMWeaponColliders;
import sid.base.gameasset.animations.collider.CGSColliderPresets;
import sid.base.main.t0001;
import sid.base.particle.t0001Particles;
import sid.base.world.capabilities.SuperKewlStyles;
import sid.base.world.capabilities.t0001WeaponCategories;
import sid.base.world.t0001Sounds;
import yesman.epicfight.registry.deferred.ItemPresetRegister;
import yesman.epicfight.registry.deferred.holders.DeferredPreset;
import yesman.epicfight.registry.entries.*;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

public class ExCapWeaponPresets {

    public static final ItemPresetRegister REGISTRY = ItemPresetRegister.create(t0001.MODID);

    public static void addMoveset(){

        EpicFightItemCapabilityPresets.FIST.get()
                .addConditionals(ExCapConditionals.AmatuerKicker)
                .addMoveset(CapabilityItem.Styles.OCHS, ExCapMovesets.amatuerKicker);
    }

    public static final DeferredPreset<?> DRAGON_GOD_SWORD = REGISTRY.registerWeapon("dragon_god_sword",
            () ->
                    WeaponCapability.builder()
                            .category(t0001WeaponCategories.DRAGON_GOD_SWORD)
                            .hitSound(EpicFightSounds.BLADE_HIT)
                            .addStyleAttibutes(
                                    SuperKewlStyles.AWAKENED_STATE,
                                    EpicFightAttributes.IMPACT,
                                    new AttributeModifier(
                                            t0001.identifier("dgs_impact"),
                                            2.0D,
                                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            )
                            .hitParticle(t0001Particles.BLOODY_CUT)
                            .collider(CGSColliderPresets.DRAGON_GOD_SWORD_COLLIDER)
                            .addMoveset(CapabilityItem.Styles.TWO_HAND, ExCapMovesets.DRAGON_GOD_SWORD_NORMAL)
                            .addMoveset(SuperKewlStyles.AWAKENED_STATE, ExCapMovesets.DRAGON_GOD_SWORD_AWAKENED)
                            .addConditionals(ExCapConditionals.DRAGON_GOD_SWORD_AWAKENED, EpicFightProviderConditionals.DEFAULT_2H_WIELD_STYLE)
                            .canBePlacedOffhand(false)
                            .setTierValues(0, 0d, 0.0, 0.0)
    );

    public static final DeferredPreset<?> FREE_KATANA = REGISTRY.registerWeapon("free_katana",
            () ->
                    WeaponCapability.builder()
                            .category(CapabilityItem.WeaponCategories.UCHIGATANA)
                            .collider(WOMWeaponColliders.SATSUJIN)
                            .canBePlacedOffhand(false)
                            .hitParticle(t0001Particles.BLOODY_CUT_NORMAL)
                            .hitSound(t0001Sounds.SLASH_HIT)
                            .swingSound(EpicFightSounds.WHOOSH)
                            .addConditionals(EpicFightProviderConditionals.DEFAULT_2H_WIELD_STYLE)
                            .addMoveset(CapabilityItem.Styles.TWO_HAND, ExCapMovesets.SOLAR_SWORD)
                            .canBePlacedOffhand(false)
                            .setTierValues(20, 0d, 3.8, 0.0));
}
