package sid.base.utils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.lowdragmc.photon.client.fx.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.watermedia.WaterMedia;
import sid.base.client.events.CameraAnimator;
import sid.base.main.t0001;
import sid.base.mixin.CameraAccessor;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.asset.JsonAssetLoader;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.TimerTask;


/// Client only use cases
public abstract class ReusableAnimEvents {

    ///Table to map entityId and runtimes to destroy or manage outside the origin
    public static final Table<Integer, String, FXRuntime > fxRuntimeTable = HashBasedTable.create();


    /// made for converting photon fx time gotten from delay-testing fx in minecraft to anim time
    public static float getAnimTimeFromTickTime(int ticks) {
        return (float) ticks / 20;
    }

    /// assumes animation to be at 60 frames per second
    public static float getAnimTimeFromFrame(int frame) {
        return (float) frame / 60;
    }

    public static final AnimationProperty.PlaybackSpeedModifier ONE50PERCENT = (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 1.5F;
    public static final AnimationProperty.PlaybackSpeedModifier ONE25PERCENT = (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 1.25F;
    public static final AnimationProperty.PlaybackSpeedModifier DOUBLE = (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 2F;
    public static final AnimationProperty.PlaybackSpeedModifier EIGHT5 = (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 0.85F;
    public static final AnimationProperty.PlaybackSpeedModifier HALF = (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 0.5F;

    /**make method forcefully throw an exception by making the videolocation have illegal characters or spaces to stop video
     *
     * usage examples: renderVideo(286, "testvideo", ".gif") ------
     *                 renderVideo(286, "impact_frames/one_inch/frame0impact", ".mp4"),
     * **/
    @ClientOnly
    @OnlyIn(Dist.CLIENT)
    public static AnimationEvent.@NotNull InTimeEvent<AnimationEvent.Event<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> renderVideo(int blenderFrame, String VideoLocation, String videoFormat) {
        return AnimationEvent.InTimeEvent.create(ReusableAnimEvents.getAnimTimeFromFrame(blenderFrame), (e, s, p) -> {
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

    /// normal speed = 1.0
    @ClientOnly
    @OnlyIn(Dist.CLIENT)
    public static AnimationEvent.@NotNull InTimeEvent<AnimationEvent.Event<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> renderVideo(int blenderFrame, String VideoLocation, String videoFormat, float speed) {
        return AnimationEvent.InTimeEvent.create(ReusableAnimEvents.getAnimTimeFromFrame(blenderFrame), (e, s, p) -> {
            if (ModList.get().isLoaded(WaterMedia.ID)) {
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
        return AnimationEvent.InTimeEvent.create(getAnimTimeFromFrame(blenderFrame),
                (e, s, p) -> {

                    CameraAnimator.getInstance().playWithOption(AnimName, false, true);

                }
                , AnimationEvent.Side.LOCAL_CLIENT);
    }


    @ClientOnly
    @OnlyIn(Dist.CLIENT)
    public static void SpawnRootJointTrackFX(LivingEntityPatch<?> e, @SuppressWarnings("SameParameterValue") String FxResourceLocationString, @SuppressWarnings("SameParameterValue") boolean setmulti) {
        FX menacing = FXHelper.getFX(ResourceLocation.parse(FxResourceLocationString));
        Entity eo = e.getOriginal();
        Level l = eo.level().isClientSide ? eo.level() : null;
        if (l != null) {
            Armature ea = e.getArmature();
            JointTrackedEntityEffect jtef = new JointTrackedEntityEffect(menacing, l, eo, ea.rootJoint, Vec3f.ZERO, EntityEffectExecutor.AutoRotate.NONE, false);
            jtef.setOffset(0, 0, 0);
            jtef.setRotation(0, 0, 0);
            jtef.setScale(1, 1, 1);
            jtef.setAllowMulti(setmulti);
            jtef.setForcedDeath(true);
            jtef.setDelay(0);
            jtef.start();
            FXRuntime runtime = jtef.getRuntime();
            fxRuntimeTable.put(e.getId(), FxResourceLocationString ,runtime); //Use Table to map entityIds, and runtimes to destroy or manage outside the origin
        }

    }


    public static final AnimationEvent.E0 CAM_ANIM = ((entitypatch, animation, params) -> {

        //TODO:LEARN ABOUT THIS SHIT SO I CAN MAKE
        /*
        JsonArray arrayF = new JsonArray();
        //wtf am i doing
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        try {
            BufferedReader reader =    Objects.requireNonNull(manager.getResource(params.first()).orElse(null)).openAsReader();
            JsonReader jsonReader = new JsonReader(reader);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } */


        assert Minecraft.getInstance().getCameraEntity() != null;
        Vec3 camVec = new Vec3(0.084567, 0.092327, -0.772684);
        Vec3f transVec = Vec3f.fromMojangVector(OpenMatrix4f.transform(JsonAssetLoader.BLENDER_TO_MINECRAFT_COORD, camVec).toVector3f());

        EpicFightClientEventHooks.Camera.BUILD_TRANSFORM_PRE.registerContextAwareEvent(
                (event, context) -> {
                    //SetRot

                    event.getCamera().rotation().set(
                            0.072303f, 0.002212f, 0.996915f, -0.030475f
                    );

                    float TS = Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();

                    long schedT = (long) (50 + TS);
                    //MOVE
                    ((CameraAccessor) event.getCamera()).invokeSetPosition(transVec.x, transVec.y, transVec.z);
                    //RESEST
                    Minecraft.getInstance().tell(
                            new TimerTask() {
                                @Override
                                public void run() {
                                    if (Minecraft.getInstance().getTimer().getGameTimeDeltaTicks() == schedT) {
                                        event.getCamera().reset();

                                    }
                                }
                            }

                    );
                }
        );


    });


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

    public static AnimationEvent.@NotNull InTimeEvent<AnimationEvent.Event<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> spawnDirectionalBlockEffect(
            String fxLocation,
            float time,
            float baseX, float baseY, float baseZ,
            float extraX, float extraY, float extraZ,
            float rotXOffset, float rotYOffset, float rotZOffset
    ) {
        return AnimationEvent.InTimeEvent.create(time, (e, s, p) -> {
            float[] result = computeSmoothedOffsetAndRotation(
                    baseX, baseY, baseZ,
                    extraX, extraY, extraZ,
                    rotXOffset, rotYOffset, rotZOffset,
                    e.getOriginal().getYRot()
            );

            BlockPos blockPos = e.getOriginal().getOnPos();
            FX fx = FXHelper.getFX(ResourceLocation.parse(fxLocation));

            BlockEffectExecutor blockEffect = new BlockEffectExecutor(fx, e.getLevel(), blockPos);
            blockEffect.setOffset(result[0], result[1], result[2]);
            blockEffect.setRotation(result[3], result[4], result[5]);
            blockEffect.setScale(1, 1, 1);
            blockEffect.setAllowMulti(true);
            blockEffect.setForcedDeath(false);
            blockEffect.setCheckState(false);
            blockEffect.start();

        }, AnimationEvent.Side.CLIENT);
    }

    @Deprecated(forRemoval = true,since = "JointTrackedEntityEffect can do it bettah")
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
