package sid.base.utils;

import com.lowdragmc.photon.client.fx.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

import java.util.List;
import java.util.TimerTask;

import static sid.base.gameasset.animations.UltimateAnimations.fxRuntimeHashBiMap;

/// Client only use cases
public abstract class ReusableAnimEvents {

    /// made for converting photon fx time gotten from delay-testing fx in minecraft to anim time
    public static float getAnimTimeFromTickTime(int ticks) {
        return (float) ticks / 20;
    }

    /// assumes animation to be at 60 frames per second
    public static float getAnimTimeFromFrame(int frame) {
        return (float) frame / 60;
    }

    public static final AnimationProperty.PlaybackSpeedModifier ONE50PERCENT = (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 1.5F;
    public static final AnimationProperty.PlaybackSpeedModifier DOUBLE = (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 2F;
    public static final AnimationProperty.PlaybackSpeedModifier EIGHT5 = (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 0.85F;
    public static final AnimationProperty.PlaybackSpeedModifier HALF = (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 0.5F;

    public static final AnimationEvent.E0 SEND_BYPASSED_CHAT_MESSAGE = ((entitypatch, animation, params) -> {
        MinecraftServer server = entitypatch.getLevel().getServer();
        if (server != null) {
            for (Player player : entitypatch.getLevel().getNearbyPlayers(TargetingConditions.DEFAULT, entitypatch.getOriginal(), AABB.ofSize(
                    Vec3.fromRGB24(20), 10, 20, 10))) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(entitypatch.getOriginal().getScoreboardName() + ": " + params.first()));
            }
        }
    });

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
            fxRuntimeHashBiMap.put(e.getId(), runtime); //Create BiHashMap to map entityIds, and runtimes to destroy or manage outside the origin
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

    // for holding positional + rotational offset for a specific directions
    public record DirectionalOffset(Direction direction, float x, float y, float z,
                                    float rotX, float rotY, float rotZ) {

        public static DirectionalOffset of(Direction dir, float x, float y, float z,
                                           float rotX, float rotY, float rotZ) {
            return new DirectionalOffset(dir, x, y, z, rotX, rotY, rotZ);
        }
    }

    /// inverts y 90 degrees with 1 y height added offset
    public static  List<DirectionalOffset> INVERT_Y_ROT = List.of(
            ReusableAnimEvents.DirectionalOffset.of(Direction.NORTH,  0f, 1f,  0f,  0f, -90f,   0f),
            ReusableAnimEvents.DirectionalOffset.of(Direction.SOUTH, 0f, 1f,  0f,  0f, 90f, 0f),
            ReusableAnimEvents.DirectionalOffset.of(Direction.EAST,   0f,   1f,  0f, 0f, 180f,  0f),
            ReusableAnimEvents.DirectionalOffset.of(Direction.WEST,   0f,   1f, 0f, 0f, 0f, 0f));



}
