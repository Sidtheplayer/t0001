package sid.base.network.RPC;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import sid.base.events.QuickTimeEventHandle;
import sid.base.gameasset.animations.MiscAnimations;
import sid.base.world.entity.JunKunaiEntity;
import sid.base.world.t0001Sounds;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;

public class ServerSidePacketDelegations {

    public static void handleQuickTime(RPCSender sender, QuickTimeEventHandle.QuickTimeResult result) {
        ServerPlayer player = sender.asPlayer();

        if (result.QuickTimeContext().equals("phantom_severance")) {
            if (!result.isQTESuccess()) {
                if (player != null) {

                    player.playNotifySound(t0001Sounds.UI_ERROR.value(), SoundSource.MASTER, 3f, 1f);

                    LivingEntity lastAttacker = player.getLastHurtByMob();

                    if (lastAttacker == null) {
                        player.hurt(player.damageSources().genericKill(), Float.MAX_VALUE);

                    } else {
                        EpicFightDamageSource damageSource = lastAttacker instanceof Player enemy
                                ? EpicFightDamageSources.playerAttack(enemy)
                                : EpicFightDamageSources.mobAttack(lastAttacker);
                        damageSource.addRuntimeTag(DamageTypeTags.BYPASSES_INVULNERABILITY);
                        player.hurt(damageSource, Float.MAX_VALUE);
                    }

                    ServerPlayerPatch patch = EpicFightCapabilities.getServerPlayerPatch(player);

                    if (patch != null) {

                        patch.playAnimationSynchronized(MiscAnimations.RAG_DOLL_STUN_UP, 0.0f);

                    }

                    RPCPacketDistributor.rpcToPlayer(player, RpcPacketIds.DESTROY_VFX_PACKET.id, true, "photon:phantom_realm", player.getId());
                }
            } else if (player != null) {
                player.playNotifySound(SoundEvents.COMPARATOR_CLICK, SoundSource.NEUTRAL, 3f, 1f);
                player.playNotifySound(SoundEvents.LARGE_AMETHYST_BUD_PLACE, SoundSource.NEUTRAL, 3f, 1f);
            }
        }

        if (result.QuickTimeContext().equals("test_quickTime")) {

            if (player == null) return;

            if (!result.isQTESuccess()) {
                player.playNotifySound(t0001Sounds.UI_ERROR.value(), SoundSource.MASTER, 10f, 1f);
            } else {
                player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.MASTER, 10f, 1f);
            }
        }
    }


    public static void handleKunaiTeleport(RPCSender sender) {
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

        if (patch != null) {
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
