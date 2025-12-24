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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Client-side manager for lightning FX effects.
 * Handles spawning, tracking, and cleanup of visual effects only.
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

    /**
     * Spawns or refreshes lightning FX on target entity.
     * If FX already exists, adds a burst effect instead of replacing.
     */
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
        lightningBall.setAllowMulti(false);
        lightningBall.setForcedDeath(true);
        lightningBall.start();

        ACTIVE_FX.put(target.getUUID(), new ActiveFX(lightningBall.getRuntime()));
    }

    /**
     * Creates a burst/flash effect for additional hits
     */
    private static void spawnBurstEffect(LivingEntity target) {
        // Create a temporary burst effect that auto-destroys
        EntityEffect burst = new EntityEffect(
                FXHelper.getFX(ResourceLocation.parse("photon:yellow_lightning_ball")),
                target.level(),
                target,
                EntityEffect.AutoRotate.NONE
        );
        burst.setOffset(0, 1, 0);
        burst.setRotation(0, 0, 0);
        burst.setScale(1.3f, 1.3f, 1.3f); // Slightly larger for burst
        burst.setAllowMulti(true); // Allow multiple instances for burst
        burst.setForcedDeath(true);
        burst.start();

        // The burst effect will auto-cleanup, we don't need to track it
    }

    /**
     * Stops and removes lightning FX from target entity
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

    /**
     * Client tick to clean up dead FX
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Iterator<Map.Entry<UUID, ActiveFX>> iterator = ACTIVE_FX.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, ActiveFX> entry = iterator.next();
            ActiveFX fx = entry.getValue();

            // Remove if FX died naturally
            if (!fx.isAlive()) {
                iterator.remove();
                continue;
            }

            fx.ticksAlive++;

            // Safety: remove if FX has been alive too long (10 seconds)
            // This prevents leaks if server never sends stop packet
            if (fx.ticksAlive > 200) {
                fx.destroy();
                iterator.remove();
            }
        }
    }
}