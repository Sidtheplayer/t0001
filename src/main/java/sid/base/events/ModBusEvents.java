package sid.base.events;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import sid.base.gameasset.animations.CentralAnimationBuild;
import sid.base.world.entity.*;
import sid.base.main.t0001;
import sid.base.world.capabilities.item.t0001WeaponCapabilityPresets;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.registry.EntityPatchRegistryEvent;

@EventBusSubscriber(modid= t0001.MODID)
public class ModBusEvents {

    //register amogus vanilla attributes
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(t0001Entities.AMOGUS.get(), Amogus.createAttributes().build());
        event.put(t0001Entities.SHADOW_CLONE.get(), ShadowCloneEntity.createBaseAttributes().build());
        event.put(t0001Entities.DARKNESS_ENTITY.get(), DarknessEntity.createAttributes().build()); // OMG AMOGUS!
    }

    // you also have to put register renderer in renderengine
    // you know what it says
    public static void registerEntityPatch(EntityPatchRegistryEvent event) {
        event.registerEntityPatch(t0001Entities.DARKNESS_ENTITY.get(),DarknessEntityPatch::new);
        event.registerEntityPatch(t0001Entities.AMOGUS.get(), AmogusPatch::new);
        event.registerEntityPatch(t0001Entities.SHADOW_CLONE.get(), ShadowClonePatch::new);
    } //I called this method in main mod class to register it check that out


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerNonExCapWeaponCapabilitiesNStuff(FMLCommonSetupEvent event){
        event.enqueueWork(
                () -> EpicFightEventHooks.Registry.WEAPON_CAPABILITY_PRESET.registerEvent(t0001WeaponCapabilityPresets::registerMovesets)
        );
    }


    @SubscribeEvent
    public static void registerEFAttribute(EntityAttributeModificationEvent event) {
        AmogusPatch.initAttributes(event);
        ShadowClonePatch.initAttributes(event);
        DarknessEntityPatch.initAttributes(event);
    }/* ifykyk */


    //build animation
    @SubscribeEvent
    public static void registerAnimations(AnimationManager.AnimationRegistryEvent event) {
        event.newBuilder(t0001.MODID, CentralAnimationBuild::listen);
    }



}
