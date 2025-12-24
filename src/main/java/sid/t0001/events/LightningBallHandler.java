package sid.t0001.events;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import sid.t0001.network.SpawnLightningFxPacket;
import sid.t0001.network.StopLightningFxPacket;
import sid.t0001.network.t0001NetworkManager;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.StunType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side handler for lightning ball damage and stun logic.
 * <p>
 * Does:
 * - Track active lightning effects
 * - Apply burst damage
 * - Apply Epic Fight stuns
 * - Decide when lightning starts and ends
 * - Send packets to clients to spawn/stop FX
 * <p>
 * Does not:
 * - Spawning FX by itself
 * - Managing FX lifetime
 * - Client-side cleanup
 * <p>
 * FX lifecycle must be handled via packets now after seeing previous implementations have problems in dedicated servers.
 */
@Mod.EventBusSubscriber
public class LightningBallHandler {

    private static final Map<LivingEntity, LightningEffectData> ACTIVE_LIGHTNING =
            new ConcurrentHashMap<>();


    /*  Data Model                                                             */

    public static class LightningEffectData {
        public int ticksLeft;
        public int totalTicks;
        public float totalDamage;

        public final List<Integer> burstTimings;
        public final List<Float> burstDamages;

        public int currentBurst;

        public LightningEffectData(int duration, float totalDamage) {
            this.ticksLeft = duration;
            this.totalTicks = duration;
            this.totalDamage = totalDamage;
            this.currentBurst = 0;

            int numBursts = Math.max(2, Math.min(5, 1 + duration / 40));

            this.burstTimings = new ArrayList<>();
            this.burstDamages = new ArrayList<>();

            int interval = duration / numBursts;

            for (int i = 0; i < numBursts; i++) {
                burstTimings.add(duration - i * interval);

                float dmg;
                if (i == 0) {
                    dmg = totalDamage * 0.4f;
                } else {
                    dmg = (totalDamage * 0.6f) / (numBursts - 1);
                }
                burstDamages.add(dmg);
            }
        }

        public void addStackedDamage(float additionalDamage, int additionalDuration) {
            totalDamage = Math.min(totalDamage + additionalDamage, 50.0f);

            if (additionalDuration > ticksLeft) {
                ticksLeft = additionalDuration;
                totalTicks = Math.max(totalTicks, additionalDuration);
            }

            int numBursts = burstDamages.size();
            burstDamages.clear();

            for (int i = 0; i < numBursts; i++) {
                burstDamages.add(recalculateBurstDamage(i, numBursts));
            }
        }

        private float recalculateBurstDamage(int i, int numBursts) {
            if (i < currentBurst) {
                return 0.0f;
            }

            if (i == currentBurst && currentBurst == 0) {
                return totalDamage * 0.4f;
            }

            int remaining = numBursts - Math.max(1, currentBurst);
            float remainingDamage = totalDamage * (currentBurst == 0 ? 0.6f : 1.0f);
            return remainingDamage / remaining;
        }
    }


    //  API shit

    public static void addLightningTarget(LivingEntity target) {
        int duration = calculateDuration(target);
        float damage = Math.min(10.0f, target.getMaxHealth() * 0.1f);

        ACTIVE_LIGHTNING.put(target, new LightningEffectData(duration, damage));

        // Send spawn packet to clients tracking this entity
        sendSpawnFxPacket(target);
    }

    public enum StackMode {
        EXTEND,
        REFRESH
    }

    public static void addLightningTarget(
            LivingEntity target,
            int amperage,
            int amplifier,
            StackMode mode
    ) {
        int ticks = Math.max(amperage * 24, 40);
        LightningEffectData existing = ACTIVE_LIGHTNING.get(target);

        if (existing != null && mode == StackMode.EXTEND) {
            existing.addStackedDamage(amplifier, ticks);
            // Don't send new FX packet, just extend existing
        } else {
            ACTIVE_LIGHTNING.put(target, new LightningEffectData(ticks, amplifier));
            // Send spawn packet for new/refreshed effect
            sendSpawnFxPacket(target);
        }
    }

