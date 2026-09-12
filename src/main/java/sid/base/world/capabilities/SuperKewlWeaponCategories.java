package sid.base.world.capabilities;

import yesman.epicfight.world.capabilities.item.Style;

public enum SuperKewlWeaponCategories implements Style {
    AWAKENED_STATE
    ;

    final int id;

    SuperKewlWeaponCategories(){
        this.id = Style.ENUM_MANAGER.assign(this);
    }

    @Override
    public boolean canUseOffhand() {
        return false;
    }

    @Override
    public int universalOrdinal() {
        return this.id;
    }
}
