package sid.base.network.RPC;

import sid.base.skill.transition_skills.AnomalousLightningTransitionSkill;
import sid.base.utils.VideoRendererUtil;

@SuppressWarnings("unused")
public enum  RpcPacketIds {

  WHITE_LIGHTNING_VFX(AnomalousLightningTransitionSkill.LightningFXPacketID),

    SEND_VIDEO(VideoRendererUtil.SendVideoToPlayer),

    ResetLivingModifier(RPCPackets.RLMBIP),

    /// Parameters: String AnimName, boolean Loop, boolean LockMouse
    SEND_CAM_ANIM(RPCPackets.playCamAnim),

    ///Parameters: boolean forceDeath, String fxLocation, int entityId
    DESTROY_VFX_PACKET(RPCPackets.destroyLocalFX),

    /// SERVER BOUND PAYLOAD
    TRIGGER_KUNAI_TELEPORT(RPCPackets.handleKunaiTp),

    SEND_AFTERIMAGE(RPCPackets.SendAfterImage_id),

    SEND_TEXTURED_AFTER_IMAGE(RPCPackets.SendTexturedAfterImage_id);



    public final String id;

    RpcPacketIds(String id){
        this.id = id;
    }

}