    /**
     * Helper method to send spawn FX packet to all clients tracking the entity
     */
    private static void sendSpawnFxPacket(LivingEntity target) {
        if (!target.level().isClientSide()) {
            t0001NetworkManager.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target),
                    new SpawnLightningFxPacket(target.getId())
            );
        }
    }

    /**
     * Helper method to send stop FX packet to all clients tracking the entity
     */
    private static void sendStopFxPacket(LivingEntity target) {
        if (!target.level().isClientSide()) {
            t0001NetworkManager.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target),
                    new StopLightningFxPacket(target.getId())
            );
        }
    }

    public static void removeLightningTarget(LivingEntity target) {
        ACTIVE_LIGHTNING.remove(target);
        sendStopFxPacket(target);
    }

    public static int getLightningDuration(LivingEntity target) {
        LightningEffectData data = ACTIVE_LIGHTNING.get(target);
        return data != null ? data.ticksLeft : 0;
    }

    public static int calculateDuration(LivingEntity target) {
        return (int) Math.min(200, 40 + target.getHealth() * 5);
    }

    /*  Server Tick                                                            */

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Iterator<Map.Entry<LivingEntity, LightningEffectData>> it =
                ACTIVE_LIGHTNING.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<LivingEntity, LightningEffectData> entry = it.next();
            LivingEntity target = entry.getKey();
            LightningEffectData data = entry.getValue();

            if (target == null || !target.isAlive()) {
                it.remove();
                if (target != null) {
                    sendStopFxPacket(target);
                }
                continue;
            }

            processBursts(target, data);

            data.ticksLeft--;

            if (data.ticksLeft <= 0) {
                it.remove();
                sendStopFxPacket(target);
            }
        }
    }

    /*  Damage + Stun Logic                                                     */

    private static void processBursts(LivingEntity target, LightningEffectData data) {
        if (data.currentBurst >= data.burstTimings.size()) return;

        int timing = data.burstTimings.get(data.currentBurst);

        if (data.ticksLeft > timing || data.ticksLeft <= timing - 2) return;

        float damage = data.burstDamages.get(data.currentBurst);
        if (damage <= 0) {
            data.currentBurst++;
            return;
        }

        target.hurt(target.damageSources().lightningBolt(), damage);

        LivingEntityPatch<?> patch =
                EpicFightCapabilities.getEntityPatch(target, LivingEntityPatch.class);

        if (patch != null) {
            applyStunAndSound(patch, target, data, damage);
        }

        // more FX packet for visual flair (but not first burst)
        if (data.currentBurst > 0) {
            sendSpawnFxPacket(target);
        }

        data.currentBurst++;
    }

    private static void applyStunAndSound(
            LivingEntityPatch<?> patch,
            LivingEntity target,
            LightningEffectData data,
            float damage
    ) {
        StunType stunType;
        float strength;
        SoundEvent sound;
        float pitch;

        if (data.currentBurst == 0) {
            stunType = StunType.FALL;
            strength = Math.min(1.6f, damage * 0.15f);
            sound = SoundEvents.TRIDENT_THUNDER;
            pitch = 1.0f;
        } else if (data.currentBurst == data.burstTimings.size() - 1) {
            stunType = target.level().getRandom().nextBoolean()
                    ? StunType.LONG
                    : StunType.HOLD;
            strength = 0.8f;
            sound = EpicFightSounds.EVISCERATE.get();
            pitch = 1.3f;
        } else {
            stunType = StunType.SHORT;
            strength = Math.max(0.5f, damage * 0.08f);
            sound = target.level().getRandom().nextBoolean()
                    ? SoundEvents.LAVA_EXTINGUISH
                    : SoundEvents.FIRE_EXTINGUISH;
            pitch = 0.8f + target.level().getRandom().nextFloat() * 0.4f;
        }

        patch.applyStun(stunType, strength);
        patch.playSound(sound, 1.0f, pitch);
    }

    /* ---------------------------------------------------------------------- */

    public static void register() {
        MinecraftForge.EVENT_BUS.register(LightningBallHandler.class);
    }
}