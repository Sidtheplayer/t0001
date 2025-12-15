package sid.t0001.events;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xame.t0001;
import sid.t0001.gameasset.t0001Entities;
import sid.t0001.world.entity.Amogus;
import sid.t0001.world.entity.AmogusPatch;
import sid.t0001.world.entity.DarknessEntity;
import sid.t0001.world.entity.DarknessEntityPatch;
import yesman.epicfight.api.forgeevent.EntityPatchRegistryEvent;

@Mod.EventBusSubscriber(modid= t0001.MODID,bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBusEvents {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(t0001Entities.AMOGUS.get(), Amogus.createAttributes().build());
        event.put(t0001Entities.DARKNESS_ENTITY.get(), DarknessEntity.createAttributes().build()); // OMG AMOGUS!
    }//register amogus vanilla attributes
    @SubscribeEvent
    public static void registerEntityPatch(EntityPatchRegistryEvent event) {
        event.getTypeEntry().put(t0001Entities.AMOGUS.get(), (entityIn) -> AmogusPatch::new);
        event.getTypeEntry().put(t0001Entities.DARKNESS_ENTITY.get(), (entityIn) -> DarknessEntityPatch::new);
        // you also have to put renderer  in renderengine
    }// you know what it says

    @SubscribeEvent
    public static void registerEFAttribute(EntityAttributeModificationEvent event) {
        AmogusPatch.initAttributes(event);
        DarknessEntityPatch.initAttributes(event);
    }/* ifykyk */
}
