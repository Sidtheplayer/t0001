package sid.base.utils;

import sid.base.skill.transition_skills.AnomalousLightningTransitionSkill;

public enum VFX_Ids {

    WHITE_LIGHTNING(AnomalousLightningTransitionSkill.LightningFXPacketID);

    public final String id;

    VFX_Ids(String id){
        this.id = id;
    }

}
