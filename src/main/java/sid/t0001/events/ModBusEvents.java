package sid.t0001.events;


import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import org.jetbrains.annotations.NotNull;
import sid.t0001.gameasset.animations.CentralAnimationBuild;
import sid.t0001.gameasset.t0001Entities;
import sid.t0001.main.t0001;
import sid.t0001.world.entity.Amogus;
import sid.t0001.world.entity.AmogusPatch;
import sid.t0001.world.entity.DarknessEntity;
import sid.t0001.world.entity.DarknessEntityPatch;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.neoevent.EntityPatchRegistryEvent;

@EventBusSubscriber(modid= t0001.MODID, value = Dist.CLIENT)
public class ModBusEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(t0001Entities.AMOGUS.get(), Amogus.createAttributes().build());
        event.put(t0001Entities.DARKNESS_ENTITY.get(), DarknessEntity.createAttributes().build()); // OMG AMOGUS!
    }//register amogus vanilla attributes

    @SubscribeEvent
    public static void registerEntityPatch(EntityPatchRegistryEvent event) {
        event.getTypeEntry().put(
                t0001Entities.AMOGUS.get(),
                entity ->
                new AmogusPatch((Amogus) entity)
        );

        event.getTypeEntry().put(
                t0001Entities.DARKNESS_ENTITY.get(),
                entity -> new DarknessEntityPatch((DarknessEntity) entity)
        );
    }

    // you also have to put renderer  in renderengine
    // you know what it says



    @SubscribeEvent
    public static void registerEFAttribute(EntityAttributeModificationEvent event) {
        AmogusPatch.initAttributes(event);
        DarknessEntityPatch.initAttributes(event);
    }/* ifykyk */


    //build animation shit here
    @SubscribeEvent
    public static void registerAnimations(AnimationManager.AnimationRegistryEvent event) {
        event.newBuilder(t0001.MODID, CentralAnimationBuild::listen);
    }


}
