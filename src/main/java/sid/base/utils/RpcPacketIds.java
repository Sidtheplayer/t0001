package sid.base.utils;

import sid.base.gameasset.ReusableEventsAndUtils;
import sid.base.skill.transition_skills.AnomalousLightningTransitionSkill;

public enum  RpcPacketIds {

  WHITE_LIGHTNING_VFX(AnomalousLightningTransitionSkill.LightningFXPacketID),

    SEND_VIDEO(VideoRendererUtil.SendVideoToPlayer),

    ResetLivingModifier(ReusableEventsAndUtils.RLMBIP),

    /// Parameters: String AnimName, boolean Loop, boolean LockMouse
    SEND_CAM_ANIM(ReusableEventsAndUtils.playCamAnim),

    ///Parameters: boolean forceDeath, String fxLocation, int entityId
    DESTROY_VFX_PACKET(ReusableEventsAndUtils.destroyLocalFX),


    SEND_TEXTURED_AFTER_IMAGE(ReusableEventsAndUtils.SendTexturedAfterImage_id);

    public final String id;

    RpcPacketIds(String id){
        this.id = id;
    }

}
