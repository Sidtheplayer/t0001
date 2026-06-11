package sid.base.utils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.lowdragmc.photon.client.fx.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.watermedia.WaterMedia;
import sid.base.client.events.CameraAnimator;
import sid.base.gameasset.ReusableEventsAndUtils;
import sid.base.main.t0001;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.Objects;

import static sid.base.gameasset.ReusableEventsAndUtils.getAnimTimeFromFrame;


/// Client only use cases
public abstract class ReusableAnimEvents {

    public static SkillContainer getLocalSkillContainer(Skill skill){
        LocalPlayerPatch localPlayerPatch = EpicFightCapabilities.getLocalPlayerPatch(Minecraft.getInstance().player);
        if (localPlayerPatch != null) {
            return localPlayerPatch.getSkill(skill);
        }
        return null;
    }

    public static boolean localPlayerHasSkill(Skill skill){
        return ReusableAnimEvents.getLocalSkillContainer(skill) != null;
    }


    ///Table to map entityId and runtimes to destroy or manage outside the origin, only one runtime per fx at time can exist
    public static final Table<Integer, String, FXRuntime > fxRuntimeTable = HashBasedTable.create();

    public static void putRuntime(int entityId, String key, FXRuntime runtime) {
        FXRuntime old = fxRuntimeTable.get(entityId, key);
        if (old != null) old.destroy(true);
        fxRuntimeTable.put(entityId, key, runtime);
    }

    public static Vec3 NORMAL_SCALE = new Vec3( 1D,1D,1D);


