package sid.t0001.client;

import com.lowdragmc.photon.client.fx.FXRuntime;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side only handler for managing Photon FX runtimes for lightning balls.
 */
@OnlyIn(Dist.CLIENT)
public final class LightningBallClientHandler {
    private static final Map<Integer, FXRuntime> CLIENT_RUNTIMES = new ConcurrentHashMap<>();

    private LightningBallClientHandler() {}

    /**
     * Store the FX runtime for an entity on the client side
     */
    public static void setClientFXRuntime(LivingEntity target, FXRuntime runtime) {
        if (target == null || runtime == null) return;
        CLIENT_RUNTIMES.put(target.getId(), runtime);
    }

    /**
     * Remove and cleanup the FX runtime for an entity
     */
    public static void stopClientFX(int entityId) {
        FXRuntime runtime = CLIENT_RUNTIMES.remove(entityId);
        if (runtime == null) return;
        try {
            if (runtime.getRoot() != null) {
                runtime.getRoot().remove(true);
            }
        } catch (Throwable ignored) {
            // defensive: photon internals might change; fail silently (1.21.1 neoforge port incoming)
        }
    }
}

