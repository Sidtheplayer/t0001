package sid.base.utils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.lowdragmc.photon.client.fx.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.watermedia.WaterMedia;
import sid.base.client.events.CameraAnimator;
import sid.base.client.photon.executor.JointTrackedEntityEffect;
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

import static sid.base.gameasset.ReusableEventsAndUtils.getAnimTimeFromFrame;


/// Client only use cases
@SuppressWarnings("unused")
public abstract class ReusableAnimEvents {

    public static SkillContainer getLocalSkillContainer(Skill skill) {
        LocalPlayerPatch localPlayerPatch = EpicFightCapabilities.getLocalPlayerPatch(Minecraft.getInstance().player);
        if (localPlayerPatch != null) {
            return localPlayerPatch.getSkill(skill);
        }
        return null;
    }

    public static boolean localPlayerHasSkill(Skill skill) {
        return ReusableAnimEvents.getLocalSkillContainer(skill) != null;
    }


    /// Table to map entityId and runtimes to destroy or manage outside the origin, only one runtime per fx at time can exist
    public static final Table<Integer, String, FXRuntime> fxRuntimeTable = HashBasedTable.create();
    public static final Table<Integer, String, IFXEffectExecutor> ifxExecutorTable = HashBasedTable.create();

    public static void putRuntime(int entityId, String key, FXRuntime runtime) {
        FXRuntime old = fxRuntimeTable.get(entityId, key);
        if (old != null) old.destroy(true);
        fxRuntimeTable.put(entityId, key, runtime);
    }

    public static void putFXExec(int entityId, String key, IFXEffectExecutor effectExecutor){
        IFXEffectExecutor old = ifxExecutorTable.get(entityId, key);
        if(old == null){
            ifxExecutorTable.put(entityId, key, effectExecutor);
        }

    }



    public static Vec3 NORMAL_SCALE = new Vec3(1D, 1D, 1D);

