package sid.t0001.client.compats;

import com.hm.efn.gameasset.EFNAnimations;
import com.hm.efn.gameasset.animations.EFNSkillAnimations;
import com.hm.efn.skill.guard.EFNParryingSkill;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import sid.t0001.gameasset.animations.DragonGodSwordAnimations;
import sid.t0001.world.capabilities.t0001WeaponCategories;
import sid.t0001.world.item.t0001Items;
import yesman.epicfight.api.client.forgeevent.WeaponCategoryIconRegisterEvent;
import yesman.epicfight.api.forgeevent.SkillBuildEvent;
import yesman.epicfight.api.forgeevent.WeaponCapabilityPresetRegistryEvent;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.guard.GuardSkill;

import java.util.List;

public class Efncompat implements ICompatModule {


    public static void onEFNParryCreation(SkillBuildEvent.ModRegistryWorker.SkillCreateEvent<GuardSkill.Builder> event){

        if(event.getRegistryName().equals(ResourceLocation.fromNamespaceAndPath("efn","efn_parry"))){
             GuardSkill.Builder builder = event.getSkillBuilder();

             builder.addGuardMotion(t0001WeaponCategories.DRAGON_GOD_SWORD,(capabilityItem,playerPatch)-> DragonGodSwordAnimations.GUARD_HIT)
                     .addGuardBreakMotion(t0001WeaponCategories.DRAGON_GOD_SWORD,(six,nine)-> Animations.BIPED_COMMON_NEUTRALIZED)

                     .addAdvancedGuardMotion(t0001WeaponCategories.DRAGON_GOD_SWORD,(four,twenty)-> List.of(EFNSkillAnimations.EFN_GUARD_ACTIVE_HIT1,EFNSkillAnimations.EFN_GUARD_ACTIVE_HIT2,EFNSkillAnimations.EFN_GUARD_ACTIVE_HIT3));
        }

    }

    public static void IconCreation(WeaponCategoryIconRegisterEvent evt){
        evt.registerCategory(t0001WeaponCategories.DRAGON_GOD_SWORD, t0001Items.DRAGON_GOD_SWORD.get());
    }



    @Override
    public void onModEventBus(IEventBus eventBus) {
        eventBus.addGenericListener(
                GuardSkill.Builder.class,
                EventPriority.NORMAL,
                Efncompat::onEFNParryCreation
        );
    }

    @Override
    public void onForgeEventBus(IEventBus eventBus) {
    }

    @Override
    public void onModEventBusClient(IEventBus eventBus) {
        eventBus.addListener(Efncompat::IconCreation);
    }

    @Override
    public void onForgeEventBusClient(IEventBus eventBus) {

    }
}
