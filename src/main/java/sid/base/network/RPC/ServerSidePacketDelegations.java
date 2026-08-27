package sid.base.network.RPC;

import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import net.minecraft.server.level.ServerPlayer;
import sid.base.world.entity.JunKunaiEntity;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class ServerSidePacketDelegations {

    public static void handleKunaiTeleport(RPCSender sender){
        ServerPlayer player = sender.asPlayer();
        if (player == null) return;

        // Check if player has an active kunai
        if (JunKunaiEntity.KunaiMap.get(player) != null) {

            JunKunaiEntity.tryTeleportShooterToKunai(player);
        } else {
            // or Else throw a new kunai
            throwKunai(player);
        }
    }

    public static void throwKunai(ServerPlayer player) {
        // Create and shoot the kunai
        JunKunaiEntity kunai = new JunKunaiEntity(player, player.level());

        kunai.setOwner(player);

        ServerPlayerPatch patch = EpicFightCapabilities.getServerPlayerPatch(player);

        if(patch!= null){
            patch.playAnimationSynchronized(Animations.BIPED_JAVELIN_THROW, 0.0f);
        }

        // Shoot in the direction the player is looking
        float speed = 2.5F;
        float divergence = 0.50F;

        kunai.shootFromRotation(
                player,
                player.getXRot(),
                player.getYRot(),
                0.0F,
                speed,
                divergence
        );

        // Add to the world
        player.level().addFreshEntity(kunai);

    }

}
