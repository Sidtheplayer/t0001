package sid.base.skill.transition_skills;


import net.minecraft.core.registries.BuiltInRegistries;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import sid.base.events.LightningBallHandler;
import sid.base.network.SpawnLightningFxPacket;
import sid.base.skill.t0001SkillCategories;
import sid.base.world.item.t0001Tab;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.IdentifierProvider;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/*
 * Lightning skill that follows Ohm's Law: V = I * R and current density principles
 * - Sweeping Edge increases Current (I) - amperage/duration
 * - Durability affects Resistance (R) via cross-sectional area
 *   * Damaged weapon = reduced cross-section = HIGHER resistance (R = ρL/A)
 * - Voltage (V) = final damage output
 */
public class AnomalousLightningTransitionSkill extends Skill {
    private static final IdentifierProvider FX_ID = IdentifierProvider.constant("6048213c-0277-4fad-ba0c-7431c858ee24");
    private final Set<ResourceLocation> blacklistedItems = new HashSet<>();
    private final Set<UUID> pendingLightning = ConcurrentHashMap.newKeySet();

    // Static tick handler shared across all instances
    private static final Map<UUID, List<ScheduledLightningData>> PENDING_EFFECTS = new ConcurrentHashMap<>();
    private static boolean tickHandlerRegistered = false;

    public AnomalousLightningTransitionSkill(SkillBuilder builder) {
        super(builder);
        registerTickHandler();
    }

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

    public static AnomalousLightningTransitionSkill.Builder createAnomalousLightningSkillBuilder(){
        return new Builder(AnomalousLightningTransitionSkill::new)
                .setCreativeTab(t0001Tab.T0001_TAB.get())
                .setCategory(t0001SkillCategories.INNER_TRANSITION);
    }

    public static class Builder extends SkillBuilder<AnomalousLightningTransitionSkill.Builder> {
        public Builder(Function<Builder, ? extends Skill> constructor) {
            super(constructor);
        }
    }


    private static synchronized void registerTickHandler() {
        if (!tickHandlerRegistered) {
            NeoForge.EVENT_BUS.register(TickHandler.class);
            tickHandlerRegistered = true;
        }
    }

    public static class TickHandler {

