package sid.base.gameasset;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import sid.base.network.CustomSynchedAnimationVariablekeys;
import sid.base.network.PacketDelegations;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import sid.base.particle.t0001Particles;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static sid.base.utils.VideoRendererUtil.SendVideoToPlayer;

//its a big jungle

public class ReusableEventsAndUtils {

    public static final String RLMBIP = "gske2o34sgsbb6kklmaof43457s";
    public static final String SendTexturedAfterImage_id = "sendtexturedafterimaget0001";


    /// made for converting photon fx time gotten from delay-testing fx in minecraft to anim time
    public static float getAnimTimeFromTickTime(int ticks) {
        return (float) ticks / 20;
    }

    /// assumes animation to be at 60 frames per second
    public static float getAnimTimeFromFrame(int frame) {
        return (float) frame / 60;
    }


    public static void sendBypassedChatMessage(EntityPatch<?> entityPatch, String words) {
        ServerLevel level = Objects.requireNonNull(entityPatch.getOriginal().getServer()).getLevel(entityPatch.getLevel().dimension());
        if (level == null) return;

        LivingEntity sender = (LivingEntity) entityPatch.getOriginal();

        Component message = Component.literal(sender.getScoreboardName() + ": " + words);

        Vec3 senderPos = sender.position();

        AABB searchBox = AABB.ofSize(senderPos, 10, 10, 10);

        for (Player player : level.getNearbyPlayers(TargetingConditions.forNonCombat(), sender, searchBox)) {
            if (!player.equals(sender)) {
                player.sendSystemMessage(message);
            }
        }
        sender.sendSystemMessage(message);
    }

    public static final AnimationEvent.E0 AFTER_IMAGE = (entitypatch, self, params) -> {
        LivingEntity entity = entitypatch.getOriginal();
        entity.level().addParticle(
                EpicFightParticles.WHITE_AFTERIMAGE.get(),
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                Double.longBitsToDouble(entity.getId()),
                0,
                0
        );
    };

    //we use custom afterimage because a lot of normal epicfight afterimages in short time will crash my fucking pc and probably yours too

    public static final AnimationEvent.E0 FASTER_AFTERIMAGE = (entitypatch, self, params) -> {
        LivingEntity entity = entitypatch.getOriginal();

        Particle particle = Minecraft.getInstance().particleEngine.createParticle(
                t0001Particles.TEX_AFTERIMAGE.get(),
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                Double.longBitsToDouble(entity.getId()),
                0,
                0
        );

        if(particle != null){
            particle.setLifetime(9);
        }

    };




    ///A CustomSynchedAnimationVariablekeys.KILLER_ENTITY Needs to be manually put by the dev somehow as I did in fang counter skill for this to properly work
    public static final AnimationEvent.E0 KillandCredit = ((e,s,p) -> {

        Optional<Integer> killerId = e.getAnimator().getVariables().get(CustomSynchedAnimationVariablekeys.KILLER_ENTITY.get(), s.get().getRealAnimation());
        if (killerId.isEmpty()) {
            return;
        }
        Entity attackerEntity = e.getLevel().getEntity(killerId.get());
        if (!(attackerEntity instanceof LivingEntity attacker)) {
            return;
        }
        LivingEntity target = e.getOriginal();
        if (target.level().isClientSide()) {
            return;
        }
        if (!target.isAlive()) {
            return;
        }
        if (target.getPersistentData().getBoolean("execution_complete")) {
            return;
        }
        target.getPersistentData().putBoolean("execution_complete", true);
        float damage = target.getMaxHealth() * 6.0F;
        MinecraftServer server = target.getServer();
        if (server == null) {
            return;
        }
        server.execute(() -> {
            if (!target.isAlive()) {return;}
            if (attacker instanceof ServerPlayer player) {
                target.hurt(target.damageSources().playerAttack(player), damage);
            } else {
                target.hurt(target.damageSources().mobAttack(attacker), damage);
            }
        });
    });

    public static final AnimationEvent.E0 modifyLivingMotionModifierByItem = ((e, s, p) -> {

        if (e instanceof ServerPlayerPatch serverPlayerPatch) {
            serverPlayerPatch.modifyLivingMotionByCurrentItem(false);
        }

    });

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

    public static final String playCamAnim = "xk3d5731super";
    public static final String destroyLocalFX = "nuclear_karate";

    @RPCPacket(playCamAnim)
    public static void setPlayCamAnim(String AnimName, boolean Loop, boolean LockMouse){
        PacketDelegations.startCamAnimOnClient(AnimName, Loop, LockMouse);
    }

    @RPCPacket(destroyLocalFX)
    public static void DestroyLocalVFX(boolean forceDeath, String fxLocation, int id){
        PacketDelegations.destroyFX(forceDeath, fxLocation, id);
    }



