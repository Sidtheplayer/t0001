package sid.base.utils;

import sid.base.skill.transition_skills.AnomalousLightningTransitionSkill;

public enum RpcPacketIds {

    WHITE_LIGHTNING_VFX(AnomalousLightningTransitionSkill.LightningFXPacketID);


    public final String id;

    RpcPacketIds(String id){
        this.id = id;
    }

}
