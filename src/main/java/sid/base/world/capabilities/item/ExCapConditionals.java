package sid.base.world.capabilities.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import sid.base.skill.t0001Skills;
import sid.base.main.t0001;
import sid.base.skill.t0001SkillDataKeys;
import sid.base.skill.t0001SkillSlots;

import yesman.epicfight.api.ex_cap.provider.ProviderConditional;
import yesman.epicfight.registry.deferred.ProviderConditionalRegister;
import yesman.epicfight.registry.deferred.holders.DeferredConditional;
import yesman.epicfight.world.capabilities.item.CapabilityItem;


public class ExCapConditionals {
    public static final ProviderConditionalRegister REGISTRY = ProviderConditionalRegister.create(t0001.MODID);


    public static final DeferredConditional DRAGON_GOD_SWORD_AWAKENED;
    public static final DeferredConditional AmatuerKicker;

    static {
        DRAGON_GOD_SWORD_AWAKENED = REGISTRY.registerConditional(
                "awakened_dgs", () ->
                        ProviderConditional.createSkillDataKey(
                                CapabilityItem.Styles.SHEATH,
                                t0001Skills.Jun_AWAKEN,
                                t0001SkillSlots.AWAKENING,
                                t0001SkillDataKeys.IS_AWAKENED,
                                false
                        ));

        AmatuerKicker = REGISTRY.registerConditional(
                "amateur_kicker", () ->
                        ProviderConditional.createSpecificWeapon(
                                CapabilityItem.Styles.OCHS,
                                Items.AIR,
                                InteractionHand.OFF_HAND,
                                true
                        )

        );

    }

}
