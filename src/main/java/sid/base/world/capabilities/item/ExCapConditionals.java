package sid.base.world.capabilities.item;

import sid.base.gameasset.t0001Skills;
import sid.base.main.t0001;
import sid.base.skill.t0001SkillDataKeys;
import yesman.epicfight.api.ex_cap.modules.core.data.ConditionalEntry;
import yesman.epicfight.api.ex_cap.modules.core.provider.ProviderConditional;
import yesman.epicfight.api.ex_cap.modules.core.provider.ProviderConditionalType;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public class ExCapConditionals {
    public static final ConditionalEntry DRAGON_GOD_SWORD_AWAKENED;

    static {
        DRAGON_GOD_SWORD_AWAKENED = new ConditionalEntry(
                t0001.identifier("awakened_dgs"),
                ProviderConditional.builder().setType(ProviderConditionalType.DATA_KEY)
                .isVisibleOffHand(false)
                .setSlot(SkillSlots.WEAPON_PASSIVE)
                .setSkillToCheck(t0001Skills.DGSPASSIVE_SKILL.value())
                .setWieldStyle(CapabilityItem.Styles.SHEATH)
                .setKey(t0001SkillDataKeys.IS_AWAKENED));
    }

}
