package sid.t0001.client;

import com.lowdragmc.photon.client.fx.EntityEffect;
import com.lowdragmc.photon.client.fx.FXHelper;
import com.lowdragmc.photon.client.fx.FXRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * for lightinin fx
 *
 */
@OnlyIn(Dist.CLIENT)
public class LightningBallClientHandler {

    private static final Map<UUID, ActiveFX> ACTIVE_FX = new HashMap<>();

    private static class ActiveFX {
        FXRuntime primaryFX;
        int ticksAlive;

        ActiveFX(FXRuntime fx) {
            this.primaryFX = fx;
            this.ticksAlive = 0;
        }

        boolean isAlive() {
            return primaryFX != null && primaryFX.isAlive();
        }

        void destroy() {
            if (primaryFX != null && primaryFX.isAlive()) {
                try {
                    primaryFX.destroy(true);
                } catch (Exception ignored) {}
            }
        }
    }

    public static void register() {
            MinecraftForge.EVENT_BUS.register(LightningBallClientHandler.class);
    }



    public static void spawnLightningFX(int entityId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Entity entity = mc.level.getEntity(entityId);
        if (!(entity instanceof LivingEntity target)) return;

        UUID targetUUID = target.getUUID();
        ActiveFX existing = ACTIVE_FX.get(targetUUID);

        // If FX already exists and is alive, spawn a burst effect
        if (existing != null && existing.isAlive()) {
            spawnBurstEffect(target);
            return;
        }

        // Otherwise, create primary continuous FX
        createPrimaryFX(target);
    }

    /**
     * Creates the main continuous lightning ball effect
     */
    private static void createPrimaryFX(LivingEntity target) {
        EntityEffect lightningBall = new EntityEffect(
                FXHelper.getFX(ResourceLocation.parse("photon:yellow_lightning_ball")),
                target.level(),
                target,
                EntityEffect.AutoRotate.NONE
        );
        lightningBall.setOffset(0, 1, 0);
        lightningBall.setRotation(0, 0, 0);
        lightningBall.setScale(1, 1, 1);
        lightningBall.setAllowMulti(true);
        lightningBall.setForcedDeath(false);
        lightningBall.start();

        ACTIVE_FX.put(target.getUUID(), new ActiveFX(lightningBall.getRuntime()));
    }

    /**
     * Create burst/flash
     */
    private static void spawnBurstEffect(LivingEntity target) {
        // Create a temporary burst effect that auto-destroys
        EntityEffect burst = new EntityEffect(
                FXHelper.getFX(ResourceLocation.parse("photon:yellow_lightning_ball")), //balls hehe
                target.level(),
                target,
                EntityEffect.AutoRotate.NONE
        );
        burst.setOffset(0, 1, 0);
        burst.setRotation(0, 0, 0);
        burst.setScale(1.3f, 1.3f, 1.3f);
        burst.setAllowMulti(true);
        burst.setForcedDeath(true);
        burst.start();


    }

    /**
     * Stops and removes lightning FX from target entity,
     * doesnt work because its hard to track both entity and fx
     * from server and clientside practically.
     */
    public static void stopLightningFX(int entityId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Entity entity = mc.level.getEntity(entityId);
        if (entity != null) {
            stopLightningFX(entity.getUUID());
        }
    }

    public static void stopLightningFX(UUID targetUUID) {
        ActiveFX fx = ACTIVE_FX.remove(targetUUID);
        if (fx != null) {
            fx.destroy();
        }
    }


}