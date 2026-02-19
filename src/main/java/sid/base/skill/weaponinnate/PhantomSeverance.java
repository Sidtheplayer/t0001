package sid.base.skill.weaponinnate;

import net.minecraft.nbt.CompoundTag;
import sid.base.gameasset.animations.DragonGodSwordAnimations;
import sid.base.skill.t0001SkillDataKeys;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.identity.RevelationSkill;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;


public class PhantomSeverance extends WeaponInnateSkill {

    public PhantomSeverance(Builder<?> builder) {
        super(builder);
    }




    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);

        var data_manager = container.getDataManager();

        eventListener.registerContextAwareEvent(EpicFightEventHooks.Player.CAST_SKILL,
                (event, eventContext) -> {

                    Skill skill = event.getSkillContainer().getSkill(); //imp

                    boolean activate = (skill.getCategory() == SkillCategories.BASIC_ATTACK);

                    if(!data_manager.hasData(t0001SkillDataKeys.ACTIVATION_KEY)){return;}

                    boolean activation_key = data_manager.getDataValue(t0001SkillDataKeys.ACTIVATION_KEY);

                    if (!activation_key && activate) {
                        EpicFightCapabilities.getUnparameterizedEntityPatch(container.getExecutor().getTarget(), LivingEntityPatch.class).ifPresent(entitypatch -> {
                            {
                                if (container.sendCastRequest(container.getClientExecutor(), ControlEngine.getInstance()).isExecutable()) {
                                    data_manager.setDataSync(t0001SkillDataKeys.ACTIVATION_KEY,true);
                                    container.activate();
                                    event.cancel();
                                }
                            }

                        });

                    }



                }, this);


    }

    @Override
    public void executeOnServer(SkillContainer container, CompoundTag args) {
        super.executeOnServer(container, args);

        if (container.getDataManager().getDataValue(t0001SkillDataKeys.ACTIVATION_KEY)) {
            container.getServerExecutor().playAnimationSynchronized(DragonGodSwordAnimations.TOO_EASY_RUN,0.2f);
        }else {
            container.getServerExecutor().playAnimationSynchronized(DragonGodSwordAnimations.TOO_EASY_STRIKE,0.0f);
        }


    }
}
