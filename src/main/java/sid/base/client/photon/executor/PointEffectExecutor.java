package sid.base.client.photon.executor;

import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXEffectExecutor;
import com.lowdragmc.photon.client.gameobject.IFXObject;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;


@OnlyIn(Dist.CLIENT) //Why is this lowkey not included in base photon
public class PointEffectExecutor extends FXEffectExecutor {
    private final Vector3f spawnPos;

    public PointEffectExecutor(FX fx, Level level, Vector3f spawnPos) {
        super(fx, level);
        this.spawnPos = new Vector3f(spawnPos);
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
        if (runtime == null || fxObject != runtime.root) {
            return;
        }
        if (runtimeEnded()) {
            notifyFinished();
        }
    }

}
