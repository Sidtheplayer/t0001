package sid.base.client.photon.executor;

import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXEffectExecutor;
import com.lowdragmc.photon.client.gameobject.IFXObject;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@OnlyIn(Dist.CLIENT)
public class FixedXROTPointFXExecutor extends FXEffectExecutor {
    private final Vector3f spawnPos;
    private final Entity entity; // for YRot

    public FixedXROTPointFXExecutor(FX fx, Level level, Vector3f spawnPos, Entity entity) {
        super(fx, level);
        this.spawnPos = new Vector3f(spawnPos);
        this.entity = entity;
    }

    @Override
    public void start() {
        resetFinishedNotification();
        this.runtime = fx.createRuntime();
        var root = this.runtime.getRoot();
        root.updatePos(new Vector3f(spawnPos).add(offset));
        root.updateRotation(rotation);
        root.updateScale(scale);
        this.runtime.emit(this, delay);
    }

    @Override
    public void updateFXObjectTick(IFXObject fxObject) {
        if (runtime == null || fxObject != runtime.root) return;
        if (runtimeEnded()) notifyFinished();
    }

    @Override
    public void updateFXObjectFrame(IFXObject fxObject, float partialTicks) {
        if (runtime == null || fxObject != runtime.root) return;

        // fixed pos but updating rotation though
        runtime.root.updatePos(new Vector3f(spawnPos).add(offset));

        LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);

        float yRot0 = patch != null ? patch.getYRotO() : entity.yRotO;
        float yRot = patch != null ? patch.getYRot() : entity.getYRot();

        float yaw = (float) Math.toRadians(Mth.lerp(partialTicks, yRot0, yRot));

        runtime.root.updateRotation(new Quaternionf(rotation).rotateY(yaw));
    }
}