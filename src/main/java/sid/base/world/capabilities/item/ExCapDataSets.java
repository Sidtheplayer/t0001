package sid.base.world.capabilities.item;

import sid.base.main.t0001;
import yesman.epicfight.api.ex_cap.modules.assets.MainConditionals;
import yesman.epicfight.api.ex_cap.modules.core.data.ExCapData;
import yesman.epicfight.api.ex_cap.modules.core.data.ExCapDataEntry;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public class ExCapDataSets
{
    public static ExCapDataEntry DRAGON_GOD_SWORD = new ExCapDataEntry(t0001.identifier("dragon_god_sword"), ExCapData.builder()
            .addMoveset(CapabilityItem.Styles.TWO_HAND, ExCapMovesets.DRAGON_GOD_SWORD_NORMAL.id())
             .addConditional(MainConditionals.DEFAULT_2H_WIELD_STYLE.id()));
}
