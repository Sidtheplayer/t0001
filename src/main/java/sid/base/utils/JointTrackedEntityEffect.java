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
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@OnlyIn(Dist.CLIENT)
public class JointTrackedEntityEffect extends EntityEffectExecutor {

    private final Joint joint;
    private final Vec3f translation;
    private final boolean updateRotation;

    // Using Caches to optimise after allocating new matrices/patches every frame is killing my pc

    // for Position lerp
    private final Vector3f prevPos   = new Vector3f();
    private final Vector3f currentPos = new Vector3f();
    private final Vector3f smoothPos  = new Vector3f();
    private boolean posBootstrapped   = false;

    // for Rotation slerp
    private final Quaternionf prevRot   = new Quaternionf();
    private final Quaternionf currentRot = new Quaternionf();
    private final Quaternionf smoothRot  = new Quaternionf();
    private boolean rotBootstrapped      = false;

    // Cached Matrices
    private final OpenMatrix4f finalMatrix  = new OpenMatrix4f();
    private final OpenMatrix4f bodyRotation = new OpenMatrix4f();
    private final Matrix4f     jomlMatrix   = new Matrix4f();
    private final Vec3f        yAxis        = new Vec3f(0.0F, 1.0F, 0.0F);

    // Cache Patch(don't know how much of optimisation this will bring)
    private LivingEntityPatch<?> cachedPatch = null;

    //If rotation math fails on init, skip to avoid catching an exception every subsequent frame
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


        if (updateRotation && !rotationFailed && entity instanceof LivingEntity living) {
            updateJointRotation(living, partialTicks);
        }
    }

    private void updateJointRotation(LivingEntity living, float partialTicks) {
        // Resolve patch once and cache it, reuse every frame
        if (cachedPatch == null) {
            cachedPatch = EpicFightCapabilities.getEntityPatch(living, LivingEntityPatch.class);
            if (cachedPatch == null) {
                t0001.LOGGER.warn("[JointTrackedEntityEffect] No EpicFight patch on {}, disabling rotation.", living);
                rotationFailed = true;
                return;
            }
        }

        // Matrix math
        OpenMatrix4f transformMatrix;
        try {
            transformMatrix = cachedPatch.getArmature()
                    .getBoundTransformFor(cachedPatch.getAnimator().getPose(partialTicks), joint);
        } catch (Exception e) {
            t0001.LOGGER.error("[JointTrackedEntityEffect] Rotation setup failed, disabling: {}", e.getMessage());
            rotationFailed = true;
            return;
        }


        bodyRotation.setIdentity();
        bodyRotation.rotate(
                -((float) Math.toRadians(living.yBodyRot + 180.0F)),
                yAxis
        );
        finalMatrix.setIdentity();
        OpenMatrix4f.mul(bodyRotation, transformMatrix, finalMatrix);


        jomlMatrix.set(
                finalMatrix.m00, finalMatrix.m01, finalMatrix.m02, finalMatrix.m03,
                finalMatrix.m10, finalMatrix.m11, finalMatrix.m12, finalMatrix.m13,
                finalMatrix.m20, finalMatrix.m21, finalMatrix.m22, finalMatrix.m23,
                finalMatrix.m30, finalMatrix.m31, finalMatrix.m32, finalMatrix.m33
        );


        smoothRot.setFromUnnormalized(jomlMatrix).rotateLocalX((float) Math.toRadians(90));

        // Bootstrap or advance rotation lerp
        if (!rotBootstrapped) {
            prevRot.set(smoothRot);
            currentRot.set(smoothRot);
            rotBootstrapped = true;
        } else {
            prevRot.set(currentRot);
            currentRot.set(smoothRot);
        }

        // Slerp into smoothRot (reused as output)
        prevRot.slerp(currentRot, partialTicks, smoothRot);

        if (runtime != null) {
            runtime.root.updateRotation(smoothRot);
        }
    }
}