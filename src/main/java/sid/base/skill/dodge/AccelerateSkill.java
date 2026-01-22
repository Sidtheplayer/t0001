package sid.base.skill.dodge;


import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.player.ModifyComboCounter;
import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.dodge.DodgeSkill;

public class AccelerateSkill extends DodgeSkill {


    public AccelerateSkill(Builder<?> builder) {
        super(builder);

    }

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container,eventListener);

        eventListener.registerEvent(
                EpicFightEventHooks.Player.MODIFY_COMBO_COUNTER,
                event ->{
                    if(event.getCausal() == ModifyComboCounter.Causal.ANOTHER_ACTION_ANIMATION
                    && event.getAnimation().get().in(this.animations)
                    ){
                        event.setNextValue(event.getPrevValue());
                    }

                },this
        );
    }

    @Override
    public Skill getPriorSkill() {
        return EpicFightSkills.FORBIDDEN_STRENGTH.get();
    }

}
