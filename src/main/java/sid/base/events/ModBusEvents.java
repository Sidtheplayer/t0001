package sid.base.events;


import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import sid.base.gameasset.animations.CentralAnimationBuild;
import sid.base.gameasset.t0001Entities;
import sid.base.main.t0001;
import sid.base.skill.OtherSkillsCompatBuilding;
import sid.base.world.capabilities.item.t0001WeaponCapabilityPresets;
import sid.base.world.entity.Amogus;
import sid.base.world.entity.AmogusPatch;
import sid.base.world.entity.DarknessEntity;
import sid.base.world.entity.DarknessEntityPatch;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.registry.EntityPatchRegistryEvent;

@EventBusSubscriber(modid= t0001.MODID)
public class ModBusEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(t0001Entities.AMOGUS.get(), Amogus.createAttributes().build());
        event.put(t0001Entities.DARKNESS_ENTITY.get(), DarknessEntity.createAttributes().build()); // OMG AMOGUS!
    }//register amogus vanilla attributes


    public static void registerEntityPatch(EntityPatchRegistryEvent event) {
        event.registerEntityPatch(t0001Entities.DARKNESS_ENTITY.get(),DarknessEntityPatch::new);
        event.registerEntityPatch(t0001Entities.AMOGUS.get(),AmogusPatch::new);
    }
    // you also have to put register renderer in renderengine
    // you know what it says


    @SubscribeEvent
    public static void OnModConstruction(FMLConstructModEvent event){
        EpicFightEventHooks.Registry.ENTITY_PATCH.registerEvent(ModBusEvents::registerEntityPatch);

        //Tried registering it in main mod class, commonsetup event, used icompat module nothing works
        EpicFightEventHooks.Registry.MODIFY_SKILL_BUILDER.registerEvent(OtherSkillsCompatBuilding::onGuardSkillCreation,-1);
        EpicFightEventHooks.Registry.MODIFY_SKILL_BUILDER.registerEvent(OtherSkillsCompatBuilding::onParrySkillCreation,-1);
        EpicFightEventHooks.Registry.MODIFY_SKILL_BUILDER.registerEvent(OtherSkillsCompatBuilding::onSwordMasterSkillCreation,-1);


        EpicFightEventHooks.Registry.WEAPON_CAPABILITY_PRESET.registerEvent(t0001WeaponCapabilityPresets::registerMovesets);

    }




    @SubscribeEvent
    public static void registerEFAttribute(EntityAttributeModificationEvent event) {
        AmogusPatch.initAttributes(event);
        DarknessEntityPatch.initAttributes(event);
    }/* ifykyk */


    //build animation
    @SubscribeEvent
    public static void registerAnimations(AnimationManager.AnimationRegistryEvent event) {
        event.newBuilder(t0001.MODID, CentralAnimationBuild::listen);
    }



}
