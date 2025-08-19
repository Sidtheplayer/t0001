package sid.t0001.skill.dodge;

import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.dodge.DodgeSkill;
import yesman.epicfight.world.entity.eventlistener.ComboCounterHandleEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.UUID;

public class AccelerateSkill extends DodgeSkill {
    private static final UUID EVENT_UUID = UUID.fromString("057f53bf-ed2c-4d6a-92eb-f7976f83ad94");

    public AccelerateSkill(DodgeSkill.Builder builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container) {
        container.getExecutor().getEventListener().addEventListener(
                PlayerEventListener.EventType.COMBO_COUNTER_HANDLE_EVENT,
                EVENT_UUID,
                (event) -> {
                    if (event.getCausal() == ComboCounterHandleEvent.Causal.ANOTHER_ACTION_ANIMATION
                            && event.getAnimation().get().in(this.animations)) {
                        event.setNextValue(event.getPrevValue());
                    }

                }
        );
    }

    @Override
    public Skill getPriorSkill() {
        return EpicFightSkills.FORBIDDEN_STRENGTH;
    }
}
