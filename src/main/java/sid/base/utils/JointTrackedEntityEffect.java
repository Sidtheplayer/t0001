package sid.base.utils;

import com.lowdragmc.photon.client.fx.EntityEffectExecutor;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.gameobject.IFXObject;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
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

    // Reused every tick — avoid allocating a new Matrix every frame
    private final Matrix4f jomlMatrix = new Matrix4f();

    // Resolve and Cache patch and reused every frame
    private LivingEntityPatch<?> cachedPatch  = null;

    private boolean rotationFailed = false;

    private long lastUpdateTick = -1L;

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
        if (Minecraft.getInstance().player == null) return;
        if (!(entity instanceof LivingEntity living)) return;


        // Resolve patch once
        if (cachedPatch == null) {
            cachedPatch = EpicFightCapabilities.getEntityPatch(living, LivingEntityPatch.class);
            if (cachedPatch == null) {
                t0001.LOGGER.warn("[JointTrackedEntityEffect] No EpicFight patch on {}, disabling.", living);
                rotationFailed = true;
                return;
            }
        }

        long currentTick = entity.level().getGameTime();
        boolean isNewTick = currentTick != lastUpdateTick;

        if (isNewTick || !posBootstrapped) {
            sampleTickBoundaries();
            lastUpdateTick = currentTick;
        }

        if (!posBootstrapped) return;

        // Lerp position across the tick window using partialTicks
        prevPos.lerp(currentPos, partialTicks, smoothPos);
        runtime.root.updatePos(smoothPos);

        // Slerp rotation across the tick window using partialTicks
        if (updateRotation && !rotationFailed) {
            prevRot.slerp(currentRot, partialTicks, smoothRot);
            runtime.root.updateRotation(new Quaternionf(rotation).mul(smoothRot));
        } else {
            //call super to ensure autorotate works
            super.updateFXObjectFrame(fxObject, partialTicks);
        }
    }

    /**
     * Called once per game tick.
     * Inspired by epicfight AnimationTrailParticle's pattern
     * Sampling at 0.0F (prev tick) and 1.0F (current tick) to get
     * tick-boundary window to lerp across — in hopes to fix jittering position update
     */
    private void sampleTickBoundaries() {
        try {
            // Entity world positions at each tick boundary
            Vec3 posOld = entity.getPosition(0.0F);
            Vec3 posCur = entity.getPosition(1.0F);

            // Poses at each tick boundary
            Pose prevPose = cachedPatch.getAnimator().getPose(0.0F);
            Pose curPose  = cachedPatch.getAnimator().getPose(1.0F);

            // Full world-space transforms
            OpenMatrix4f prevWorldModelTf = OpenMatrix4f
                    .createTranslation((float) posOld.x, (float) posOld.y, (float) posOld.z)
                    .rotateDeg(180.0F, Vec3f.Y_AXIS)
                    .mulBack(cachedPatch.getModelMatrix(0.0F));

            OpenMatrix4f curWorldModelTf = OpenMatrix4f
                    .createTranslation((float) posCur.x, (float) posCur.y, (float) posCur.z)
                    .rotateDeg(180.0F, Vec3f.Y_AXIS)
                    .mulBack(cachedPatch.getModelMatrix(1.0F));

            // Apply Joint transform on top of world model transform
            OpenMatrix4f prevJointTf = cachedPatch.getArmature()
                    .getBoundTransformFor(prevPose, joint).mulFront(prevWorldModelTf);
            OpenMatrix4f curJointTf  = cachedPatch.getArmature()
                    .getBoundTransformFor(curPose, joint).mulFront(curWorldModelTf);

            // transform translation offset through the joint world matrix
            Vec3 prevJointPos = OpenMatrix4f.transform(prevJointTf, translation.toDoubleVector());
            Vec3 curJointPos  = OpenMatrix4f.transform(curJointTf, translation.toDoubleVector());

            // advance Or bootstrap position window
            if (!posBootstrapped) {
                prevPos.set((float) prevJointPos.x, (float) prevJointPos.y, (float) prevJointPos.z);
                currentPos.set((float) curJointPos.x, (float) curJointPos.y, (float) curJointPos.z);
                posBootstrapped = true;
            } else {
                prevPos.set(currentPos);
                currentPos.set((float) curJointPos.x, (float) curJointPos.y, (float) curJointPos.z);
            }

            // Rotation from current tick's full world joint transform
            if (updateRotation && !rotationFailed) {
                jomlMatrix.set(
                        curJointTf.m00, curJointTf.m01, curJointTf.m02, curJointTf.m03,
                        curJointTf.m10, curJointTf.m11, curJointTf.m12, curJointTf.m13,
                        curJointTf.m20, curJointTf.m21, curJointTf.m22, curJointTf.m23,
                        curJointTf.m30, curJointTf.m31, curJointTf.m32, curJointTf.m33
                );


                if (!rotBootstrapped) {
                    currentRot.setFromUnnormalized(jomlMatrix);
                    prevRot.set(currentRot);
                    rotBootstrapped = true;
                } else {
                    prevRot.set(currentRot);
                    currentRot.setFromUnnormalized(jomlMatrix);
                }
            }

        } catch (Exception e) {
            t0001.LOGGER.error("[JointTrackedEntityEffect] Tick sampling failed, disabling rotation: {}", e.getMessage());
            rotationFailed = true;
        }
    }
}