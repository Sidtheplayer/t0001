package sid.base.skill.awakening;

import sid.base.skill.t0001SkillCategories;
import sid.base.world.item.t0001Tab;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;

public class AwakeningSkill extends Skill {

    public AwakeningSkill(SkillBuilder<?> builder) {
        super(builder);
        builder.setCreativeTab(t0001Tab.T0001_TAB.get());
        builder.setCategory(t0001SkillCategories.AWAKENING);
        builder.setResource(Resource.NONE);
    }




}
