package sid.t0001.events;

import com.lowdragmc.photon.client.fx.FXRuntime;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.StunType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles lightning-ball damage in bursts with proper multi-player stacking.
 */
@Mod.EventBusSubscriber
public class LightningBallHandler {
    private static final Map<LivingEntity, LightningEffectData> ACTIVE_LIGHTNING = new ConcurrentHashMap<>();

    public static class LightningEffectData {
        public int ticksLeft;
        public int totalTicks;
        public float totalDamage;
        public List<Integer> burstTimings; // When each burst should fire (countdown ticks)
        public List<Float> burstDamages;   // Damage for each burst
        public int currentBurst;
        public FXRuntime fxRuntime;

        public LightningEffectData(int duration, float totalDamage, FXRuntime runtime) {
            this.ticksLeft = duration;
            this.totalTicks = duration;
            this.totalDamage = totalDamage;
            this.fxRuntime = runtime;
            this.currentBurst = 0;

            // Calculate number of bursts based on duration
            int numBursts = Math.max(2, Math.min(5, 1 + (duration / 40)));

            this.burstTimings = new ArrayList<>();
            this.burstDamages = new ArrayList<>();

            // Distribute burst
            int interval = duration / numBursts;

            // First burst is immediate (at full duration countdown)
            // Subsequent bursts spread out
            for (int i = 0; i < numBursts; i++) {
                int burstTiming = duration - (i * interval);
                this.burstTimings.add(burstTiming);

                // Damage distribution: First burst = 40%, rest distributed evenly
                float burstDamage;
                if (i == 0) {
                    burstDamage = totalDamage * 0.4f;
                } else {
                    burstDamage = (totalDamage * 0.6f) / (numBursts - 1);
                }
                this.burstDamages.add(burstDamage);
            }
        }

        // Add damage from another source (stacking)
        public void addStackedDamage(float additionalDamage, int additionalDuration) {
            this.totalDamage += additionalDamage;

            // Extend duration if new one is longer
            if (additionalDuration > this.ticksLeft) {
                this.ticksLeft = additionalDuration;
                this.totalTicks = Math.max(this.totalTicks, additionalDuration);
            }

            // Recalculate burst distribution with new total damage
            int numBursts = this.burstDamages.size();
            this.burstDamages.clear();

            for (int i = 0; i < numBursts; i++) {
                float burstDamage = getBurstDamage(i, numBursts);
                this.burstDamages.add(burstDamage);
            }

            // Cap total damage to prevent abuse
            this.totalDamage = Math.min(this.totalDamage, 50.0f);
        }

        private float getBurstDamage(int i, int numBursts) {
            float burstDamage;
            if (i == 0 && currentBurst == 0) {
                // First burst not fired yet
                burstDamage = totalDamage * 0.4f;
            } else if (i < currentBurst) {
                // Already fired, keep original value
                burstDamage = 0;
            } else {
                // Redistribute remaining damage
                int remainingBursts = numBursts - Math.max(1, currentBurst);
                float remainingDamage = totalDamage * (currentBurst == 0 ? 0.6f : 1.0f);
                burstDamage = remainingDamage / remainingBursts;
            }
            return burstDamage;
        }
    }

    public static void addLightningTarget(LivingEntity target) {
        int duration = calculateDuration(target);
        float damage = Math.min(10.0f, target.getMaxHealth() * 0.1f);
        ACTIVE_LIGHTNING.put(target, new LightningEffectData(duration, damage, null));
    }

    public static void addLightningTarget(LivingEntity target, int amperage, int amplifier, FXRuntime runtime) {
        addLightningTarget(target, amperage, amplifier, runtime, StackMode.EXTEND);
    }

    public enum StackMode {
        EXTEND,   // Add damage and extend duration (default)
        REFRESH   // Restart and Replace.
    }

    public static void addLightningTarget(LivingEntity target, int amperage, int amplifier, FXRuntime runtime, StackMode mode) {
        int ticks = Math.max(amperage * 24, 40);

        LightningEffectData existing = ACTIVE_LIGHTNING.get(target);

        if (existing != null && mode == StackMode.EXTEND) {
            existing.addStackedDamage((float) amplifier, ticks);

            if (runtime != null && (existing.fxRuntime == null || !existing.fxRuntime.isAlive())) {
                existing.fxRuntime = runtime;
            }
        } else {
            if (existing != null && existing.fxRuntime != null && existing.fxRuntime.isAlive()) {
                try { existing.fxRuntime.destroy(true); }
                catch (Exception ignored) {}
            }
            ACTIVE_LIGHTNING.put(target, new LightningEffectData(ticks, (float) amplifier, runtime));
        }
    }

