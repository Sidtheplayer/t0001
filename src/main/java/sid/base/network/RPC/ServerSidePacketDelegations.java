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

        JunKunaiEntity kunai = JunKunaiEntity.KunaiMap.get(player);

        if (kunai != null && kunai.isAlive()) {
            JunKunaiEntity.tryTeleportShooterToKunai(player);
        } else {
            JunKunaiEntity.KunaiMap.remove(player);
            throwKunai(player);
        }

    }

    public static void throwKunai(ServerPlayer player) {

        JunKunaiEntity existing = JunKunaiEntity.KunaiMap.get(player);

        if (existing != null && existing.isAlive()) {
            return;
        }

        // Remove stale reference
        JunKunaiEntity.KunaiMap.remove(player);


        // Create and shoot
        JunKunaiEntity kunai = new JunKunaiEntity(player, player.level());

        kunai.setOwner(player); //fail-safe

        ServerPlayerPatch patch = EpicFightCapabilities.getServerPlayerPatch(player);

        if(patch != null){
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

        JunKunaiEntity.KunaiMap.put(player, kunai);


        // Add to the world
        player.level().addFreshEntity(kunai);


    }

}