    public static final AnimationProperty.PlaybackSpeedModifier ONE50PERCENT = (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 1.5F;
    public static final AnimationProperty.PlaybackSpeedModifier ONE25PERCENT = (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 1.25F;
    public static final AnimationProperty.PlaybackSpeedModifier DOUBLE = (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 2F;
    public static final AnimationProperty.PlaybackSpeedModifier EIGHT5 = (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 0.85F;
    public static final AnimationProperty.PlaybackSpeedModifier HALF = (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 0.5F;

    /// Spawns joint tracked entity effect with entry into fxRuntimeTable
    public static void spawnJointEffect(String location, LivingEntity entity, Joint biped, boolean updateRotation, boolean allowMulti) {
        try {
            JointTrackedEntityEffect effect = new JointTrackedEntityEffect(
                    FXHelper.getFX(ResourceLocation.parse(location)),
                    entity.level(),
                    entity,
                    biped,
                    Vec3f.ZERO,
                    EntityEffectExecutor.AutoRotate.XROT,
                    updateRotation
            );
            effect.setRotation(0, 0, 0);
            effect.setOffset(0, 0, 0);
            effect.setScale(1, 1, 1);
            effect.setDelay(0);
            effect.setForcedDeath(false);
            effect.setAllowMulti(allowMulti);
            effect.start();
            FXRuntime runtime = effect.getRuntime();
            fxRuntimeTable.put(entity.getId(), location ,runtime);
        } catch (Exception e) {
            t0001.LOGGER.error("NO Fx present at {}", location);
        }
    }

    ///Joint Based Entity Effect
    public static void spawnJointEntityEffect(String location, LivingEntity entity, EntityEffectExecutor.AutoRotate autoRotate, boolean allow_multi, boolean forceDeath, Joint joint, Vec3f translation, boolean useJointROT) {
        try {
            EntityEffectExecutor effect = new EntityEffectExecutor(
                    FXHelper.getFX(ResourceLocation.parse(location)),
                    entity.level(),
                    entity,
                    autoRotate
            );

            Vector3f joint_offset = Objects.requireNonNull(ReusableEventsAndUtils.JointTrack.getjointpos(entity, joint, translation)).toVector3f();
            Quaternionf joint_rot = ReusableEventsAndUtils.JointTrack.getJointRotationInTime(entity,joint);
            effect.setRotation(0, 0, 0);
            if(useJointROT){
                effect.setRotation(joint_rot);
            }
            effect.setOffset(joint_offset);
            effect.setScale(1, 1, 1);
            effect.setDelay(0);
            effect.setForcedDeath(forceDeath);
            effect.setAllowMulti(allow_multi);
            effect.start();
            FXRuntime runtime = effect.getRuntime();
            putRuntime(entity.getId(), location, runtime);
        } catch (Exception e) {
            t0001.LOGGER.error("JointEntityEffect throws an error: {}", e.getMessage());
        }
    }



    /**make method forcefully throw an exception by making the videolocation have illegal characters or spaces to stop video
     *
     * usage examples: renderVideo(286, "testvideo", ".gif") ------
     *                 renderVideo(286, "impact_frames/one_inch/frame0impact", ".mp4"),
     * **/
    @ClientOnly
    public static AnimationEvent.@NotNull InTimeEvent<AnimationEvent.Event<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> renderVideo(int blenderFrame, String VideoLocation, String videoFormat) {
        return AnimationEvent.InTimeEvent.create(getAnimTimeFromFrame(blenderFrame), (e, s, p) -> {
            if (ModList.get().isLoaded(WaterMedia.ID) ) {
                try {
                    ResourceLocation location = ResourceLocation.fromNamespaceAndPath(t0001.MODID, VideoLocation + videoFormat);
                    System.out.println(location);
                    VideoRendererUtil.playVideo(location.toString(), e.getId(), 1.0f);
                } catch (Exception exception) {
                    VideoRendererUtil.stopVideo(e.getOriginal().getUUID());
                }
            }
        }, AnimationEvent.Side.LOCAL_CLIENT);
    }

    /// IGNITES LAST HIT ENEMIES AFTER TIME
    public static AnimationEvent.@NotNull InTimeEvent<AnimationEvent.Event<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> igniteLastHitenemies(float FireTime) {
        return AnimationEvent.InTimeEvent.create(FireTime, (e, s, p) -> {
                    if (e.isLastAttackSuccess() && !e.getCurrentlyActuallyHitEntities().isEmpty()) {
                        e.getCurrentlyActuallyHitEntities().forEach(
                                entity -> entity.igniteForSeconds(3)
                        );
                    }
                },
                AnimationEvent.Side.SERVER);
    }


    @ClientOnly
    public static AnimationEvent.@NotNull InTimeEvent<AnimationEvent.Event<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> frameAfterImage(int blenderFrame) {
        return AnimationEvent.InTimeEvent.create(getAnimTimeFromFrame(blenderFrame), (e, s, p) -> {
            LivingEntity entity = e.getOriginal();

            Particle particle = Minecraft.getInstance().particleEngine.createParticle(
                    EpicFightParticles.WHITE_AFTERIMAGE.get(),
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    Double.longBitsToDouble(entity.getId()),
                    0,
                    0
            );

            if(particle != null){
                particle.setLifetime(4);
            }

        }, AnimationEvent.Side.CLIENT);
    }

    /// normal speed = 1.0
    @ClientOnly
    public static AnimationEvent.@NotNull InTimeEvent<AnimationEvent.Event<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> renderVideoIfCamAnim(int blenderFrame, String VideoLocation, String videoFormat, float speed) {
        return AnimationEvent.InTimeEvent.create(getAnimTimeFromFrame(blenderFrame), (e, s, p) -> {
            if (ModList.get().isLoaded(WaterMedia.ID) && CameraAnimator.getInstance().isPlaying()) {
                try {
                    ResourceLocation location = ResourceLocation.fromNamespaceAndPath(t0001.MODID, VideoLocation + videoFormat);
                    System.out.println(location);
                    VideoRendererUtil.playVideo(location.toString(), e.getId(), speed);
                } catch (Exception exception) {
                    VideoRendererUtil.stopVideo(e.getOriginal().getUUID());
                }
            }
        }, AnimationEvent.Side.LOCAL_CLIENT);
    }

    public static AnimationEvent.@NotNull InTimeEvent<AnimationEvent.Event<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> playCamAnim(String AnimName, int blenderFrame) {
        if(blenderFrame == 0){
            blenderFrame++;
        }

        return AnimationEvent.InTimeEvent.create(getAnimTimeFromFrame(blenderFrame),
                (e, s, p) -> {

                    CameraAnimator.getInstance().playWithOption(AnimName, false, true);

                }
                , AnimationEvent.Side.LOCAL_CLIENT);
    }


    @ClientOnly
    public static void SpawnRootJointTrackFX(LivingEntityPatch<?> e, String fx, boolean setMulti) {
        spawnJointEffect(fx, e.getOriginal(), e.getArmature().rootJoint, false, setMulti);
    }


    private static float[] computeSmoothedOffsetAndRotation(
            float baseX, float baseY, float baseZ,
            float extraX, float extraY, float extraZ,
            float rotXOffset, float rotYOffset, float rotZOffset,
            float yawDegrees) {
        float yaw = yawDegrees * ((float) Math.PI / 180f);

        // base rotate with entity
        float rotatedX = (float) (baseX * Math.cos(yaw) - baseZ * Math.sin(yaw));
        float rotatedZ = (float) (baseX * Math.sin(yaw) + baseZ * Math.cos(yaw));


        return new float[]{
                rotatedX + extraX,
                baseY + extraY,
                rotatedZ + extraZ,
                rotXOffset, rotYOffset - yawDegrees, rotZOffset
        };
    }

    /// Just spawn a joint based effect without joint tracking every tick, Extra(X,Y,Z) are translation for joint
    public static AnimationEvent.@NotNull InTimeEvent<AnimationEvent.Event<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> spawnDirectionalJointBlockEffect(
            String fxLocation,
            float time,
            float extraX, float extraY, float extraZ, Joint joint
    ) {
        return AnimationEvent.InTimeEvent.create(time, (e, s, p) -> {

            try {
                Quaternionf jr = ReusableEventsAndUtils.JointTrack.getJointRotationInTime(e.getOriginal(), joint);

                BlockPos blockPos = e.getOriginal().getOnPos();
                FX fx = FXHelper.getFX(ResourceLocation.parse(fxLocation));

                BlockEffectExecutor blockEffect = new BlockEffectExecutor(fx, e.getLevel(), blockPos);
                blockEffect.setOffset(extraX, extraY, extraZ);
                blockEffect.setRotation(jr);
                blockEffect.setScale(1, 1, 1);
                blockEffect.setAllowMulti(true);
                blockEffect.setForcedDeath(false);
                blockEffect.setCheckState(false);
                blockEffect.start();

                FXRuntime runtime = blockEffect.getRuntime();
                putRuntime(e.getId(), fxLocation, runtime);

            } catch (Exception ex) {
                t0001.LOGGER.error(ex.getMessage());
            }

        }, AnimationEvent.Side.CLIENT);
    }

    @Deprecated(forRemoval = true, since = "JointTrackedEntityEffect can do it bettah")
    public static AnimationEvent.@NotNull InTimeEvent<AnimationEvent.Event<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> spawnDirectionalEntityEffect(
            String fxLocation,
            float time,
            float baseX, float baseY, float baseZ,
            float extraX, float extraY, float extraZ,
            float rotXOffset, float rotYOffset, float rotZOffset,
            EntityEffectExecutor.AutoRotate autoRotate
    ) {
        return AnimationEvent.InTimeEvent.create(time, (e, s, p) -> {
            LivingEntity entity = e.getOriginal();
            float[] result = computeSmoothedOffsetAndRotation(
                    baseX, baseY, baseZ,
                    extraX, extraY, extraZ,
                    rotXOffset, rotYOffset, rotZOffset,
                    entity.getYRot()
            );

            FX fx = FXHelper.getFX(ResourceLocation.parse(fxLocation));

            EntityEffectExecutor entityEffect = new EntityEffectExecutor(fx, e.getLevel(), entity, autoRotate);
            entityEffect.setOffset(result[0], result[1], result[2]);
            entityEffect.setRotation(result[3], result[4], result[5]);
            entityEffect.setScale(1, 1, 1);
            entityEffect.setAllowMulti(true);
            entityEffect.setForcedDeath(false);
            entityEffect.start();

        }, AnimationEvent.Side.CLIENT);
    }

}
