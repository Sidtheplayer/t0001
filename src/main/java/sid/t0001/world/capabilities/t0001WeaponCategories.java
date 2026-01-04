package sid.t0001.world.capabilities;

import yesman.epicfight.world.capabilities.item.WeaponCategory;


//this needs to be registered in main mod class constructor.
public enum t0001WeaponCategories implements WeaponCategory {

    DRAGON_GOD_SWORD
    ;

    final int id;

    t0001WeaponCategories(){
        this.id = WeaponCategory.ENUM_MANAGER.assign(this); // use weaponcategorie's enum manager to assign our things
    }

    @Override
    public int universalOrdinal() {
        return this.id;
    }

}
