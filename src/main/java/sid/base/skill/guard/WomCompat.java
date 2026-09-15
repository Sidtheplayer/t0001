package sid.base.skill.guard;

import net.neoforged.bus.api.IEventBus;
import reascer.wom.gameasset.WOMSkills;
import reascer.wom.gameasset.animations.weapons.AnimsRuine;
import sid.base.gameasset.animations.DragonGodSwordAnimations;
import sid.base.world.capabilities.t0001WeaponCategories;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.registry.SkillBuilderModificationEvent;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.guard.GuardSkill;

public class WomCompat implements ICompatModule {


    public static void onCounterAttackSkillCreation(SkillBuilderModificationEvent event) {

        if (event.getRegistryName().equals(WOMSkills.COUNTER_ATTACK.getId())) {
            if ((event.getSkillBuilder() instanceof GuardSkill.Builder builder)) {
                builder
                        .addGuardMotion(
                                t0001WeaponCategories.DRAGON_GOD_SWORD,
                                (item, player) -> DragonGodSwordAnimations.GUARD_HIT
                        )
                        .addAdvancedGuardMotion(t0001WeaponCategories.DRAGON_GOD_SWORD, (i,p)->
                                        AnimsRuine.RUINE_PUNITION
                                )
                        .addGuardBreakMotion(
                                t0001WeaponCategories.DRAGON_GOD_SWORD,
                                (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED
                        );
            }
        }
    }



    @Override
    public void onModEventBus(IEventBus eventBus) {

    }

    @Override
    public void onGameEventBus(IEventBus eventBus) {
        EpicFightEventHooks.Registry.MODIFY_SKILL_BUILDER.registerEvent(WomCompat::onCounterAttackSkillCreation, 3);
    }

    @Override
    public void onModEventBusClient(IEventBus eventBus) {

    }

    @Override
    public void onGameEventBusClient(IEventBus eventBus) {

    }
}
