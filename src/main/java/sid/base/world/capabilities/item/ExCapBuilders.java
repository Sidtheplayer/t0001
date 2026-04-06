package sid.base.world.capabilities.item;

import sid.base.gameasset.animations.collider.CGSColliderPresets;
import sid.base.main.t0001;
import sid.base.particle.t0001Particles;
import sid.base.world.capabilities.t0001WeaponCategories;
import yesman.epicfight.api.ex_cap.modules.core.data.BuilderEntry;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

public class ExCapBuilders {

    public static final BuilderEntry DRAGON_GOD_SWORD = new BuilderEntry(t0001.identifier("dragon_god_sword"), WeaponCapability.builder()
            .category(t0001WeaponCategories.DRAGON_GOD_SWORD)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .hitParticle(t0001Particles.BLOODY_CUT.get())
            .collider(CGSColliderPresets.DRAGON_GOD_SWORD_COLLIDER)
            .canBePlacedOffhand(false)
            .setTierValues(0, 0d, 0.0, 0.0));

    public static final BuilderEntry FREE_KATANA = new BuilderEntry(t0001.identifier("free_katana"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.UCHIGATANA)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .hitParticle(t0001Particles.BLOODY_CUT.get())
            .collider(ColliderPreset.TACHI)
            .canBePlacedOffhand(false)
            .setTierValues(0, 0d, 0.0, 0.0));
}
