package sid.t0001.events;

import com.lowdragmc.photon.client.fx.FXRuntime;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import sid.t0001.gameasset.t0001Sounds;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.StunType;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles lightning-ball damage, stun logic, and tick duration tracking.
 * clanker documentation
 */
@Mod.EventBusSubscriber
public class LightningBallHandler {
    /** Tracks active targets and their lightning effect data */
    private static final Map<LivingEntity, LightningEffectData> ACTIVE_LIGHTNING = new ConcurrentHashMap<>();
    private static final Map<LivingEntity, Float> LIGHTNING_AMPLIFIERS = new ConcurrentHashMap<>();

    public static class LightningEffectData {
        public int ticksLeft;
        public FXRuntime fxRuntime;

        public LightningEffectData(int ticksLeft, FXRuntime runtime) {
            this.ticksLeft = ticksLeft;
            this.fxRuntime = runtime;
        }
    }

    /**
     * Adds a target for lightning ball with automatically calculated duration (based on health, in ticks).
     */
    public static void addLightningTarget(LivingEntity target) {
        int duration = calculateDuration(target);
        ACTIVE_LIGHTNING.put(target, new LightningEffectData(duration, null));
    }

    /**
     * Adds a target with custom duration (in seconds).
     *
     * @param target   The entity affected
     * @param seconds  Duration in seconds (will be converted to ticks)
     * @param amplifier Defines short stun time
     * @param runtime  Optional FXRuntime to track visuals
     */
    public static void addLightningTarget(LivingEntity target, int seconds, int amplifier, FXRuntime runtime) {
        int ticks = Math.max(seconds * 20, 1);
        ACTIVE_LIGHTNING.put(target, new LightningEffectData(ticks, runtime));
        LIGHTNING_AMPLIFIERS.put(target, 0.5f * amplifier);
    }

    /**
     * Returns the remaining lightning effect duration (in ticks).
     */
    public static int getLightningDuration(LivingEntity target) {
        LightningEffectData data = ACTIVE_LIGHTNING.get(target);
        return data != null ? data.ticksLeft : 0;
    }

    /**
     * Returns the remaining lightning effect duration (in seconds, rounded down).
     */
    public static int getLightningDurationSeconds(LivingEntity target) {
        return getLightningDuration(target) / 20;
    }

    /**
     * Calculates how long the lightning effect should last based on target health.
     *
     * @param target the affected entity
     * @return number of ticks for the lightning effect
     */
    public static int calculateDuration(LivingEntity target) {
        return (int) Math.min(200, 40 + target.getHealth() * 5);
    }

    /**
     * Removes a target from active lightning tracking.
     */
    public static void removeLightningTarget(LivingEntity target) {
        ACTIVE_LIGHTNING.remove(target);
        LIGHTNING_AMPLIFIERS.remove(target);
    }

    // Cleanup helper
    private static void cleanupAndRemove(LivingEntity target, LightningEffectData data, Iterator<Map.Entry<LivingEntity, LightningEffectData>> iterator) {
        if (data != null && data.fxRuntime != null && data.fxRuntime.isAlive()) {
            try {
                data.fxRuntime.destroy(true);
            } catch (Exception ignored) { }
        }
        iterator.remove();
        LIGHTNING_AMPLIFIERS.remove(target);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Iterator<Map.Entry<LivingEntity, LightningEffectData>> iterator = ACTIVE_LIGHTNING.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<LivingEntity, LightningEffectData> entry = iterator.next();
            LivingEntity target = entry.getKey();
            LightningEffectData data = entry.getValue();

            // If data missing, remove
            if (data == null) {
                iterator.remove();
                LIGHTNING_AMPLIFIERS.remove(target);
                continue;
            }

            // If target is dead or invalid -> cleanup
            if (target == null || !target.isAlive()) {
                cleanupAndRemove(target, data, iterator);
                continue;
            }

            // If runtime exists but is dead -> cleanup
            if (data.fxRuntime != null && !data.fxRuntime.isAlive()) {
                cleanupAndRemove(target, data, iterator);
                continue;
            }

            // If already expired -> cleanup
            if (data.ticksLeft <= 0) {
                cleanupAndRemove(target, data, iterator);
                continue;
            }

            /*
             * Apply damage + stun at completely not insane intervals,
             * not every single server tick.
             */
            if (data.ticksLeft % 25 == 0) {
                // clamp amplifier to a non-insane maximum so we don't cheese bosses
                float rawAmp = LIGHTNING_AMPLIFIERS.getOrDefault(target, 0.5f);
                float amp = Math.min(rawAmp, 3.0f); // cap damage per application
                target.hurt(target.damageSources().magic(), amp);

                // clamp stun strength between 0 and 1
                float stunAmp = Math.min(1.0F, amp);

                // weighted random stun selection
                StunType randomStun;
                double rand = Math.random();
                if (rand < 0.6) randomStun = StunType.SHORT;
                else if (rand < 0.9) randomStun = StunType.FALL;
                else randomStun = StunType.LONG;

                SoundEvent randomSound;
                if (rand < 0.5) randomSound = SoundEvents.LAVA_EXTINGUISH;
                else if (rand < 0.8) randomSound = SoundEvents.CANDLE_EXTINGUISH;
                else if (rand < 0.9)randomSound = SoundEvents.FIRE_EXTINGUISH;
                else randomSound = t0001Sounds.AMOGUS_DEATH.get();


                LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(target, LivingEntityPatch.class);
                if (patch != null && patch.getOriginal().getHealth() > 1.5F) {
                    patch.applyStun(randomStun, stunAmp);
                    patch.playSound(randomSound, 1.2F, 0.8F);
                }
            }

            // decrement tick counter
            data.ticksLeft--;
        }
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(LightningBallHandler.class);
    }
}