    public static final AnimationProperty.PlaybackSpeedModifier ONE50PERCENT = (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 1.5F;
    public static final AnimationProperty.PlaybackSpeedModifier ONE25PERCENT = (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 1.25F;
    public static final AnimationProperty.PlaybackSpeedModifier DOUBLE = (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 2F;
    public static final AnimationProperty.PlaybackSpeedModifier EIGHT5 = (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 0.85F;
    public static final AnimationProperty.PlaybackSpeedModifier HALF = (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 0.5F;


    public static void spawnJointEffect(String location, LivingEntity entity, Joint biped, boolean updateRotation, boolean allowMulti, Vec3f translation) {
        spawnJointEffect(location, entity, biped, Vec3f.ZERO, updateRotation, allowMulti, translation);
    }


    /** Spawns joint tracked entity effect with entry into fxRuntimeTable
     *
     * @apiNote will not allow more than one effect at a time
     * **/
    public static void spawnJointEffect(String location, LivingEntity entity, Joint biped, Vec3f rotation, boolean updateRotation, boolean allowMulti, Vec3f translation) {
        try {
            JointTrackedEntityEffect effect = new JointTrackedEntityEffect(
                    FXHelper.getFX(ResourceLocation.parse(location)),
                    entity.level(),
                    entity,
                    biped,
                    translation,
                    EntityEffectExecutor.AutoRotate.XROT,
                    updateRotation
            );
            effect.setRotation( rotation.x,  rotation.y, rotation.z);
            effect.setOffset(0, 0, 0);
            effect.setScale(1, 1, 1);
            effect.setDelay(0);
            effect.setForcedDeath(false);
            effect.setAllowMulti(allowMulti);
            effect.start();
            FXRuntime runtime = effect.getRuntime();
            fxRuntimeTable.put(entity.getId(), location, runtime);
            ifxExecutorTable.put(entity.getId(), location, effect);
        } catch (Exception e) {
            t0001.LOGGER.error("NO Fx present at {}", location);
        }
    }

    public static void spawnJointEffect(String location, LivingEntity entity, Joint joint, boolean updateRotation, boolean allowMulti) {
        spawnJointEffect(location, entity, joint, Vec3f.ZERO, updateRotation, allowMulti, Vec3f.ZERO);
    }

    /// Tick timed JointEffect
    public static AnimationEvent.@NotNull InTimeEvent<AnimationEvent.Event<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> spawnJointEffect_t(int tick, String location, Joint joint, boolean updateRotation, Vec3f translation) {
        return AnimationEvent.InTimeEvent.create(ReusableEventsAndUtils.getAnimTimeFromTickTime(tick), (e, s, p) ->
                {
                    LivingEntity entity = e.getOriginal();
                    spawnJointEffect(location, entity, joint, Vec3f.ZERO, updateRotation, true, translation);
                },
                AnimationEvent.Side.CLIENT
        );
    }

    public static AnimationEvent.@NotNull InTimeEvent<AnimationEvent.Event<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> spawnJointEffect_t(int tick, String location, Joint joint, boolean updateRotation, Vec3f translation, Vec3f rotation) {
        return AnimationEvent.InTimeEvent.create(ReusableEventsAndUtils.getAnimTimeFromTickTime(tick), (e, s, p) ->
                {
                    LivingEntity entity = e.getOriginal();
                    spawnJointEffect(location, entity, joint, rotation, updateRotation, true, translation);
                },
                AnimationEvent.Side.CLIENT
        );
    }

    /// Frame timed JointEffect
    public static AnimationEvent.@NotNull InTimeEvent<AnimationEvent.Event<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> spawnJointEffect_f(int blenderFrame, String location, Joint joint, boolean updateRotation,Vec3f rotation, Vec3f translation) {
        return AnimationEvent.InTimeEvent.create(ReusableEventsAndUtils.getAnimTimeFromFrame(blenderFrame), (e, s, p) ->
                {
                    LivingEntity entity = e.getOriginal();
                    spawnJointEffect(location, entity, joint, rotation, updateRotation, true, translation);
                },
                AnimationEvent.Side.CLIENT
        );
    }

    public static AnimationEvent.@NotNull InTimeEvent<AnimationEvent.Event<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> spawnJointEffect_f(int blenderFrame, String location, Joint joint, boolean updateRotation, Vec3f translation) {
        return AnimationEvent.InTimeEvent.create(ReusableEventsAndUtils.getAnimTimeFromFrame(blenderFrame), (e, s, p) ->
                {
                    LivingEntity entity = e.getOriginal();
                    spawnJointEffect(location, entity, joint, Vec3f.ZERO, updateRotation, true, translation);
                },
                AnimationEvent.Side.CLIENT
        );
    }

    /// Frame timed entityFx
    public static AnimationEvent.@NotNull InTimeEvent<AnimationEvent.Event<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> spawnEntityEffect_f(int blenderFrame, String location, EntityEffectExecutor.AutoRotate autoRotate, boolean allow_multi, boolean forceDeath, @Nullable Vec3f offset, @Nullable Quaternionf rotation) {
        return AnimationEvent.InTimeEvent.create(ReusableEventsAndUtils.getAnimTimeFromFrame(blenderFrame), (e, s, p) ->
                {
                    LivingEntity entity = e.getOriginal();
                    HandleEntityEffect(location, entity, autoRotate, allow_multi, forceDeath, offset, rotation);
                },
                AnimationEvent.Side.CLIENT
        );
    }

    /// tick timed entityFx
    public static AnimationEvent.@NotNull InTimeEvent<AnimationEvent.Event<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> spawnEntityEffect_t(int tick, String location, EntityEffectExecutor.AutoRotate autoRotate, boolean allow_multi, boolean forceDeath, @Nullable Vec3f offset, @Nullable Quaternionf rotation) {
        return AnimationEvent.InTimeEvent.create(ReusableEventsAndUtils.getAnimTimeFromTickTime(tick), (e, s, p) ->
                {
                    LivingEntity entity = e.getOriginal();
                    HandleEntityEffect(location, entity, autoRotate, allow_multi, forceDeath, offset, rotation);
                },
                AnimationEvent.Side.CLIENT
        );
    }

    /// Photon Entity Effect
    public static void HandleEntityEffect(String location, LivingEntity entity, EntityEffectExecutor.AutoRotate autoRotate, boolean allow_multi, boolean forceDeath, @Nullable Vec3f offset, @Nullable Quaternionf rotation) {
        try {
            EntityEffectExecutor effect = new EntityEffectExecutor(
                    FXHelper.getFX(ResourceLocation.parse(location)),
                    entity.level(),
                    entity,
                    autoRotate
            );

            effect.setRotation(0, 0, 0);
            effect.setOffset(0, 0, 0);
            if (rotation != null) {
                effect.setRotation(rotation);
            }
            if (offset != null) {
                effect.setOffset(offset.toMojangVector());
            }
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


    /**
     * make method forcefully throw an exception by making the videolocation have illegal characters or spaces to stop video
     * -
     * usage examples: renderVideo(286, "testvideo", ".gif") ------
     * renderVideo(286, "impact_frames/one_inch/frame0impact", ".mp4"),
     *
     **/
    @ClientOnly
    public static AnimationEvent.@NotNull InTimeEvent<AnimationEvent.Event<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> renderVideo(int blenderFrame, String VideoLocation, String videoFormat) {
        return AnimationEvent.InTimeEvent.create(getAnimTimeFromFrame(blenderFrame), (e, s, p) -> {
            if (ModList.get().isLoaded(WaterMedia.ID)) {
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

            if (particle != null) {
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
                    VideoRendererUtil.playVideo(location.toString(), e.getId(), speed + 0.1f); //somehow bugs out if exactly 1.0
                } catch (Exception exception) {
                    VideoRendererUtil.stopVideo(e.getOriginal().getUUID());
                }
            }
        }, AnimationEvent.Side.LOCAL_CLIENT);
    }

    public static AnimationEvent.@NotNull InTimeEvent<AnimationEvent.Event<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> playCamAnim(String AnimName, int blenderFrame) {
        if (blenderFrame == 0) {
            blenderFrame++;
        }

        return AnimationEvent.InTimeEvent.create(getAnimTimeFromFrame(blenderFrame),
                (e, s, p) -> {

                    CameraAnimator.getInstance().playWithOption(AnimName, false, true);

                }
                , AnimationEvent.Side.LOCAL_CLIENT);
    }

    public static AnimationEvent.@NotNull InTimeEvent<AnimationEvent.Event<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> playCamAnimMirrored(String AnimName, int blenderFrame) {
        if (blenderFrame == 0) {
            blenderFrame++;
        }

        return AnimationEvent.InTimeEvent.create(getAnimTimeFromFrame(blenderFrame),
                (e, s, p) -> {

                    CameraAnimator.getInstance().play_mirrored(AnimName, false, true);

                }
                , AnimationEvent.Side.LOCAL_CLIENT);
    }


    @ClientOnly
    public static void SpawnRootJointTrackFX(LivingEntityPatch<?> e, String fx, boolean setMulti) {
        spawnJointEffect(fx, e.getOriginal(), e.getArmature().rootJoint, false, setMulti);
    }


    public static AnimationEvent.@NotNull InTimeEvent<AnimationEvent.Event<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> spawnDirectionalJointBlockEffect(
            String fxLocation,

            float time,
            float extraX, float extraY, float extraZ, Joint joint, boolean tryJointAsBlockPos
    ) {
        return spawnDirectionalJointBlockEffect(fxLocation, Vec3f.ZERO.toDoubleVector(), time, extraX, extraY, extraZ, joint, tryJointAsBlockPos);
    }

    /// Just spawn a joint based effect without joint tracking every tick, Extra(X,Y,Z) are translation for joint
    public static AnimationEvent.@NotNull InTimeEvent<AnimationEvent.Event<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> spawnDirectionalJointBlockEffect(
            String fxLocation,
            Vec3 rotation,
            float time,
            float extraX, float extraY, float extraZ, Joint joint, boolean tryJointAsBlockPos
    ) {
        return AnimationEvent.InTimeEvent.create(time, (e, s, p) -> {

            try {
                Quaternionf jr = ReusableEventsAndUtils.JointTrack.getJointRotationInTime(e.getOriginal(), joint);

                jr.mul(new Quaternionf().rotateXYZ((float) rotation.x, (float) rotation.y, (float) rotation.z));

                BlockPos blockPos = e.getOriginal().getOnPos();

                Vector3f translate = new Vector3f(extraX, extraY, extraZ);

                Vec3 jointPos = ReusableEventsAndUtils.JointTrack.getjointpos(e.getOriginal(), joint, Vec3f.fromMojangVector(translate));

                assert jointPos != null;
                BlockPos fromJoint = new BlockPos(
                        Mth.floor(jointPos.x),
                        Mth.floor(jointPos.y),
                        Mth.floor(jointPos.z)
                );

                BlockPos toUse = tryJointAsBlockPos ? fromJoint : blockPos;
                Vector3f offset = tryJointAsBlockPos ? Vec3f.ZERO.toMojangVector() : translate;

                FX fx = FXHelper.getFX(ResourceLocation.parse(fxLocation));

                BlockEffectExecutor blockEffect = new BlockEffectExecutor(fx, e.getLevel(), toUse);
                blockEffect.setOffset(offset);
                blockEffect.setRotation(jr);
                blockEffect.setScale(1, 1, 1);
                blockEffect.setAllowMulti(true);
                blockEffect.setForcedDeath(false);
                blockEffect.setCheckState(false);
                blockEffect.start();

                FXRuntime runtime = blockEffect.getRuntime();
                putRuntime(e.getId(), fxLocation, runtime);

            } catch (Exception ex) {
                t0001.LOGGER.error("failure to spawn jointBlockEffect: {}", ex.getMessage());
            }

        }, AnimationEvent.Side.CLIENT);
    }


}
