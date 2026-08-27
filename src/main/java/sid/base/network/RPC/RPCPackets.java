package sid.base.network.RPC;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.Objects;
import java.util.UUID;

import static sid.base.utils.VideoRendererUtil.SendVideoToPlayer;

public class RPCPackets {

    //Packet String Constants as Ids
    public static final String RLMBIP = "gske2o34sgsbb6kklmaof43457s";

    public static final String SendTexturedAfterImage_id = "sendtexturedafterimaget0001";


    public static final String playCamAnim = "xk3d5731super";
    public static final String destroyLocalFX = "nuclear_karate";
    public static final String handleKunaiTp = "minato_type_shit";




    //CLIENT BOUND
    @RPCPacket(SendTexturedAfterImage_id)
    public static void sendTexAftrImage(int entityId){
        PacketDelegations.setSendTexturedAfterImage(entityId);
    }

    /**
     * Play a video fullscreen for a specific target entity
     * Video will play when this entity is the local player
     *
     * @param videoLocation ResourceLocation format: "modid:video/filename.mp4"
     * @param PlayerId The entityID this video is for (usually the player who triggered it)
     * @param speed Video playback speed (0.1 to 3.0, normal = 1.0)
     */
    @RPCPacket(SendVideoToPlayer) //NOTE TO SELF: RPCPackets only support parameters listed in https://low-drag-mc.github.io/LowDragMC-Doc/ldlib2/sync/types_support/
    public static void startVideoOnPlayer(String videoLocation, int PlayerId, float speed){
        PacketDelegations.startVidOnClient(videoLocation,PlayerId,speed);
    }

    @RPCPacket(RLMBIP)
    public static void resetLivingMotionModifierByItemPacket(RPCSender sender, UUID playerUUID) {

        //does what the name implies, testing pending
        MinecraftServer server = Objects.requireNonNull(sender.asPlayer()).getServer();
        ServerPlayer serverPlayer = null;

        if (server != null) {
            serverPlayer = server.getPlayerList().getPlayer(playerUUID);
        }

        ServerPlayerPatch serverPlayerPatch = EpicFightCapabilities.getServerPlayerPatch(serverPlayer);

        if (serverPlayerPatch != null) {
            serverPlayerPatch.modifyLivingMotionByCurrentItem(false);
        }


    }



    @RPCPacket(playCamAnim)
    public static void setPlayCamAnim(String AnimName, boolean Loop, boolean LockMouse){
        PacketDelegations.startCamAnimOnClient(AnimName, Loop, LockMouse);
    }

    @RPCPacket(destroyLocalFX)
    public static void DestroyLocalVFX(boolean forceDeath, String fxLocation, int id){
        PacketDelegations.destroyFX(forceDeath, fxLocation, id);
    }

    // SERVER BOUND --
    @RPCPacket(handleKunaiTp)
    public static void HandleKunaiTP(RPCSender entity){
        ServerSidePacketDelegations.handleKunaiTeleport(entity);
    }


}
