package sid.t0001.skill.transition_skills;

import com.lowdragmc.photon.client.fx.EntityEffect;
import com.lowdragmc.photon.client.fx.FXHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraftforge.registries.ForgeRegistries;
import sid.t0001.client.LightningBallClientHandler;
import sid.t0001.events.LightningBallHandler;

import sid.t0001.skill.t0001SkillCategories;
import sid.t0001.world.item.t0001Tab;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.network.server.SPSkillExecutionFeedback;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightning skill that follows Ohm's Law: V = I * R and current density principles
 * - Sweeping Edge increases Current (I) - amperage/duration
 * - Durability affects Resistance (R) via cross-sectional area
 *   * Damaged weapon = reduced cross-section = HIGHER resistance (R = ρL/A)
 * - Voltage (V) = final damage output
 */
public class AnomalousLightningTransitionSkill extends Skill {
    private static final UUID EVENT_UUID = UUID.fromString("607cb7a8-bb2c-4cc3-8839-993d34c584ae");
    private static final UUID FX_UUID = UUID.fromString("6048213c-0277-4fad-ba0c-7431c858ee24");
    private final Set<ResourceLocation> blacklistedItems = new HashSet<>();
    private final Map<UUID, Boolean> pendingLightning = new HashMap<>();

    // Static tick handler shared across all instances
    private static final Map<UUID, List<ScheduledLightningData>> PENDING_EFFECTS = new ConcurrentHashMap<>();
    private static boolean tickHandlerRegistered = false;

    private static class ScheduledLightningData {
        UUID targetUUID;
        int delayTicks;
        int duration;
        float damage;
        ServerLevel level;

        ScheduledLightningData(UUID targetUUID, int delayTicks, int duration, float damage, ServerLevel level) {
            this.targetUUID = targetUUID;
            this.delayTicks = delayTicks;
            this.duration = duration;
            this.damage = damage;
            this.level = level;
        }
    }

    public AnomalousLightningTransitionSkill(Builder builder) {
        super(builder);
        registerTickHandler();
    }

    private static synchronized void registerTickHandler() {
        if (!tickHandlerRegistered) {
            MinecraftForge.EVENT_BUS.register(TickHandler.class);
            tickHandlerRegistered = true;
        }
    }

    public static class TickHandler {
        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

            Iterator<Map.Entry<UUID, List<ScheduledLightningData>>> playerIterator = PENDING_EFFECTS.entrySet().iterator();

