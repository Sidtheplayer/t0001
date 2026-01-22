package sid.base.client;


import com.lowdragmc.photon.client.fx.BlockEffectExecutor;
import com.lowdragmc.photon.client.fx.EntityEffectExecutor;
import com.lowdragmc.photon.client.fx.FXHelper;
import com.lowdragmc.photon.client.fx.FXRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import sid.base.main.t0001;

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

    }

    public static void register() {
            NeoForge.EVENT_BUS.register(LightningBallClientHandler.class);
    }



    public static void spawnLightningFX(LivingEntity target) {
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
        BlockEffectExecutor lightningBall = new BlockEffectExecutor(
                FXHelper.getFX(ResourceLocation.parse("photon:yellow_lightning_ball")),
                target.level(),
                target.getOnPos()

        );
        lightningBall.setOffset(0, -1, 0);
        lightningBall.setRotation(0, 0, 0);
        lightningBall.setScale(1, 1, 1);
        lightningBall.setAllowMulti(true);
        lightningBall.setForcedDeath(false);
        lightningBall.setCheckState(false);
        lightningBall.start();
        t0001.LOGGER.debug("LIGHTNINGFXHASBEENSTARTED");

        ACTIVE_FX.put(target.getUUID(), new ActiveFX(lightningBall.getRuntime()));
    }

    /**
     * Create burst/flash
     */
    private static void spawnBurstEffect(LivingEntity target) {
        // Create a temporary burst effect that auto-destroys
        BlockEffectExecutor burst = new BlockEffectExecutor(
                FXHelper.getFX(ResourceLocation.parse("photon:yellow_lightning_ball")), //balls hehe
                target.level(),
                target.getOnPos()
        );
        burst.setOffset(0, -1, 0);
        burst.setRotation(0, 0, 0);
        burst.setScale(1.3, 1.3, 1.3);
        burst.setAllowMulti(true);
        burst.setForcedDeath(true);
        burst.setCheckState(false);
        burst.start();
        t0001.LOGGER.debug("LIGHTNINGFXHASBEENSTARTED");



    }


}