    public static int getLightningDuration(LivingEntity target) {
        LightningEffectData data = ACTIVE_LIGHTNING.get(target);
        return data != null ? data.ticksLeft : 0;
    }

    public static int getLightningDurationSeconds(LivingEntity target) {
        return getLightningDuration(target) / 20;
    }

    public static int calculateDuration(LivingEntity target) {
        return (int) Math.min(200, 40 + target.getHealth() * 5);
    }

    public static void removeLightningTarget(LivingEntity target) {
        ACTIVE_LIGHTNING.remove(target);
    }

    private static void cleanupAndRemove(LivingEntity target, LightningEffectData data, Iterator<Map.Entry<LivingEntity, LightningEffectData>> iterator) {
        if (data != null && data.fxRuntime != null && data.fxRuntime.isAlive()) {
            try {
                data.fxRuntime.destroy(true);
            } catch (Exception ignored) { }
        }
        iterator.remove();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Iterator<Map.Entry<LivingEntity, LightningEffectData>> iterator = ACTIVE_LIGHTNING.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<LivingEntity, LightningEffectData> entry = iterator.next();
            LivingEntity target = entry.getKey();
            LightningEffectData data = entry.getValue();

            // Cleanup invalid targets and dead target
            if (data == null || target == null || !target.isAlive() || target.getHealth() <= 0) {
                cleanupAndRemove(target, data, iterator);
                continue;
            }

            // Check and Cleanup if FX runtime is dead
            if (data.fxRuntime != null && !data.fxRuntime.isAlive()) {
                cleanupAndRemove(target, data, iterator);
                continue;
            }


            if (data.currentBurst < data.burstTimings.size()) {
                int nextBurstTiming = data.burstTimings.get(data.currentBurst);

                if (data.ticksLeft <= nextBurstTiming && data.ticksLeft > nextBurstTiming - 2) {

                    float burstDamage = data.burstDamages.get(data.currentBurst);

                    if (burstDamage > 0) {
                        target.hurt(target.damageSources().lightningBolt(), burstDamage);


                        StunType stunType;
                        float stunStrength;

                        List<StunType> stunTypeList = List.of(StunType.LONG,StunType.HOLD);

                        if (data.currentBurst == 0) {

                            stunType = StunType.FALL;
                            stunStrength = Math.min(1.2f, burstDamage * 0.15f);

                        } else if (data.currentBurst == data.burstTimings.size() - 1) {

                            stunType = stunTypeList.get(target.level().getRandom().nextInt(stunTypeList.size())) ;
                            stunStrength = Math.min(0.8f, burstDamage * 0.1f);
                        } else {

                            stunType = StunType.SHORT;
                            stunStrength = Math.min(0.5f, burstDamage * 0.08f);
                        }

                        // Apply stun w sound
                        LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(target, LivingEntityPatch.class);
                        if (patch != null) {
                            patch.applyStun(stunType, stunStrength);

                            // Sound variety
                            SoundEvent sound;
                            float pitch;
                            if (data.currentBurst == 0) {
                                sound = SoundEvents.TRIDENT_THUNDER;
                                pitch = 1.0F;
                            } else if (data.currentBurst == data.burstTimings.size() - 1) {
                                sound = SoundEvents.TRIDENT_THUNDER;
                                pitch = 1.3F;
                            } else {
                                sound = Math.random() < 0.5 ? SoundEvents.LAVA_EXTINGUISH : SoundEvents.FIRE_EXTINGUISH;
                                pitch = 0.8F + (float)Math.random() * 0.4F;
                            }

                            patch.playSound(sound, 1.0F, pitch);
                        }
                    }

                    data.currentBurst++;
                }
            }

            // Countdown
            data.ticksLeft--;

            // Cleanup if duration expires
            if (data.ticksLeft <= 0) {
                cleanupAndRemove(target, data, iterator);
            }
        }
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(LightningBallHandler.class);
    }
}