        @SubscribeEvent
        public static void onServerTick(ServerTickEvent.Post event) {
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



    @Override
    public void loadDatapackParameters(CompoundTag parameters) {
        super.loadDatapackParameters(parameters);
        blacklistedItems.clear();
        //FIXME:BEFORE RELEASE REMEMBER TO REMOVE IRON_TACHI FROM Skill parameter json
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
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);

        eventListener.registerEvent(
                EpicFightEventHooks.Entity.DELIVER_DAMAGE_INCOME,
                (event) -> {
                    if (event.getDamageSource().is(EpicFightDamageTypeTags.WEAPON_INNATE)) {
                        UUID playerUUID = event.getEntityPatch().getOriginal().getUUID();

                        ItemStack weapon = event.getEntityPatch().getOriginal().getMainHandItem();
                        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(weapon.getItem());

                        if (blacklistedItems.contains(itemId)) return;
                        if (weapon.getItem() instanceof TieredItem tieredItem) {
                            if (tieredItem.getTier() == Tiers.WOOD || tieredItem.getTier() == Tiers.STONE) return;
                        }

                        pendingLightning.add(playerUUID);
                    }



                },this
        );

         //TODO: REWRITE THE LOGIC USING DELIVER_DAMAGE_POST INSTEAD
        eventListener.registerEvent(
                EpicFightEventHooks.Animation.END,
                (event) -> {
                    if (event.getAnimation() instanceof AttackAnimation) {

                        UUID playerUUID = event.getEntityPatch().getOriginal().getUUID();
                        LivingEntity e = event.getEntityPatch().getOriginal();

                        pendingLightning.remove(playerUUID);

                        List<LivingEntity> hurtEntities = event.getEntityPatch().getCurrentlyActuallyHitEntities();
                        if (hurtEntities.isEmpty()) return;

                        ItemStack weapon = event.getEntityPatch().getOriginal().getMainHandItem();

                        int sweepingLevel = weapon.getEnchantmentLevel(e.level().holderOrThrow(Enchantments.SWEEPING_EDGE));
                        float resistance = getResistance(weapon);


                        float current = 20.0f + (sweepingLevel * 20.0f);
                        float voltage = current * resistance;  // V = I * R basically lmao.
                        int durationTicks = Math.round(current);
                        float totalDamage = voltage / 200.0f;

                        event.getEntityPatch().playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, -1F, 0.25F);

                        int baseDelay = 4;
                        int increment = 4;

                        // clearly I am not good with math, and that is exactly why I am going to study high school math to college calculus by 2026 february
                        //oh my god I didn't learn shit and its already jan 21

                        List<ScheduledLightningData> playerScheduledData = new ArrayList<>();

                        // process server side
                        boolean isServerSide = !container.getExecutor().isLogicalClient();
                        ServerLevel serverLevel = isServerSide ? (ServerLevel) event.getEntityPatch().getOriginal().level() : null;

                        for (int i = 0; i < hurtEntities.size(); i++) {
                            LivingEntity target = hurtEntities.get(i);
                            if (target == null || !target.isAlive()) continue;

                            final int delayTicks = baseDelay + (i * increment);

                            // Store data for server-side tick processing
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

                       //      Sen packets for fx (server-side)
                            if (isServerSide && target instanceof ServerPlayer serverPlayer) {

                                SPEntityPairingPacket pairingPacket = new SPEntityPairingPacket(target.getId(), EntityPairingPacketTypes.FLASH_WHITE);
                                pairingPacket.buffer().writeInt(4);
                                pairingPacket.buffer().writeInt(20);
                                pairingPacket.buffer().writeInt(10);
                                pairingPacket.buffer().writeBoolean(false);

                                EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(pairingPacket, serverPlayer);
                            }
                        }

                        if (!playerScheduledData.isEmpty()) {
                            PENDING_EFFECTS.computeIfAbsent(playerUUID,k->
                                    new ArrayList<>()).addAll(playerScheduledData);
                        }
                    }


                }, FX_ID
        );

    }


    private static float getResistance(ItemStack weapon) {
        int maxDamage = Math.max(1, weapon.getMaxDamage());
        int currentDamage = Math.min(weapon.getDamageValue(), maxDamage - 1);
        float durabilityRatio = ((float) maxDamage - currentDamage) / maxDamage;
        return 10.0f + (durabilityRatio * 40.0f);
    }

    private static void applyLightningEffectStatic(LivingEntity target, int durationTicks, float totalDamage) {
        if (!target.isAlive()) return;

        // Server logic
        if (!target.level().isClientSide()) {
            target.playSound(SoundEvents.TRIDENT_THUNDER.value()    );

            // Slowness dur = lightning dur
            target.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    durationTicks,
                    2,
                    false,
                    false,
                    false
            ));

            int amperageParam = Math.max(1, durationTicks / 24);

            //  lightning effect (server-side track)
            LightningBallHandler.addLightningTarget(
                    target,
                    amperageParam,
                    totalDamage,
                    LightningBallHandler.StackMode.EXTEND
            );

            // Send packet to all clients tracking this entity to spawn FX
            if (target.level() instanceof ServerLevel) {
                SpawnLightningFxPacket packet = new SpawnLightningFxPacket(target.getId());
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(target, packet);
            }
        }
    }


    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);

        var listener =  container.getExecutor().getEventListener();

        listener.removeListenersBelongTo(this);
        listener.removeListenersBelongTo(FX_ID);

        // Clear all pending effects for this player
        UUID playerUUID = container.getExecutor().getOriginal().getUUID();
        PENDING_EFFECTS.remove(playerUUID);
        pendingLightning.remove(playerUUID);
    }
}