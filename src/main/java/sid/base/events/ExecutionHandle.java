package sid.base.events;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import sid.base.gameasset.t0001Sounds;
import sid.base.network.CustomSynchedAnimationVariablekeys;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.registry.entries.EpicFightMobEffects;
import yesman.epicfight.registry.entries.EpicFightSynchedAnimationVariableKeys;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.Objects;
import java.util.Set;

public class ExecutionHandle {

    public static void setup_simple_forward_execution(Double forwardOffset , LivingEntity victim, PlayerPatch<?> playerPatch, AnimationManager.AnimationAccessor<? extends StaticAnimation> attackerAnimation, AnimationManager.AnimationAccessor<? extends StaticAnimation> victimAnimation, @Nullable SoundEvent startSound){

        LivingEntityPatch<?> victimPatch = EpicFightCapabilities.getEntityPatch(victim, LivingEntityPatch.class);

        if (victimPatch != null) {

            Vec3 playerEyePos = playerPatch.getOriginal().getEyePosition();
            Vec3 playerLookVec = playerPatch.getOriginal().getLookAngle().normalize();


            // Calculate teleport position in front of player
            Vec3 tpPos = playerEyePos.add(playerLookVec.scale(forwardOffset));
            // this code is cursed af T^T

            victim.lookAt(EntityAnchorArgument.Anchor.EYES, playerEyePos.multiply(new Vec3(-1, 1, -1)));
            victim.setYRot(victim.getYHeadRot());

            if (victimPatch instanceof ServerPlayerPatch serverPlayerPatch) {

                serverPlayerPatch.setModelYRot(victim.getYRot(), true);

            } else victim.setYBodyRot(victim.getYRot());

            // Teleport victim
            victim.teleportTo(tpPos.x, playerPatch.getOriginal().getY(), tpPos.z);

            Vec3 attackerEyePos = victim.getEyePosition();
            LivingEntity player = playerPatch.getOriginal();


            player.lookAt(EntityAnchorArgument.Anchor.EYES, attackerEyePos.multiply(new Vec3(-1, 1, -1)));
            player.setYRot(player.getYHeadRot());
            player.yBodyRot = player.getYRot();

            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundPlayerPositionPacket(
                        player.getX(), player.getY(), player.getZ(),
                        player.getYRot(), player.getXRot(),
                        Set.of(),
                        0
                ));

                // Broadcast to other players in range
                ClientboundMoveEntityPacket.Rot rotPacket = new ClientboundMoveEntityPacket.Rot(
                        player.getId(),
                        (byte) Mth.floor(player.getYRot() * 256.0F / 360.0F),
                        (byte) Mth.floor(player.getXRot() * 256.0F / 360.0F),
                        player.onGround()
                );

                Objects.requireNonNull(serverPlayer.getServer()).getPlayerList().getPlayers().forEach(otherPlayer -> {
                    if (otherPlayer != serverPlayer) {
                        otherPlayer.connection.send(rotPacket);
                    }
                });
            }

            playerPatch.getAnimator().getVariables().put(
                    EpicFightSynchedAnimationVariableKeys.TARGET_ENTITY.get(),
                    attackerAnimation,
                    victim.getId()
            );

            victimPatch.getAnimator().getVariables().put(
                    CustomSynchedAnimationVariablekeys.KILLER_ENTITY.get(),
                    victimAnimation,
                    playerPatch.getId()
            );

            victim.addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY, 120, 2));
            victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 42069, 10));

            victimPatch.playAnimationSynchronized(victimAnimation, 0.121F); //prev 0.121
            playerPatch.playAnimationSynchronized(attackerAnimation, 0.0F);
            if (startSound != null) {
                playerPatch.getLevel().playSound(null, playerPatch.getOriginal().getOnPos(), startSound, SoundSource.PLAYERS, 150f, 1f);
            }


        } else victim.knockback(30.0F, victim.xOld, victim.zOld);
    }
}