    // most of the entityfx code is removed because they keep crashing in dedicated server and i thought its not a big deal
    /** thanks to yonchi for this code  😉 */
        public static class JointTrack {
            public static Vec3 getJointWithTranslation(LocalPlayer renderer, Entity ent, Vec3f translation, Joint joint) {
                if (renderer != null && ent != null && translation != null) {
                    if (renderer.level().isClientSide) {
                        LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(ent, LivingEntityPatch.class);
                        if (entitypatch != null) {
                            float interpolation = 0.0F;
                            OpenMatrix4f transformMatrix;
                            transformMatrix = entitypatch.getArmature().getBoundTransformFor(entitypatch.getAnimator().getPose(interpolation), joint);
                            transformMatrix.translate(translation);
                            OpenMatrix4f.mul((new OpenMatrix4f()).rotate(-((float) Math.toRadians(entitypatch.getOriginal().yBodyRotO + 180.0F)), new Vec3f(0.0F, 1.0F, 0.0F)), transformMatrix, transformMatrix);
                            return new Vec3(
                                    (double) transformMatrix.m30 + (entitypatch.getOriginal()).getX(),
                                    (double) transformMatrix.m31 + ((entitypatch.getOriginal()).getY() + (ent.getBbHeight() / 1.8) - 1),
                                    (double) transformMatrix.m32 + (entitypatch.getOriginal()).getZ()
                            );
                        }
                    }
                }
                return null;
            }

        public static Quaternionf getJointRotationInTime(LivingEntity entity, Joint joint) {
            if (entity == null || joint == null) {
                return null;
            }

            LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
            if (entitypatch != null && entitypatch.getArmature() != null) {

                Pose currentPose = entitypatch.getAnimator().getPose(0.1f);

                OpenMatrix4f jointTransform = entitypatch.getArmature().getBoundTransformFor(currentPose, joint);

                float interpolatedBodyRot = entity.yBodyRotO + (entity.yBodyRot - entity.yBodyRotO) * 0.1f;

                float angleRad = -((float) Math.toRadians(interpolatedBodyRot + 180.0F));
                OpenMatrix4f rotationMatrix = (new OpenMatrix4f()).rotate(angleRad, new Vec3f(0.0F, 1.0F, 0.0F));

                OpenMatrix4f.mul(rotationMatrix, jointTransform, jointTransform);

                Matrix4f jomlMatrix = new Matrix4f(
                        jointTransform.m00, jointTransform.m01, jointTransform.m02, jointTransform.m03,
                        jointTransform.m10, jointTransform.m11, jointTransform.m12, jointTransform.m13,
                        jointTransform.m20, jointTransform.m21, jointTransform.m22, jointTransform.m23,
                        jointTransform.m30, jointTransform.m31, jointTransform.m32, jointTransform.m33
                );

                return new Quaternionf().setFromUnnormalized(jomlMatrix);
            }
            return null;
        }

        public static Vec3 getjointpos(LivingEntity entity,Joint joint,Vec3f translation){
            LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
            if (entitypatch!=null) {
                OpenMatrix4f transformMatrix = entitypatch.getArmature().getBoundTransformFor(entitypatch.getAnimator().getPose(0.0F), joint);
                transformMatrix.translate(translation);
                OpenMatrix4f.mul((new OpenMatrix4f()).rotate(-((float) Math.toRadians(entitypatch.getOriginal().yBodyRotO + 180.0F)), new Vec3f(0.0F, 1.0F, 0.0F)), transformMatrix, transformMatrix);
                return new Vec3(
                        (double) transformMatrix.m30 + (entitypatch.getOriginal()).getX(),
                        (double) transformMatrix.m31 + ((entitypatch.getOriginal()).getY() + (entity.getBbHeight() / 1.8) - 1),
                        (double) transformMatrix.m32 + (entitypatch.getOriginal()).getZ()
                );
            }
            return null;
        }
    }

        /**
         * replaces item with another item on breakage, for now handle the detection of item breakage on your own
         *
         * @param entity    if you don't know this uninstall rn!!$
         * @param itemStack use .get().getdefaultinstance if you cant get itemstack(item) to work.
         * @param breaksound custom break sound
         */
        public static void handleBreak(LivingEntity entity, ItemStack itemStack, SoundEvent breaksound) {
            LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);

            SoundEvent sound = breaksound != null ? breaksound : EpicFightSounds.NEUTRALIZE_MOBS.get();

            if (patch != null) {
                patch.getOriginal().setItemInHand(InteractionHand.MAIN_HAND, itemStack);
                Level level = entity.level();
                level.playSound(
                        null,
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        sound,
                        SoundSource.PLAYERS,
                        1.0F,
                        0.75F
                );
                level.playSound(
                        null,
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        EpicFightSounds.CLASH.get(),
                        SoundSource.PLAYERS,
                        1.0F,
                        4.55F
                );
                //todo:add broken sword fragments flying effect
            }
        }


        /**
         * replaces item with another item on breakage, for now handle the detection of item breakage on your own
         *
         * @param entity    if you don't know this uninstall rn!!$
         * @param itemStack use .get().getdefaultinstance if you cant get itemstack(item) to work.
         *
         */
        public static void handleBreak(LivingEntity entity, ItemStack itemStack) {
            LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);

            SoundEvent sound = EpicFightSounds.NEUTRALIZE_MOBS.get();

            if (patch != null) {
                patch.getOriginal().setItemInHand(InteractionHand.MAIN_HAND, itemStack);
                Level level = entity.level();
                level.playSound(
                        null,
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        sound,
                        SoundSource.PLAYERS,
                        1.0F,
                        0.55F
                );
            }


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




}

