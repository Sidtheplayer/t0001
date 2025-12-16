package sid.t0001.events;

import com.lowdragmc.photon.client.fx.FXRuntime;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.gameasset.EpicFightSounds;
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

    // Client-side FX tracking (only exists on client)
    @OnlyIn(Dist.CLIENT)
    private static final Map<UUID, FXRuntime> CLIENT_FX_RUNTIMES = new ConcurrentHashMap<>();

    public static class LightningEffectData {
        public int ticksLeft;
        public int totalTicks;
        public float totalDamage;
        public List<Integer> burstTimings; // When each burst should fire (countdown ticks)
        public List<Float> burstDamages;   // Damage for each burst
        public int currentBurst;

        public LightningEffectData(int duration, float totalDamage) {
            this.ticksLeft = duration;
            this.totalTicks = duration;
            this.totalDamage = totalDamage;
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
        ACTIVE_LIGHTNING.put(target, new LightningEffectData(duration, damage));
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
        } else {
            // Clean up old FX if replacing
            if (existing != null) {
                cleanupClientFX(target);
            }
            ACTIVE_LIGHTNING.put(target, new LightningEffectData(ticks, (float) amplifier));
        }
    }

    // Client-side FX storage
    @OnlyIn(Dist.CLIENT)
    public static void setClientFXRuntime(LivingEntity target, FXRuntime runtime) {
        if (runtime != null) {
            CLIENT_FX_RUNTIMES.put(target.getUUID(), runtime);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static FXRuntime getClientFXRuntime(LivingEntity target) {
        return CLIENT_FX_RUNTIMES.get(target.getUUID());
    }

    @OnlyIn(Dist.CLIENT)
    private static void removeClientFXRuntime(LivingEntity target) {
        CLIENT_FX_RUNTIMES.remove(target.getUUID());
    }

    // Helper method to safely check and destroy FX
    private static void cleanupClientFX(LivingEntity target) {
        if (target.level().isClientSide()) {
            cleanupClientFXClient(target);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void cleanupClientFXClient(LivingEntity target) {
        FXRuntime runtime = getClientFXRuntime(target);
        if (runtime != null && runtime.isAlive()) {
            try {
                runtime.destroy(true);
            } catch (Exception ignored) {}
        }
        removeClientFXRuntime(target);
    }



    public static int getLightningDuration(LivingEntity target) {
        LightningEffectData data = ACTIVE_LIGHTNING.get(target);
        return data != null ? data.ticksLeft : 0;
    }

    public static int calculateDuration(LivingEntity target) {
        return (int) Math.min(200, 40 + target.getHealth() * 5);
    }

    public static void removeLightningTarget(LivingEntity target) {
        ACTIVE_LIGHTNING.remove(target);
        cleanupClientFX(target);
    }

    private static void cleanupAndRemove(LivingEntity target, LightningEffectData data, Iterator<Map.Entry<LivingEntity, LightningEffectData>> iterator) {
        cleanupClientFX(target);
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

            // Process burst damage
            if (data.currentBurst < data.burstTimings.size()) {
                int nextBurstTiming = data.burstTimings.get(data.currentBurst);

                if (data.ticksLeft <= nextBurstTiming && data.ticksLeft > nextBurstTiming - 2) {

                    float burstDamage = data.burstDamages.get(data.currentBurst);

                    if (burstDamage > 0) {
                        target.hurt(target.damageSources().lightningBolt(), burstDamage);

                        // Determine stun type and strength
                        StunType stunType;
                        float stunStrength;

                        List<StunType> stunTypeList = List.of(StunType.LONG, StunType.HOLD);

                        if (data.currentBurst == 0) {
                            stunType = StunType.FALL;
                            stunStrength = Math.min(1.2f, burstDamage * 0.15f);
                        } else if (data.currentBurst == data.burstTimings.size() - 1) {
                            stunType = stunTypeList.get(target.level().getRandom().nextInt(stunTypeList.size()));
                            stunStrength = Math.scalb(0.8f, (int) (burstDamage * 0.1f));
                        } else {
                            stunType = StunType.SHORT;
                            stunStrength = Math.max(0.5f, burstDamage * 0.08f);
                        }

                        // Apply stun with sound
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
                                sound = EpicFightSounds.EVISCERATE.get();
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

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Check for dead FX runtimes and clean them up
        Iterator<Map.Entry<UUID, FXRuntime>> iterator = CLIENT_FX_RUNTIMES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, FXRuntime> entry = iterator.next();
            FXRuntime runtime = entry.getValue();

            if (runtime == null || !runtime.isAlive()) {
                iterator.remove();
            }
        }

        // Also check if entities with active lightning effects lost their FX
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level != null) {
            for (Map.Entry<LivingEntity, LightningEffectData> entry : ACTIVE_LIGHTNING.entrySet()) {
                LivingEntity target = entry.getKey();

                // If entity is in client world and FX is dead but effect is still active, remove it
                if (target.level() == mc.level) {
                    FXRuntime runtime = CLIENT_FX_RUNTIMES.get(target.getUUID());
                    if (runtime != null && !runtime.isAlive()) {
                        // FX died prematurely - this will trigger cleanup on next server tick
                        CLIENT_FX_RUNTIMES.remove(target.getUUID());
                    }
                }
            }
        }
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(LightningBallHandler.class);
    }
}