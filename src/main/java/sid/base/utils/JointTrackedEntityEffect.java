package sid.base.utils;

import com.lowdragmc.photon.client.fx.EntityEffectExecutor;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.gameobject.IFXObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import sid.base.gameasset.ReusableEvents;
import sid.base.main.t0001;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@OnlyIn(Dist.CLIENT)
public class JointTrackedEntityEffect extends EntityEffectExecutor {

    private final Joint joint;
    private final Vec3f translation;
    private final boolean updateRotation;

    // Using caches to optimise — allocating new matrices/patches every frame = my pc tweaking

    // Position lerp
    private final Vector3f prevPos    = new Vector3f();
    private final Vector3f currentPos = new Vector3f();
    private final Vector3f smoothPos  = new Vector3f();
    private boolean posBootstrapped   = false;

    // Rotation slerp
    private final Quaternionf prevRot    = new Quaternionf();
    private final Quaternionf currentRot = new Quaternionf();
    private final Quaternionf smoothRot  = new Quaternionf();
    private boolean rotBootstrapped      = false;

    // Cache matrices and reuse every frame
    private final Matrix4f     jomlMatrix   = new Matrix4f();

    // Cached patch — looked up once on first rotation update, reused every frame
    private LivingEntityPatch<?> cachedPatch = null;

    // If rotation math fails on init, flag and skip to avoid catching exceptions every frame
    private boolean rotationFailed = false;

    /**
     * @param fx             photon fx — FXHelper.getFX(ResourceLocation.parse("photon:trail"))
     * @param level          the world level
     * @param entity         the entity to track
     * @param joint          the joint for rotation and position updates
     * @param translation    bone-space offset
     * @param autoRotate     AutoRotate.NONE works for most cases
     * @param updateRotation if true, syncs rotation from the joint matrix each frame
     */
    public JointTrackedEntityEffect(FX fx, Level level, Entity entity, Joint joint,
                                    Vec3f translation, AutoRotate autoRotate, boolean updateRotation) {
        super(fx, level, entity, autoRotate);
        this.joint          = joint;
        this.translation    = translation;
        this.updateRotation = updateRotation;
    }

    @Override
    public void updateFXObjectFrame(IFXObject fxObject, float partialTicks) {
        if (runtime == null || fxObject != runtime.root) return;

        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        Vec3 jointPos = ReusableEvents.JointTrack.getJointWithTranslation(
                localPlayer, entity, translation, joint);
        if (jointPos == null) return;

        //Position Update
        if (!posBootstrapped) {
            prevPos.set((float) jointPos.x, (float) jointPos.y, (float) jointPos.z);
            currentPos.set(prevPos);
            posBootstrapped = true;
        } else {
            prevPos.set(currentPos);
            currentPos.set((float) jointPos.x, (float) jointPos.y, (float) jointPos.z);
        }

        prevPos.lerp(currentPos, partialTicks, smoothPos);
        runtime.root.updatePos(smoothPos);

        //Rotation Update
        if (updateRotation && !rotationFailed && entity instanceof LivingEntity living) {
            updateJointRotation(living, partialTicks);
        }
    }

    private void updateJointRotation(LivingEntity living, float partialTicks) {
        // Resolve patch once, reuse every frame
        if (cachedPatch == null) {
            cachedPatch = EpicFightCapabilities.getEntityPatch(living, LivingEntityPatch.class);
            if (cachedPatch == null) {
                t0001.LOGGER.warn("[JointTrackedEntityEffect] No EpicFight patch on {}, disabling rotation.", living);
                rotationFailed = true;
                return;
            }
        }

        Pose pose;
        OpenMatrix4f transformMatrix;
        OpenMatrix4f rawModelMatrix;
        try {
            pose             = cachedPatch.getAnimator().getPose(partialTicks);
            transformMatrix  = cachedPatch.getArmature().getBoundTransformFor(pose, joint);
            rawModelMatrix   = cachedPatch.getModelMatrix(partialTicks);
        } catch (Exception e) {
            t0001.LOGGER.error("[JointTrackedEntityEffect] Rotation setup failed, disabling: {}", e.getMessage());
            rotationFailed = true;
            return;
        }

         //to fix weird rotation issues
        Vec3 pos = living.position();
        OpenMatrix4f worldModelTf = OpenMatrix4f.createTranslation(
                        (float) pos.x, (float) pos.y, (float) pos.z)
                .mulBack(OpenMatrix4f.createRotatorDeg(180.0F, Vec3f.Y_AXIS)
                        .mulBack(rawModelMatrix));

        // Apply joint on top of world model transform (mulFront = joint * worldModel)
        OpenMatrix4f result = transformMatrix.mulFront(worldModelTf);

        // Reuse jomlMatrix
        jomlMatrix.set(
                result.m00, result.m01, result.m02, result.m03,
                result.m10, result.m11, result.m12, result.m13,
                result.m20, result.m21, result.m22, result.m23,
                result.m30, result.m31, result.m32, result.m33
        );

        // Extract rotation — rotateLocalX(90) removed; was compensating for missing model matrix.
        // Add it back if axes are still off after testing.
        smoothRot.setFromUnnormalized(jomlMatrix);

        // Bootstrap or advance rotation slerp
        if (!rotBootstrapped) {
            prevRot.set(smoothRot);
            currentRot.set(smoothRot);
            rotBootstrapped = true;
        } else {
            prevRot.set(currentRot);
            currentRot.set(smoothRot);
        }

        prevRot.slerp(currentRot, partialTicks, smoothRot);

        if (runtime != null) {
            runtime.root.updateRotation(smoothRot);
        }
    }
}