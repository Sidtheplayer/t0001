package sid.base.client.photon.fx;


import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXEffectExecutor;
import com.lowdragmc.photon.client.gameobject.FXObject;
import com.lowdragmc.photon.client.gameobject.IFXObject;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.*;

//MIT CODE FROM: https://github.com/dfdyz/EpicVFX

public class EFPatchExecutor extends FXEffectExecutor {
    public static Map<LivingEntityPatch<?>, List<EFPatchExecutor>> CACHE = new HashMap<>();
    public final LivingEntityPatch<?> entityPatch;
    public final JointPoseGetter poseGetter;

    private boolean destroyed = false;

    @FunctionalInterface
    public interface JointPoseGetter{
        OpenMatrix4f handle(LivingEntityPatch<?> entityPatch, float partialTicks);
    }

    public EFPatchExecutor(FX fx, Level level, LivingEntityPatch<?> entityPatch, JointPoseGetter poseGetter
    ) {
        super(fx, level);
        this.entityPatch = entityPatch;
        this.poseGetter = poseGetter;
        setAllowMulti(true);
        setForcedDeath(true);
    }

    @Override
    public void updateFXObjectTick(IFXObject fxObject) {
        if (runtime != null && fxObject == runtime.root) {
            if (!entityPatch.getOriginal().isAlive()) {
                destroy();
            }

            boolean isAlive = false;
            for (IFXObject sceneObject : runtime.getObjects().values()) {
                if(sceneObject == runtime.root) continue;
                if(sceneObject instanceof FXObject fxO){
                    isAlive = isAlive || fxO.isAlive();
                }
            }
            if(!isAlive) destroy();
        }
    }

    public void destroy(){
        if(destroyed) return;
        destroyed = true;
        //System.out.println("Destroyed.");
        runtime.destroy(forcedDeath);
        CACHE.computeIfAbsent(entityPatch, p -> new ArrayList<>()).remove(this);
        if (CACHE.get(entityPatch).isEmpty()) {
            CACHE.remove(entityPatch);
        }
    }

    public static JointPoseGetter GetJointHandler(Joint joint){
        return (entitypatch, partialTicks) -> {
            //var position = entitypatch.getOriginal().getPosition(partialTicks);
            Animator animator = entitypatch.getAnimator();
            Pose pose = animator.getPose(partialTicks);
            return new OpenMatrix4f(entitypatch.getArmature().getBoundTransformFor(pose, joint));
        };
    }

    @Override
    public void updateFXObjectFrame(IFXObject fxObject, float partialTicks) {
        if (runtime != null && fxObject == runtime.root) {
            updateRoot(partialTicks);
        }
    }

    private void printAngle(String title, Vector3f v){
        v.x = (float) Math.toDegrees(v.x);
        v.y = (float) Math.toDegrees(v.y);
        v.z = (float) Math.toDegrees(v.z);
        System.out.println(title + "(%.1f, %.1f, %.1f)".formatted(v.x ,v.y, v.z));
    }

    protected void updateRoot(float partialTicks){
        Vec3 pos = entityPatch.getOriginal().getPosition(partialTicks);
        OpenMatrix4f modelTf = OpenMatrix4f.createTranslation((float)pos.x, (float)pos.y, (float)pos.z)
                .rotateDeg(180.0F, Vec3f.Y_AXIS)
                .mulBack(entityPatch.getModelMatrix(partialTicks));

        var finalTf = poseGetter.handle(entityPatch, partialTicks).mulFront(modelTf);
        var euler = finalTf.toQuaternion().getEulerAnglesZXY(new Vector3f());

        runtime.root.updatePos(OpenMatrix4f.transform(finalTf, Vec3.ZERO).toVector3f());
        runtime.root.updateRotation(new Quaternionf().rotateX(-euler.x).rotateZ(-euler.z).rotateLocalY(-euler.y));
        runtime.root.updateScale(finalTf.toScaleVector().toMojangVector());
    }


    @Override
    public void start() {
        if (!entityPatch.getOriginal().isAlive()) return;

        var effects = CACHE.computeIfAbsent(entityPatch, p -> new ArrayList<>());

        if (!allowMulti) {
            var iter = effects.iterator();
            while (iter.hasNext()) {
                var effect = iter.next();
                boolean removed = false;
                if (effect.runtime != null && !effect.runtime.isAlive()) {
                    iter.remove();
                    removed = true;
                }
                if ((effect.fx.equals(fx) || Objects.equals(effect.fx.getFxLocation(), fx.getFxLocation())) && !removed) {
                    return;
                }
            }
        }
        this.runtime = fx.createRuntime();

        updateRoot(1);

        this.runtime.emit(this, delay);
        effects.add(this);
    }

}