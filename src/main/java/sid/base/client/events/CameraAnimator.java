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
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.utils.math.Vec3f;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;


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

    private boolean lockMousePanning;
    private float lockedYaw;

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
            play(name, false, false);

    }

    public void play(String name, boolean loop, boolean lockMouse) {
        CameraAnimation animation = animations.get(name);
        if (animation == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            this.lockedYaw = mc.player.getYRot();
        }

        this.currentAnimation = animation;
        this.currentAnimationName = name;
        this.currentTime = 0.0f;
        this.playing = true;
        this.looping = loop;
        this.lockMousePanning = lockMouse;
    }

    public void playWithOption(String name, boolean loop ,boolean lockMousePan) {
        CameraType cameraType = Minecraft.getInstance().options.getCameraType();
        if (cameraType.isFirstPerson() || cameraType.isMirrored() || !Config.camAniToggle) {
            return;
        }

        play(name, loop, lockMousePan);

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


        Minecraft mc = Minecraft.getInstance();
        if (mc.cameraEntity != null) {
            new CameraTransform(
                    new Vector3f((float) mc.cameraEntity.getX(), (float) mc.cameraEntity.getEyeY(), (float) mc.cameraEntity.getZ()),
                    new Quaternionf().rotationYXZ(
                            (float) Math.toRadians(-mc.cameraEntity.getYRot()),
                            (float) Math.toRadians(mc.cameraEntity.getXRot()),
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

    //for playanim command
    public Iterable<String> getAnimationNames() {
        return animations.keySet();
    }

    public void tick() {
        if (!playing || currentAnimation == null) {
            return;
        }

        currentTime += 0.05f;


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
        if (!playing || currentAnimation == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        float renderTime = currentTime + (partialTick / 20.0f);

        CameraTransform anim = getTransformAtTime(renderTime);

        float baseYaw = this.lockMousePanning ? this.lockedYaw : mc.player.getViewYRot(partialTick);

        //Inverse X and Y positions to account for blender to mc conversion
        Vector3f animOffset = new Vector3f(-anim.location.x, anim.location.y, -anim.location.z);

        Quaternionf yawRot = new Quaternionf().rotateY((float) Math.toRadians(-baseYaw));
        animOffset = yawRot.transform(animOffset);

        Vector3f animPos = new Vector3f(
                (float) mc.player.getX(),
                (float) mc.player.getY() + mc.player.getEyeHeight(), //add Eye Height to correct y height properly
                (float) mc.player.getZ()
        ).add(animOffset);


        Vector3f euler = anim.rotation.getEulerAnglesYXZ(new Vector3f());

        float animYaw = (float) Math.toDegrees(euler.y);
        float animPitch = (float) Math.toDegrees(euler.x);
        float animRoll = (float) Math.toDegrees(euler.z);

        float finalYaw = baseYaw + animYaw;

        applyCameraTransform(camera, animPos, finalYaw, animPitch, animRoll);
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

    public boolean isLockMousePanning() {
        return lockMousePanning;
    }

    public void setLockMousePanning(boolean lockMousePanning) {
        this.lockMousePanning = lockMousePanning;
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

    public record CameraTransform(Vector3f location, Quaternionf rotation) {}

}