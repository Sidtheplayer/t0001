package sid.base.client.events;

import com.google.gson.JsonObject;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
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
    private boolean isMirrored;
    private boolean worldSpace;

    private boolean lockMousePanning;
    private float lockedYaw;

    private CameraAnimator() {
        this.currentTime = 0.0f;
        this.playing = false;
        this.looping = false;
        this.worldSpace = false;
    }

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
        if (cameraObject == null) throw new IllegalArgumentException("JSON must contain 'camera' object");

        JsonObject convertedJson = new JsonObject();
        convertedJson.add("time", cameraObject.get("time"));
        convertedJson.add("transform", cameraObject.get("transform"));
        convertedJson.addProperty("name", "camera");

        TransformSheet sheet = JsonAssetLoader.getTransformSheet(
                convertedJson, null, false, JsonAssetLoader.TransformFormat.ATTRIBUTES
        );
        return new CameraAnimation(sheet);
    }



    //Remembered a photon bug where particle movements were going haywire cause of world-space local space conflict, figured
    //I need to implement a world space in this camera animator to fix big bug when it goes psycho after massive player tp in local space
    public void play(String name, boolean loop, boolean lockMouse, boolean useWorldSpace, @Nullable Vec3 WorldSpaceOrigin) {
        CameraType cameraType = Minecraft.getInstance().options.getCameraType();
        if (cameraType.isFirstPerson() || cameraType.isMirrored() || !Config.camAniToggle) return;

        EpicFightClientEventHooks.Camera.BUILD_TRANSFORM_POST.registerEvent(event -> {
            try {
                if (event.getCameraApi().isLockingOnTarget()) {
                    event.getCameraApi().toggleLockOn();
                }
            } catch (Exception e) {
                log.error("LockOnError! : ", e);
            }
        });

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
        this.isMirrored = false;

        this.worldSpace = useWorldSpace;

        if (useWorldSpace && WorldSpaceOrigin == null && mc.player != null) {
            this.worldSpaceOrigin = mc.player.getEyePosition(1.0f);
        } else {
            this.worldSpaceOrigin = WorldSpaceOrigin;
        }

    }

    public void play_mirrored(String name, boolean loop, boolean lockMouse, boolean useWorldSpace, @Nullable Vec3 WorldSpaceOrigin) {
        play(name, loop, lockMouse, useWorldSpace, WorldSpaceOrigin);
        this.isMirrored = true;
    }


    public void stop() {
        this.playing = false;
        this.currentTime = 0.0f;
        this.currentAnimation = null;
        this.currentAnimationName = null;
    }

    public void pause() { this.playing = false; }

    public void resume() { if (this.currentAnimation != null) this.playing = true; }

    public Iterable<String> getAnimationNames() { return animations.keySet(); }

    public void tick() {
        if (!playing || currentAnimation == null) return;
        currentTime += 0.05f;
        if (currentTime >= currentAnimation.getDuration()) {
            if (looping) {
                currentTime = currentTime % currentAnimation.getDuration();
            } else {
                playing = false;
                currentTime = currentAnimation.getDuration();
            }
        }
    }

    public void applyToCamera(Camera camera, float partialTick) {
        if (!playing || currentAnimation == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        float animTime = currentTime + (partialTick / 20.0f);
        CameraTransform anim = getTransformAtTime(animTime);

        float baseYaw = this.lockMousePanning ? this.lockedYaw : mc.player.getViewYRot(partialTick);

        // Inverse X and Z positions to account for Blender to MC conversion
        Vector3f animOffset = new Vector3f(-anim.location.x, anim.location.y, -anim.location.z);

        if (isMirrored) {
            animOffset = new Vector3f(anim.location.x, -anim.location.y, -anim.location.z);
        }

        // Rotate offset by base yaw
        Quaternionf yawRot = new Quaternionf().rotateY((float) Math.toRadians(-baseYaw));
        animOffset = yawRot.transform(animOffset);

        // get Desired Local/World Space params
        Vector3f desiredPos = getDesiredPos(partialTick, mc, animOffset);

        // Convert rotation to Euler angles
        Vector3f euler = anim.rotation.getEulerAnglesYXZ(new Vector3f());
        float animYaw = (float) Math.toDegrees(euler.y);
        float animPitch = (float) Math.toDegrees(euler.x);
        float animRoll = (float) Math.toDegrees(euler.z);

        // NaN safety if something bad might happen
        if (Float.isNaN(animYaw) || Float.isNaN(animPitch) || Float.isNaN(animRoll)) {
            animYaw = this.lastAppliedYaw;
            animPitch = this.lastAppliedPitch;
            animRoll = this.lastAppliedRoll;
        } else {
            this.lastAppliedYaw = animYaw;
            this.lastAppliedPitch = animPitch;
            this.lastAppliedRoll = animRoll;
        }

        float finalYaw = baseYaw + animYaw;

        // Apply to camera
        applyCameraTransform(camera, desiredPos, finalYaw, animPitch, animRoll);
    }

    private @NotNull Vector3f getDesiredPos(float partialTick, Minecraft mc, Vector3f animOffset) {
        Vec3 basePos;
        if (worldSpace) {
            if (worldSpaceOrigin != null) {
                basePos = worldSpaceOrigin;
            } else {
                assert mc.player != null;
                basePos = mc.player.getEyePosition(0.01f);
            }
        } else {
            assert mc.player != null;
            basePos = mc.player.getEyePosition(partialTick);
        }

        return new Vector3f(
                (float) basePos.x + animOffset.x,
                (float) basePos.y + animOffset.y,
                (float) basePos.z + animOffset.z
        );
    }

    private float lastAppliedYaw = 0f, lastAppliedPitch = 0f, lastAppliedRoll = 0f;
    private Vec3 worldSpaceOrigin = null; // Important - Set a god reference world Space point before launch or else fallbacks

    private void applyCameraTransform(Camera camera, Vector3f position, float yaw, float pitch, float roll) {
        CameraAccessor accessor = (CameraAccessor) camera;
        accessor.invokeSetPosition(position.x, position.y, position.z);
        accessor.invokeSetRotation(yaw, pitch, roll);
    }

    private CameraTransform getTransformAtTime(float time) {
        if (currentAnimation == null) return new CameraTransform(new Vector3f(), new Quaternionf());
        JointTransform transform = currentAnimation.sheet.getInterpolatedTransform(time);
        Vec3f efTranslation = transform.translation();
        Quaternionf efRotation = transform.rotation();
        return new CameraTransform(
                new Vector3f(efTranslation.x, efTranslation.y, efTranslation.z),
                new Quaternionf(efRotation.x, efRotation.y, efRotation.z, efRotation.w)
        );
    }


    // Getters
    public boolean isPlaying() { return playing; }

    @Nullable public String getCurrentAnimationName() { return currentAnimationName; }

    public float getCurrentTime() { return currentTime; }

    public boolean hasAnimation(String name) { return animations.containsKey(name); }

    public boolean isMousePanningLocked() { return lockMousePanning; }

    public void setLockMousePanning(boolean lock) { this.lockMousePanning = lock; }

    private static class CameraAnimation {
        private final TransformSheet sheet;
        private final float duration;
        public CameraAnimation(TransformSheet sheet) {
            this.sheet = sheet;
            this.duration = sheet.maxFrameTime();
        }
        public float getDuration() { return duration; }
    }

    public record CameraTransform(Vector3f location, Quaternionf rotation) {}
}