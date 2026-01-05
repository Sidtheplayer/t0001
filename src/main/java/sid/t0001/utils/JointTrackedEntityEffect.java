package sid.t0001.utils;


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
import org.joml.Quaternionf;
import sid.t0001.gameasset.ReusableEvents;
import sid.t0001.main.t0001;
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

    /**
     * (PAIN)
     *
     * @param fx photon fx location, typical usage: FX fx = FXHelper.getFX(ResourceLocation.parse("photon:trail"))
     * @param level the world level
     * @param entity the entity to track
     * @param joint the joint for rotation and position updates
     * @param translation offsets for the bone
     * @param autoRotate autorotate of the photon fx (e.g., AutoRotate.NONE(works for most cases))
     * @param updateRotation if true, updates rotation from bone; if false, only positional updates
     */
    public JointTrackedEntityEffect(FX fx, Level level, Entity entity, Joint joint, Vec3f translation, AutoRotate autoRotate, boolean updateRotation)
    {
        super(fx, level, entity, autoRotate);
        this.joint = joint;
        this.translation = translation;
        this.updateRotation = updateRotation;
    }

    @Override
    public void updateFXObjectFrame(IFXObject fxObject, float partialTicks) {
        if (runtime == null || fxObject != runtime.root) {
            return;
        }
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) {
            return;
        }

        Vec3 jointPos = ReusableEvents.JointTrack.getJointWithTranslation(localPlayer, entity, translation, joint);
        if (jointPos == null) {
            return;
        }

        runtime.root.updatePos(jointPos.toVector3f());


        if (updateRotation && entity instanceof LivingEntity living) {
            updateJointRotation(living, partialTicks);
        }
    }

    private void updateJointRotation(LivingEntity living, float partialTicks) {
        LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(living, LivingEntityPatch.class);
        if (patch == null) {
            return;
        }

        try {
            OpenMatrix4f transformMatrix = patch.getArmature()
                    .getBoundTransformFor(patch.getAnimator().getPose(partialTicks), joint);

            OpenMatrix4f finalMatrix = new OpenMatrix4f();
            OpenMatrix4f bodyRotation = new OpenMatrix4f().rotate(
                    -((float) Math.toRadians(living.yBodyRot + 180.0F)),
                    new Vec3f(0.0F, 1.0F, 0.0F)
            );
            OpenMatrix4f.mul(bodyRotation, transformMatrix, finalMatrix);

            org.joml.Matrix4f jomlMatrix = new org.joml.Matrix4f(
                    finalMatrix.m00, finalMatrix.m01, finalMatrix.m02, finalMatrix.m03,
                    finalMatrix.m10, finalMatrix.m11, finalMatrix.m12, finalMatrix.m13,
                    finalMatrix.m20, finalMatrix.m21, finalMatrix.m22, finalMatrix.m23,
                    finalMatrix.m30, finalMatrix.m31, finalMatrix.m32, finalMatrix.m33
            );

            Quaternionf rotation = new Quaternionf()
                    .setFromUnnormalized(jomlMatrix)
                    .rotateLocalX((float) Math.toRadians(90)); // Adjustment might be needed later on.

            if(runtime != null){
            runtime.root.updateRotation(rotation);}
            else {
                t0001.LOGGER.error("RUNTIME  IS NULL");}
        } catch (Exception ignored) {
        }
    }
}