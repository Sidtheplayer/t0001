package sid.base.client.events;

import com.google.gson.JsonObject;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import sid.base.main.Config;
import sid.base.mixin.CameraAccessor;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.asset.JsonAssetLoader;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.utils.math.Vec3f;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

//TODO: FIX Camera Movements via Mouse Movements not affect CameraAnimator
public class CameraAnimator {

    private static final Logger log = LogManager.getLogger(CameraAnimator.class);
    private static CameraAnimator INSTANCE;

    public static CameraAnimator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CameraAnimator();
        }
        return INSTANCE;
    }

    private final Map<String, CameraAnimation> animations = new HashMap<>();

    @Nullable
    private CameraAnimation currentAnimation;
    private String currentAnimationName;

    private float currentTime;
    private boolean playing;
    private boolean looping;

    private boolean transitioning;
    private float transitionTime;
    private float transitionDuration;
    private CameraTransform transitionStart;

    private  CoordinateMode coordinateMode = CoordinateMode.MINECRAFT;

    public void setCoordinateMode(CoordinateMode coordinateMode) {
        this.coordinateMode = coordinateMode;
    }

    private boolean followPlayer = false;
    private boolean rotateWithPlayer = false;
    private boolean useCamOffsets;

    private Vec3f CamOffsets;

    private CameraAnimator() {
        this.currentTime = 0.0f;
        this.playing = false;
        this.looping = false;
    }

    /**
     * Register a camera animation from resource location
     */
    public void registerAnimation(String name, ResourceLocation resourceLocation) {
        try {
            Minecraft mc = Minecraft.getInstance();
            JsonAssetLoader loader = new JsonAssetLoader(mc.getResourceManager(), resourceLocation);
            CameraAnimation animation = loadAnimation(loader);
            animations.put(name, animation);
            System.out.println("[CameraAnimator] Registered animation: " + name + " (" + animation.getDuration() + "s)");
        } catch (Exception e) {
            System.err.println("[CameraAnimator] Failed to load animation '" + name + "': " + e.getMessage());
           ;
        }
    }

    public void registerAnimation(String name, JsonAssetLoader loader) {
        try {
            CameraAnimation animation = loadAnimation(loader);
            animations.put(name, animation);
            System.out.println("[CameraAnimator] Registered animation: " + name + " (" + animation.getDuration() + "s)");
        } catch (Exception e) {
            System.err.println("[CameraAnimator] Failed to load animation '" + name + "': " + e.getMessage());

        }
    }

    private CameraAnimation loadAnimation(JsonAssetLoader loader) {
        JsonObject rootJson = loader.getRootJson();
        JsonObject cameraObject = rootJson.getAsJsonObject("camera");

        if (cameraObject == null) {
            throw new IllegalArgumentException("JSON must contain 'camera' object");
        }

        JsonObject convertedJson = new JsonObject();
        convertedJson.add("time", cameraObject.get("time"));
        convertedJson.add("transform", cameraObject.get("transform"));
        convertedJson.addProperty("name", "camera");

        TransformSheet sheet = JsonAssetLoader.getTransformSheet(
                convertedJson,
                null,
                false,
                JsonAssetLoader.TransformFormat.ATTRIBUTES
        );

        return new CameraAnimation(sheet);
    }

    public void play(String name) {
        CameraType cameraType = Minecraft.getInstance().options.getCameraType();
        if(cameraType.isFirstPerson() || cameraType.isMirrored() || !Config.camAniToggle){
            return;
        }
        EpicFightClientEventHooks.Camera.BUILD_TRANSFORM_POST.registerEvent(event ->
        {
            try {
                if(event.getCameraApi().isLockingOnTarget() && this.isPlaying()){
                    event.getCameraApi().toggleLockOn();
                }
            } catch (Exception e) {
                log.error("LockOnError! : ", e);
            }
        });
            useCamOffsets = false;
            play(name, false);

    }

    public void play(String name, boolean loop) {
        CameraAnimation animation = animations.get(name);
        if (animation == null) {
            System.err.println("[CameraAnimator] Animation not found: " + name);
            return;
        }

        this.currentAnimation = animation;
        this.currentAnimationName = name;
        this.currentTime = 0.0f;
        this.playing = true;
        this.looping = loop;
        this.transitioning = false;

        System.out.println("[CameraAnimator] Playing animation: " + name);
    }

    public void playWithTransitionAndOffsets(String name, float transitionSeconds, @Nullable Vec3f offsets) {
        play(name, false);
        this.transitioning = true;
        this.transitionTime = 0.0f;
        this.transitionDuration = transitionSeconds;
        this.CamOffsets = offsets;

        if(offsets != null){
            useCamOffsets = true;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.cameraEntity != null) {
            this.transitionStart = new CameraTransform(
                    new Vector3f((float)mc.cameraEntity.getX(), (float)mc.cameraEntity.getEyeY(), (float)mc.cameraEntity.getZ()),
                    new Quaternionf().rotationYXZ(
                            (float)Math.toRadians(-mc.cameraEntity.getYRot()),
                            (float)Math.toRadians(mc.cameraEntity.getXRot()),
                            0
                    )
            );
        }
    }

    public void stop() {
        this.playing = false;
        this.currentTime = 0.0f;
        this.currentAnimation = null;
        this.currentAnimationName = null;
        this.transitioning = false;
        System.out.println("[CameraAnimator] Stopped animation");
    }

    public void pause() {
        this.playing = false;
    }

    public void resume() {
        if (this.currentAnimation != null) {
            this.playing = true;
        }
    }

    public void setFollowPlayer(boolean follow) {
        this.followPlayer = follow;
    }

    public void setRotateWithPlayer(boolean rotate) {
        this.rotateWithPlayer = rotate;
    }

    public void tick() {
        if (!playing || currentAnimation == null) {
            return;
        }

        currentTime += 0.05f;

        if (transitioning) {
            transitionTime += 0.05f;
            if (transitionTime >= transitionDuration) {
                transitioning = false;
            }
        }

        if (currentTime >= currentAnimation.getDuration()) {
            if (looping) {
                currentTime = currentTime % currentAnimation.getDuration();
            } else {
                playing = false;
                currentTime = currentAnimation.getDuration();
                System.out.println("[CameraAnimator] Animation finished");
            }
        }
    }

    public void applyToCamera(Camera camera, float partialTick) {
        if (!playing && currentTime == 0.0f && !transitioning) {
            return;
        }

        if (currentAnimation == null) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        float renderTime = currentTime + (playing ? partialTick / 20.0f : 0);

        CameraTransform animTransform = getTransformAtTime(renderTime);

        CameraTransform finalTransform = animTransform;
        if (transitioning && transitionStart != null) {
            float alpha = Mth.clamp(transitionTime / transitionDuration, 0.0f, 1.0f);
            alpha = alpha * alpha * (3.0f - 2.0f * alpha);
            finalTransform = CameraTransform.lerp(transitionStart, animTransform, alpha);
        }

        finalTransform = convertCoordinates(finalTransform);

        if (followPlayer && mc.player != null) {
            Vector3f playerPos = new Vector3f(
                    (float)mc.player.getX(),
                    (float)mc.player.getEyeY(),
                    (float)mc.player.getZ()
            );

            Vector3f offset = finalTransform.location;

            if (rotateWithPlayer) {
                Quaternionf playerRotation = new Quaternionf().rotationY(
                        (float)Math.toRadians(-mc.player.getYRot())
                );
                offset = playerRotation.transform(new Vector3f(offset), new Vector3f());
            }

            finalTransform = new CameraTransform(
                    new Vector3f(
                            playerPos.x + offset.x,
                            playerPos.y + offset.y,
                            playerPos.z + offset.z
                    ),
                    finalTransform.rotation
            );

            if(useCamOffsets){
                finalTransform = new CameraTransform(
                        new Vector3f(
                                playerPos.x + offset.x + CamOffsets.x,
                                playerPos.y + offset.y + CamOffsets.y,
                                playerPos.z + offset.z + CamOffsets.z
                        ),
                        finalTransform.rotation
                );
            }
        }

        Vector3f euler = finalTransform.rotation.getEulerAnglesYXZ(new Vector3f());
        float yaw = (float) Math.toDegrees(euler.y);
        float pitch = (float) Math.toDegrees(euler.x);
        float roll = (float) Math.toDegrees(euler.z);

        applyCameraTransform(camera, finalTransform.location, yaw, pitch, roll);
    }


    private void applyCameraTransform(Camera camera, Vector3f position, float yaw, float pitch, float roll) {
        CameraAccessor accessor = (CameraAccessor) camera;

        // Set position
        accessor.invokeSetPosition(position.x, position.y, position.z);

        //set rot
        accessor.invokeSetRotation(yaw, pitch, roll);
    }


    private CameraTransform getTransformAtTime(float time) {
        if (currentAnimation == null) {
            return new CameraTransform(new Vector3f(), new Quaternionf());
        }


       JointTransform transform =
                currentAnimation.sheet.getInterpolatedTransform(time);

        // Get translation and rotation directly from JointTransforms
        Vec3f efTranslation = transform.translation();
        Quaternionf efRotation = transform.rotation();

        return new CameraTransform(
                new Vector3f(efTranslation.x, efTranslation.y, efTranslation.z),
                new Quaternionf(efRotation.x, efRotation.y, efRotation.z, efRotation.w)
        );
    }

    private CameraTransform convertCoordinates(CameraTransform transform) {
        switch (coordinateMode) {
            case BLENDER -> {
                return new CameraTransform(
                        new Vector3f(transform.location.x, transform.location.z, -transform.location.y),
                        transform.rotation
                );
            }
            case MINECRAFT -> {
                return transform;
            }
        }
        return transform;
    }

    // Getters
    public boolean isPlaying() {
        return playing;
    }

    @Nullable
    public String getCurrentAnimationName() {
        return currentAnimationName;
    }

    public float getCurrentTime() {
        return currentTime;
    }

    public float getAnimationDuration(String name) {
        CameraAnimation animation = animations.get(name);
        return animation != null ? animation.getDuration() : 0.0f;
    }

    public boolean hasAnimation(String name) {
        return animations.containsKey(name);
    }

    public int getAnimationCount() {
        return animations.size();
    }

    private static class CameraAnimation {
        private final TransformSheet sheet;
        private final float duration;

        public CameraAnimation(TransformSheet sheet) {
            this.sheet = sheet;
            this.duration = sheet.maxFrameTime();
        }

        public float getDuration() {
            return duration;
        }
    }

    public static class CameraTransform {
        public final Vector3f location;
        public final Quaternionf rotation;

        public CameraTransform(Vector3f location, Quaternionf rotation) {
            this.location = location;
            this.rotation = rotation;
        }

        public static CameraTransform lerp(CameraTransform a, CameraTransform b, float alpha) {
            Vector3f loc = new Vector3f();
            a.location.lerp(b.location, alpha, loc);

            Quaternionf rot = new Quaternionf();
            a.rotation.slerp(b.rotation, alpha, rot);

            return new CameraTransform(loc, rot);
        }
    }

    public enum CoordinateMode {
        BLENDER,
        MINECRAFT
    }


}