package sid.base.skill.awakening;

import sid.base.skill.t0001SkillDataKeys;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;

public class InfiniteAwakening extends AwakeningSkill{

    public InfiniteAwakening(SkillBuilder<?> builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);

        var data_manager = container.getDataManager();

        eventListener.registerContextAwareEvent(EpicFightEventHooks.Player.TICK_EPICFIGHT_MODE,
                (evt, context) -> {

                    data_manager.setDataSync(t0001SkillDataKeys.IS_AWAKENED,true);


                }, this

        );


    }


}
