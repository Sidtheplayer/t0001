package sid.base.utils;

import sid.base.gameasset.ReusableEventsAndUtils;
import sid.base.skill.transition_skills.AnomalousLightningTransitionSkill;

public enum RpcPacketIds {

    WHITE_LIGHTNING_VFX(AnomalousLightningTransitionSkill.LightningFXPacketID),
    SEND_VIDEO(VideoRendererUtil.SendVideoToPlayer),
    ResetLivingModifier(ReusableEventsAndUtils.RLMBIP),
    SEND_CAM_ANIM(ReusableEventsAndUtils.playCamAnim),
    SEND_TEXTURED_AFTER_IMAGE(ReusableEventsAndUtils.SendTexturedAfterImage_id);

    public final String id;

    RpcPacketIds(String id){
        this.id = id;
    }

}
