package sid.t0001.utils;

import yesman.epicfight.skill.SkillContainer;

public class misc_utils {

    public static  boolean IsContainerCreative(SkillContainer container){
        return container.getExecutor().getOriginal().isCreative();
    }


}
