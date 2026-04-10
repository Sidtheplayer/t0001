package sid.base.skill.awakening;

import sid.base.skill.t0001SkillCategories;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;

import java.util.function.Function;

public class AwakeningSkill extends Skill {

    public AwakeningSkill(SkillBuilder<?> builder) {
        super(builder);
    }

    public static SkillBuilder<?> createAwakeningSkillBuilder(Function<SkillBuilder<?>, ? extends AwakeningSkill> constructor) {
        return new SkillBuilder<>(constructor)
                .setCategory(t0001SkillCategories.AWAKENING)
                .setResource(Resource.NONE);
    }




}
