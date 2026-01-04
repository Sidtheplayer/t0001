package sid.t0001.client.model;


import net.minecraftforge.fml.common.Mod;
import org.xame.t0001;
import sid.t0001.armature.DarknessArmature;
import sid.t0001.gameasset.t0001Entities;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.model.armature.HumanoidArmature;

@Mod.EventBusSubscriber(
        modid = "t0001",
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public class t0001Armatures {

    public static final Armatures.ArmatureAccessor<HumanoidArmature> AMOGUS = Armatures.ArmatureAccessor.create(t0001.MODID, "entity/amogus", HumanoidArmature::new);
    public static final Armatures.ArmatureAccessor<DarknessArmature> DARKNESSARMATURE = Armatures.ArmatureAccessor.create(t0001.MODID, "entity/darkness_entity", DarknessArmature::new);


    public static void registerEntityTypes() {

        Armatures.registerEntityTypeArmature(t0001Entities.AMOGUS.get(), AMOGUS);
        Armatures.registerEntityTypeArmature(t0001Entities.DARKNESS_ENTITY.get(), DARKNESSARMATURE);
    }

}
