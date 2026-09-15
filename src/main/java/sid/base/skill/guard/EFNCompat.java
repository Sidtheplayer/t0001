package sid.base.skill.guard;

import com.hm.efn.gameasset.animations.EFNSkillAnimations;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import sid.base.gameasset.animations.DragonGodSwordAnimations;
import sid.base.world.capabilities.t0001WeaponCategories;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.registry.SkillBuilderModificationEvent;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.guard.GuardSkill;

import java.util.List;

public class EFNCompat implements ICompatModule {

    public static void onEnhancedParry(SkillBuilderModificationEvent event) {
        if (event.getRegistryName().equals(ResourceLocation.fromNamespaceAndPath("efn", "efn_parry"))) {
            if (event.getSkillBuilder() instanceof GuardSkill.Builder builder) {
                builder

                        .addGuardMotion(
                                t0001WeaponCategories.DRAGON_GOD_SWORD,
                                (i, p) -> DragonGodSwordAnimations.GUARD_HIT
                        ).addGuardBreakMotion(t0001WeaponCategories.DRAGON_GOD_SWORD,
                                (i, p) -> Animations.GREATSWORD_GUARD_BREAK)


                        .addAdvancedGuardMotion(t0001WeaponCategories.DRAGON_GOD_SWORD, ((capabilityItem, pp) ->
                                List.of(EFNSkillAnimations.EFN_GUARD_ACTIVE_HIT1, EFNSkillAnimations.EFN_GUARD_ACTIVE_HIT2, EFNSkillAnimations.EFN_GUARD_ACTIVE_HIT3)))


                ;

            }
        }
    }


    @Override
    public void onModEventBus(IEventBus eventBus) {
        EpicFightEventHooks.Registry.MODIFY_SKILL_BUILDER.registerEvent(EFNCompat::onEnhancedParry, 3);
    }

    @Override
    public void onGameEventBus(IEventBus eventBus) {

    }

    @Override
    public void onModEventBusClient(IEventBus eventBus) {

    }

    @Override
    public void onGameEventBusClient(IEventBus eventBus) {

    }
}
