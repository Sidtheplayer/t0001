package sid.t0001.utils;


import com.lowdragmc.photon.client.fx.EntityEffect;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.gameobject.IFXObject;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import sid.t0001.gameasset.ReusableEvents;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class JointTrackedEntityEffect extends EntityEffect {
    private final LocalPlayer localPlayer;
    private final Joint joint;
    private final Vec3f translation;

    public JointTrackedEntityEffect(FX fx, Level level, Entity entity, LocalPlayer localPlayer, Joint joint, Vec3f translation, AutoRotate autoRotate, Vec3f offsets) {
        super(fx, level, entity, autoRotate);
        this.localPlayer = localPlayer;
        this.joint = joint;
        this.translation = translation;
        this.offset = offsets.toMojangVector();
    }

    @Override
    public void updateFXObjectFrame(IFXObject fxObject, float partialTicks) {
        if (runtime != null && fxObject == runtime.root) {
            // Recalculate joint position every frame
            Vec3 jointPos = ReusableEvents.MyFxHelpers.JointTrack.getJointWithTranslation(localPlayer, entity, translation, joint);

            if (jointPos != null) {
                Vec3f offsetted_updatepos = Vec3f.fromDoubleVector(
                        new Vec3(
                                jointPos.x - this.offset.x,
                                jointPos.y - this.offset.y,
                                jointPos.z - this.offset.z
                        )
                );

                runtime.root.updatePos(offsetted_updatepos.toMojangVector());


                if (entity instanceof LivingEntity living) {
                    LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(living, LivingEntityPatch.class);
                    if (patch != null) {

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

                        Quaternionf rotation = new Quaternionf().setFromUnnormalized(jomlMatrix);

//                        runtime.root.updateRotation(rotation);
                    }
                }
            }
        }
    }
}