            while (playerIterator.hasNext()) {
                Map.Entry<UUID, List<ScheduledLightningData>> entry = playerIterator.next();
                List<ScheduledLightningData> scheduled = entry.getValue();

                if (scheduled == null || scheduled.isEmpty()) {
                    playerIterator.remove();
                    continue;
                }

                Iterator<ScheduledLightningData> dataIterator = scheduled.iterator();

                while (dataIterator.hasNext()) {
                    ScheduledLightningData data = dataIterator.next();
                    data.delayTicks--;

                    if (data.delayTicks <= 0) {
                        // Find the target entity
                        LivingEntity target = null;
                        if (data.level != null) {
                            for (net.minecraft.world.entity.Entity entity : data.level.getAllEntities()) {
                                if (entity.getUUID().equals(data.targetUUID) && entity instanceof LivingEntity living) {
                                    target = living;
                                    break;
                                }
                            }
                        }

                        if (target != null && target.isAlive()) {
                            applyLightningEffectStatic(target, data.duration, data.damage);
                        }

                        dataIterator.remove();
                    }
                }

                if (scheduled.isEmpty()) {
                    playerIterator.remove();
                }
            }
        }
    }

    public static Builder createAnomalousLightningTransitionBuilder() {
        return (new Builder())
                .setCategory(t0001SkillCategories.INNER_TRANSITION)
                .setCreativeTab(t0001Tab.T0001_TAB.get())
                .setResource(Resource.NONE);
    }

    public static class Builder extends SkillBuilder<AnomalousLightningTransitionSkill> {
    }

    @Override
    public void setParams(CompoundTag parameters) {
        super.setParams(parameters);
        blacklistedItems.clear();

        if (parameters != null && parameters.contains("blacklisted_items", Tag.TAG_LIST)) {
            ListTag list = parameters.getList("blacklisted_items", Tag.TAG_STRING);
            for (Tag t : list) {
                String s = t.getAsString();
                if (!s.isEmpty()) {
                    blacklistedItems.add(ResourceLocation.tryParse(s));
                }
            }
        }
    }

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);
        PlayerEventListener listener = container.getExecutor().getEventListener();

        listener.addEventListener(PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_DAMAGE, EVENT_UUID, (event) -> {
            if (event.getDamageSource().is(EpicFightDamageTypeTags.WEAPON_INNATE)) {
                UUID playerUUID = event.getPlayerPatch().getOriginal().getUUID();

                ItemStack weapon = event.getPlayerPatch().getOriginal().getMainHandItem();
                ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(weapon.getItem());

                if (itemId != null && blacklistedItems.contains(itemId)) return;
                if (weapon.getItem() instanceof TieredItem tieredItem) {
                    if (tieredItem.getTier() == Tiers.WOOD || tieredItem.getTier() == Tiers.STONE) return;
                }

                pendingLightning.put(playerUUID, true);
            }
        });

        listener.addEventListener(PlayerEventListener.EventType.ATTACK_ANIMATION_END_EVENT, FX_UUID, (event) -> {
            UUID playerUUID = event.getPlayerPatch().getOriginal().getUUID();

            if (!pendingLightning.getOrDefault(playerUUID, false)) {
                event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
                return;
            }

            pendingLightning.remove(playerUUID);

            List<LivingEntity> hurtEntities = event.getPlayerPatch().getCurrentlyActuallyHitEntities();
            if (hurtEntities.isEmpty()) return;

            ItemStack weapon = event.getPlayerPatch().getOriginal().getMainHandItem();

            int sweepingLevel = weapon.getEnchantmentLevel(Enchantments.SWEEPING_EDGE);
            float resistance = getResistance(weapon);

            float baseAmperage = 20.0f + (sweepingLevel * 20.0f);
            float current = Math.min(100.0f, baseAmperage);
            float voltage = current * resistance;
            int durationTicks = Math.round(current);
            float totalDamage = voltage / 200.0f;

            event.getPlayerPatch().playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, -1F, 0.25F);

            int baseDelay = 4;
            int increment = 4;

            List<ScheduledLightningData> playerScheduledData = new ArrayList<>();

            // Only process on server side
            boolean isServerSide = !container.getExecutor().isLogicalClient();
            ServerLevel serverLevel = isServerSide ? (ServerLevel) event.getPlayerPatch().getOriginal().level() : null;

            for (int i = 0; i < hurtEntities.size(); i++) {
                LivingEntity target = hurtEntities.get(i);
                if (target == null || !target.isAlive()) continue;

                final int delayTicks = baseDelay + (i * increment);

                if (isServerSide) {
                    ScheduledLightningData schedData = new ScheduledLightningData(
                            target.getUUID(),
                            delayTicks,
                            durationTicks,
                            totalDamage,
                            serverLevel
                    );
                    playerScheduledData.add(schedData);
                }

                // Send network packets for visual feedback (server-side only)
                if (isServerSide && target instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    EpicFightNetworkManager.sendToPlayer(SPSkillExecutionFeedback.executed(container.getSlotId()), serverPlayer);

                    SPEntityPairingPacket pairingPacket = new SPEntityPairingPacket(target.getId(), EntityPairingPacketTypes.FLASH_WHITE);
                    pairingPacket.getBuffer().writeInt(4);
                    pairingPacket.getBuffer().writeInt(20);
                    pairingPacket.getBuffer().writeInt(10);
                    pairingPacket.getBuffer().writeBoolean(false);

                    EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(pairingPacket, serverPlayer);
                }
            }

            if (!playerScheduledData.isEmpty()) {
                PENDING_EFFECTS.put(playerUUID, playerScheduledData);
            }

            event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
        });
    }


    private static float getResistance(ItemStack weapon) {
        int maxDamage = Math.max(1, weapon.getMaxDamage());
        int currentDamage = Math.min(weapon.getDamageValue(), maxDamage - 1);
        float durabilityRatio = ((float) maxDamage - currentDamage) / maxDamage;
        return 10.0f + (durabilityRatio * 40.0f);
    }

    private static void applyLightningEffectStatic(LivingEntity target, int durationTicks, float totalDamage) {
        if (!target.isAlive()) return;


        if (!target.level().isClientSide()) {
            target.playSound(SoundEvents.TRIDENT_THUNDER);

            target.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    durationTicks,
                    2,
                    false,
                    false,
                    false
            ));

            int amperageParam = Math.max(1, durationTicks / 24);

            // Add lightning effect (server-side tracking)
            LightningBallHandler.addLightningTarget(
                    target,
                    amperageParam,
                    (int) totalDamage,
                    LightningBallHandler.StackMode.EXTEND
            );

        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void createClientSideFX(int entityId) {
        // Called from packet handler on client side
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null) return;

        net.minecraft.world.entity.Entity entity = mc.level.getEntity(entityId);
        if (!(entity instanceof LivingEntity target)) return;

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
        lightningBall.setForcedDeath(true);
        lightningBall.start();

        // Register FX runtime with the client-side handler
        LightningBallClientHandler.setClientFXRuntime(target, lightningBall.getRuntime());
    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
        PlayerEventListener listener = container.getExecutor().getEventListener();
        listener.removeListener(PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_DAMAGE, EVENT_UUID);
        listener.removeListener(PlayerEventListener.EventType.ATTACK_ANIMATION_END_EVENT, FX_UUID);

        // Clear all pending effects for this player
        UUID playerUUID = container.getExecutor().getOriginal().getUUID();
        PENDING_EFFECTS.remove(playerUUID);
        pendingLightning.remove(playerUUID);
    }
}

