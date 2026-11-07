package sid.base.client.camera;

import com.google.gson.JsonObject;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
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

    private final Map<ResourceLocation, CameraAnimation> animations = new HashMap<>();

    @Nullable
    private CameraAnimation currentAnimation;
    private ResourceLocation currentAnimationName;
    private CamAnim currentCamAnim;
    private Entity originEntity;

    private float currentTime;
    private boolean playing;
    private boolean looping;
    private boolean isMirrored;
    private boolean worldSpace;
    private boolean hasEventRegistered;
    private boolean paused;

    private boolean lockMousePanning;
    private float lockedYaw;

    private CameraAnimator() {
        this.currentTime = 0.0f;
        this.playing = false;
        this.looping = false;
        this.worldSpace = false;
    }

    public CamAnim getCurrentCamAnim() {
        return currentCamAnim;
    }

    public record CamAnim(ResourceLocation id, ResourceLocation jsonLocation) {
        public CamAnim(ResourceLocation id) {
            this(id, ResourceLocation.fromNamespaceAndPath(
                    id.getNamespace(), "camera/" + id.getPath() + ".json"));
        }

        public String animationName() {
            return id.getPath();
        }
    }

    public void registerAnimation(CamAnim camAnim) {
        try {
            Minecraft mc = Minecraft.getInstance();
            JsonAssetLoader loader = new JsonAssetLoader(mc.getResourceManager(), camAnim.jsonLocation());
            CameraAnimation animation = loadAnimation(loader);
            ResourceLocation name = camAnim.id();
            animations.put(name, animation);
            System.out.println("[CameraAnimator] Registered animation: " + name + " (" + animation.getDuration() + "s)");
        } catch (Exception e) {
            System.err.println("[CameraAnimator] Failed to load animation '" + camAnim.animationName() + "': " + e.getMessage());
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

    public void play(CamAnim anim, boolean loop, boolean lockMouse, boolean useWorldSpace, @Nullable Vec3 WorldSpaceOrigin){
        play(anim, loop,lockMouse,useWorldSpace, WorldSpaceOrigin, Minecraft.getInstance().player, false);
    }

    //Remembered a photon bug where particle movements were going haywire cause of world-space local space conflict, figured
    //I need to implement a world space in this camera animator to fix big bug when it goes psycho after massive player tp in local space
    public void play(CamAnim anim, boolean loop, boolean lockMouse, boolean useWorldSpace, @Nullable Vec3 WorldSpaceOrigin, @Nullable Entity OriginEntity, boolean isMirrored) {

        if (this.playing && this.originEntity != null
                && OriginEntity != null && this.originEntity != OriginEntity) {
            log.warn("[CameraAnimator] play() for entity {} ignored - already playing '{}' for entity {}",
                    OriginEntity, this.currentAnimationName, this.originEntity);
            return;
        }


        CameraType cameraType = Minecraft.getInstance().options.getCameraType();
        if (cameraType.isFirstPerson() || cameraType.isMirrored() || !Config.camAniToggle) return;

        if (!hasEventRegistered) {
            EpicFightClientEventHooks.Camera.BUILD_TRANSFORM_POST.registerEvent(event -> {
                try {
                    if (event.getCameraApi().isLockingOnTarget() && this.isPlaying()) {
                        event.getCameraApi().toggleLockOn();
                    }
                } catch (Exception e) {
                    log.error("LockOnError! : ", e);
                }
            }, "cameraAnimator", -100);
            hasEventRegistered = true;
        }


        CameraAnimation animation = animations.get(anim.id);
        if (animation == null) return;

        Minecraft mc = Minecraft.getInstance();


        this.currentTime = 0.0f;
        this.isMirrored = false;
        this.lockedYaw = 0.0f;
        this.worldSpaceOrigin = null;
        this.lastAppliedYaw = 0.0f;
        this.lastAppliedPitch = 0.0f;
        this.lastAppliedRoll = 0.0f;

        this.originEntity = OriginEntity != null ? OriginEntity : mc.player;

        if (this.originEntity != null) {
            this.lockedYaw = this.originEntity.getYRot();
        }

        // Resolve world-space origin. Falls back to the origin entity's eye at partialTick
        // if the caller didn't pass one explicitly.
        if (useWorldSpace) {
            if (WorldSpaceOrigin != null) {
                this.worldSpaceOrigin = WorldSpaceOrigin;
            } else if (this.originEntity != null) {
                this.worldSpaceOrigin = this.originEntity.getEyePosition(0.0f);
            } else {
                log.warn("[CameraAnimator] useWorldSpace=true but no origin entity and no explicit origin — aborting.");
                return;
            }
        } else {
            this.worldSpaceOrigin = null;
        }

        this.currentAnimation = animation;
        this.currentAnimationName = anim.id();
        this.currentCamAnim = anim;
        this.playing = true;
        this.paused = false;
        this.looping = loop;
        this.lockMousePanning = lockMouse;
        this.isMirrored = isMirrored;

        this.worldSpace = useWorldSpace;
    }

    /// WorldSpace only
    public void play_mirrored(CamAnim camAnim, boolean loop, Entity originEntity) {
        Vec3 worldSpaceOrigin = originEntity.getEyePosition();
        play(camAnim, loop, true, true, worldSpaceOrigin, Minecraft.getInstance().player, true);
    }


    public void stop() {
        this.playing = false;
        this.paused = false;
        this.currentTime = 0.0f;
        this.currentAnimation = null;
        this.currentAnimationName = null;
        this.currentCamAnim = null;
        this.worldSpaceOrigin = null;
        this.originEntity = null;
        this.isMirrored = false;
        this.worldSpace = false;
        this.looping = false;
        this.lockMousePanning = false;
        this.lockedYaw = 0.0f;
        this.lastAppliedYaw = 0.0f;
        this.lastAppliedPitch = 0.0f;
        this.lastAppliedRoll = 0.0f;
    }


    public Iterable<ResourceLocation> getAnimationNames() { return animations.keySet(); }

    private boolean gameHasBeenPaused;

    public void tick() {
        if (!playing || currentAnimation == null) return;

        // pause handle when singleplayer
        Minecraft mc = Minecraft.getInstance();
        boolean gamePaused = mc.isSingleplayer()
                && mc.screen != null
                && mc.screen.isPauseScreen();

        if (gamePaused || paused) {
            gameHasBeenPaused = true;
            return;
        } else if (gameHasBeenPaused) {
            gameHasBeenPaused = false;
        }

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

        float baseYaw = this.lockMousePanning
                ? this.lockedYaw
                : (originEntity != null ? originEntity.getViewYRot(partialTick) : mc.player.getViewYRot(partialTick));

        // Inverse X and Z positions to account for Blender to MC conversion
        Vector3f animOffset = new Vector3f(-anim.location.x, anim.location.y, -anim.location.z);

        if (isMirrored) { //Reverse Positions for Mirrored mode
            animOffset.x = -animOffset.x;
            animOffset.z = -animOffset.z;
        }

        // Rotate offset by base yaw
        Quaternionf yawRot = new Quaternionf().rotateY((float) Math.toRadians(-baseYaw));
        animOffset = yawRot.transform(animOffset);

        // get Desired Local/World Space params
        Vector3f desiredPos = getDesiredPos(partialTick, mc, animOffset);

        Quaternionf rotation = anim.rotation;

        // Convert rotation to Euler angles
        Vector3f euler = rotation.getEulerAnglesYXZ(new Vector3f());
        float animYaw = (float) Math.toDegrees(euler.y);
        float animPitch = (float) Math.toDegrees(euler.x);
        float animRoll = (float) Math.toDegrees(euler.z);

        if (isMirrored) {
            animYaw += 180f;
        }

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
                if (mc.player == null) return new Vector3f(animOffset);
                basePos = mc.player.getEyePosition(0.01f);
            }
        } else {
            if (mc.player == null) return new Vector3f(animOffset);
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
    public void pause()  { this.paused = true;  }

    public void resume() { this.paused = false; }

    public boolean isPlaying() { return playing; }

    public boolean isPaused()  { return paused;  }


    @Nullable public ResourceLocation getCurrentAnimationName() { return currentAnimationName; }

    public boolean isMirrored(){ return isMirrored;}

    public float getCurrentTime() { return currentTime; }

    public boolean hasAnimation(ResourceLocation name) { return animations.containsKey(name); }

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