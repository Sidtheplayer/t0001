package sid.base.skill;

import yesman.epicfight.skill.SkillCategory;
import yesman.epicfight.skill.SkillSlot;


public enum t0001SkillSlots implements SkillSlot {
    INNER_TRANSITION(t0001SkillCategories.INNER_TRANSITION);

    private final SkillCategory category;
    final int id;


    t0001SkillSlots(SkillCategory category) {
        this.category = category;
        this.id = SkillSlot.ENUM_MANAGER.assign(this);
    }

    @Override
    public SkillCategory category() {
        return this.category;
    }

    @Override
    public int universalOrdinal() {
        return this.id;
    }
}
