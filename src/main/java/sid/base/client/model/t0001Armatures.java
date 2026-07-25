package sid.base.client.model;


import sid.base.client.armature.DarknessArmature;
import sid.base.gameasset.t0001Entities;
import sid.base.main.t0001;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.model.armature.HumanoidArmature;


public class t0001Armatures {

    public static final Armatures.ArmatureAccessor<HumanoidArmature> AMOGUS = Armatures.ArmatureAccessor.create(t0001.MODID, "entity/amogus", HumanoidArmature::new);
    public static final Armatures.ArmatureAccessor<DarknessArmature> DARKNESSARMATURE = Armatures.ArmatureAccessor.create(t0001.MODID, "entity/darkness_entity", DarknessArmature::new);


    public static void registerEntityTypes() {
        Armatures.registerEntityTypeArmature(t0001Entities.AMOGUS.get(), AMOGUS);
        Armatures.registerEntityTypeArmature(t0001Entities.DARKNESS_ENTITY.get(), DARKNESSARMATURE);
    }

}
