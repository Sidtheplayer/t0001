package sid.base.utils;

import sid.base.gameasset.ReusableEvents;
import sid.base.skill.transition_skills.AnomalousLightningTransitionSkill;

public enum RpcPacketIds {

    WHITE_LIGHTNING_VFX(AnomalousLightningTransitionSkill.LightningFXPacketID),
    SEND_VIDEO(VideoRendererUtil.SendVideoToPlayer),
    ResetLivingModifier(ReusableEvents.RLMBIP),
    SEND_TEXTURED_AFTER_IMAGE(ReusableEvents.SendTexturedAfterImage_id);

    public final String id;

    RpcPacketIds(String id){
        this.id = id;
    }

}
