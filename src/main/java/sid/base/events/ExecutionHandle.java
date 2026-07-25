package sid.base.events;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import sid.base.gameasset.animations.CustomSynchedAnimationVariablekeys;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.registry.entries.EpicFightMobEffects;
import yesman.epicfight.registry.entries.EpicFightSynchedAnimationVariableKeys;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutionHandle {

    public static ThreadLocal<ConcurrentHashMap<LivingEntity, LivingEntity>> executionMap = new ThreadLocal<>();

    public static void setup_simple_forward_execution(
            LivingEntity victim,
            LivingEntityPatch<?> playerPatch,
            AnimationManager.AnimationAccessor<? extends StaticAnimation> attackerAnimation,
            AnimationManager.AnimationAccessor<? extends StaticAnimation> victimAnimation,
            @Nullable SoundEvent startSound,
            float rotationOffsetForVictim,
            Vec3 positionOffset,
            boolean inverseEyePos
    ) {
        LivingEntityPatch<?> victimPatch = EpicFightCapabilities.getEntityPatch(victim, LivingEntityPatch.class);

        if (victimPatch != null) {
            LivingEntity attacker = playerPatch.getOriginal();
            Vec3 attackerEyePos = attacker.getEyePosition();

            Vec3 finalEyePos = inverseEyePos ? attackerEyePos.multiply(-1,1,-1) : attackerEyePos;
            //calculate local-space relative coordinate
            Vec3 lookAngle = attacker.getLookAngle();
            Vec3 horizontal = new Vec3(lookAngle.x, 0, lookAngle.z);
            Vec3 forward = horizontal.lengthSqr() > 1.0E-6
                    ? horizontal.normalize()
                    : new Vec3(Mth.sin(-attacker.getYRot() * Mth.DEG_TO_RAD), 0,
                    Mth.cos(attacker.getYRot() * Mth.DEG_TO_RAD));

            Vec3 up = new Vec3(0, 1, 0);
            Vec3 right = forward.cross(up).normalize();

            //convert to world space and add relative offset
            Vec3 worldOffset = right.scale(positionOffset.x)
                    .add(forward.scale(positionOffset.z));

            Vec3 tpPos = finalEyePos.add(worldOffset);
            double baseY = attacker.getY() + positionOffset.y;

            victim.teleportTo(tpPos.x, baseY, tpPos.z);

            victim.lookAt(EntityAnchorArgument.Anchor.EYES, finalEyePos);
            float victimYaw = victim.getYHeadRot() + rotationOffsetForVictim;
            victim.setYRot(victimYaw);

            if (victimPatch instanceof ServerPlayerPatch serverPlayerPatch) {
                serverPlayerPatch.toEpicFightMode(true);
                serverPlayerPatch.setModelYRot(victimYaw, true);
            } else {
                victim.setYBodyRot(victimYaw);
            }


            Vec3 victimEyePos = victim.getEyePosition();
            attacker.lookAt(EntityAnchorArgument.Anchor.EYES, victimEyePos);
            float attackerYaw = attacker.getYHeadRot();
            attacker.setYRot(attackerYaw);
            attacker.yBodyRot = attackerYaw;

            if (attacker instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundPlayerPositionPacket(
                        attacker.getX(), attacker.getY(), attacker.getZ(),
                        attackerYaw, attacker.getXRot(),
                        Set.of(), 0
                ));

                ClientboundMoveEntityPacket.Rot rotPacket = new ClientboundMoveEntityPacket.Rot(
                        attacker.getId(),
                        (byte) Mth.floor(attackerYaw * 256.0F / 360.0F),
                        (byte) Mth.floor(attacker.getXRot() * 256.0F / 360.0F),
                        attacker.onGround()
                );

                var server = serverPlayer.getServer();
                if (server != null) {
                    for (ServerPlayer otherPlayer : server.getPlayerList().getPlayers()) {
                        if (otherPlayer != serverPlayer && otherPlayer.distanceToSqr(attacker) < 16384) {
                            otherPlayer.connection.send(rotPacket);
                        }
                    }
                }
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

            victimPatch.playAnimationSynchronized(victimAnimation, 0.0F);
            playerPatch.playAnimationSynchronized(attackerAnimation, 0.0F);

            // putExecEntitiesInMap(attacker, victim);

            if (startSound != null) {
                attacker.level().playSound(null, attacker.getOnPos(), startSound, SoundSource.PLAYERS, 150f, 1f);
            }
        } else {
            double dx = victim.getX() - playerPatch.getOriginal().getX();
            double dz = victim.getZ() - playerPatch.getOriginal().getZ();
            victim.knockback(30.0F, dx, dz);
        }
    }

    public static void putExecEntitiesInMap(LivingEntity attacker, LivingEntity victim){
        executionMap.get().put(attacker,victim);
    }

    public static LivingEntity getVictim(LivingEntity attacker){
     return     executionMap.get().get(attacker);
    }

    public static LivingEntity getAttacker(LivingEntity Victim){
        return     executionMap.get().get(Victim);
    }

    //and i plan to put this in a Server tick Event
    public static void SnapToValidTerrainWhileExecution(){
        executionMap.get().forEach( (attacker,victim) -> {
            if(victim.getY() < attacker.getY()){
                victim.teleportTo(victim.getX(), attacker.getY(), victim.getZ());
            }
        });
    }

}

