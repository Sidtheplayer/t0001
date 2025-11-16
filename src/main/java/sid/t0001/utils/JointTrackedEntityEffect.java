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
    private final boolean updaterotation;

    /**
     * @param fx photon fxlocation, typical usage: FX #fxname = FXHELPER.getFx(Resourcelocation.parse("photon:trail"))
     * @param joint the joint u need rot and pos updates for
     * @param translation offsets for the bone
     * @param autoRotate autorotate of the poton fx example: AUTOROTATE.NONE normally works in most scenarios for me
     * @param updaterot boolean - if u want to update rotation from bone or just need positional updates
     * @param entity the entity u will be using this for
     */
    public JointTrackedEntityEffect(FX fx, Level level, Entity entity, LocalPlayer localPlayer, Joint joint, Vec3f translation, AutoRotate autoRotate, boolean updaterot) {
        super(fx, level, entity, autoRotate);
        this.localPlayer = localPlayer;
        this.joint = joint;
        this.translation = translation;
        this.updaterotation = updaterot;
    }

    @Override
    public void updateFXObjectFrame(IFXObject fxObject, float partialTicks) {
        if (runtime != null && fxObject == runtime.root) {
            Vec3 jointPos = ReusableEvents.MyFxHelpers.JointTrack.getJointWithTranslation(localPlayer, entity, translation, joint);

            if (jointPos != null) {
                // Don't subtract offset - you should just use joint position directly
                // The offset should be applied by Photon's internal system
                runtime.root.updatePos(jointPos.toVector3f());

                if (entity instanceof LivingEntity living && updaterotation) {
                    LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(living, LivingEntityPatch.class);
                    if (patch != null) {
                        // Get the transform matrix
                        OpenMatrix4f transformMatrix = patch.getArmature()
                                .getBoundTransformFor(patch.getAnimator().getPose(partialTicks), joint);


                        OpenMatrix4f finalMatrix = new OpenMatrix4f();
                        OpenMatrix4f bodyRotation = new OpenMatrix4f().rotate(
                                -((float) Math.toRadians(living.yBodyRot + 180.0F)),
                                new Vec3f(0.0F, 1.0F, 0.0F)
                        );
                        OpenMatrix4f.mul(bodyRotation, transformMatrix, finalMatrix);

                        // Convert to JOML
                        org.joml.Matrix4f jomlMatrix = new org.joml.Matrix4f(
                                finalMatrix.m00, finalMatrix.m01, finalMatrix.m02, finalMatrix.m03,
                                finalMatrix.m10, finalMatrix.m11, finalMatrix.m12, finalMatrix.m13,
                                finalMatrix.m20, finalMatrix.m21, finalMatrix.m22, finalMatrix.m23,
                                finalMatrix.m30, finalMatrix.m31, finalMatrix.m32, finalMatrix.m33
                        );


                        Quaternionf rotation = new Quaternionf()
                                .setFromUnnormalized(jomlMatrix)
                                .rotateLocalX((float)Math.toRadians(90)); // Adjust based on orientation

                        runtime.root.updateRotation(rotation); //update rotation in time
                    }
                }
            }
        }
    }
}