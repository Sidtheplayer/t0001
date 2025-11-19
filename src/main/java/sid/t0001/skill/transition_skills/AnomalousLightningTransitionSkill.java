package sid.t0001.skill.transition_skills;

import com.lowdragmc.photon.client.fx.EntityEffect;
import com.lowdragmc.photon.client.fx.FXHelper;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraftforge.registries.ForgeRegistries;
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

/**
 * Lightning skill that follows Ohm's Law: V = I * R and current density principles
 * - Sweeping Edge increases Current (I) - amperage/duration
 * - Durability affects Resistance (R) via cross-sectional area
 *   * Damaged weapon = reduced cross-section = HIGHER resistance (R = ρL/A)
 * - Voltage (V) = final damage output
 *
 *
 */
public class AnomalousLightningTransitionSkill extends Skill {
    private static final UUID EVENT_UUID = UUID.fromString("607cb7a8-bb2c-4cc3-8839-993d34c584ae");
    private static final UUID FX_UUID = UUID.fromString("6048213c-0277-4fad-ba0c-7431c858ee24");
    private final Set<ResourceLocation> blacklistedItems = new HashSet<>();
    private final Map<UUID, Boolean> pendingLightning = new HashMap<>();

    // Track scheduled tasks per entity for proper cleanup
    private final Map<UUID, List<ScheduledLightningTask>> scheduledTasks = new HashMap<>();

    public AnomalousLightningTransitionSkill(Builder builder) {
        super(builder);
    }

    public static Builder createAnomalousLightningTransitionBuilder() {
        return (new Builder())
                .setCategory(t0001SkillCategories.INNER_TRANSITION)
                .setCreativeTab(t0001Tab.T0001_TAB.get())
                .setResource(Resource.NONE);
    }

    public static class Builder extends SkillBuilder<AnomalousLightningTransitionSkill> {
    }

    private static class ScheduledLightningTask {
        Timer timer;
        UUID targetUUID;

        ScheduledLightningTask(Timer timer, UUID targetUUID) {
            this.timer = timer;
            this.targetUUID = targetUUID;
        }

        void cancel() {
            if (timer != null) {
                timer.cancel();
            }
        }
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

            //Penality to prevent abuse for damaged weapons using this skill
            if (weapon.getDamageValue() > (weapon.getMaxDamage() * 0.75F)) {
                int extraDamage = getToDamageValue(weapon);
                weapon.setDamageValue(weapon.getDamageValue() + extraDamage);
            }



            float baseAmperage = 20.0f + (sweepingLevel * 20.0f);

            float current = Math.min(100.0f, baseAmperage);


            float voltage = current * resistance;


            int durationTicks = Math.round(current);


            float totalDamage =  voltage / 200.0f; //removed clamp

            event.getPlayerPatch().playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, -1F, 0.25F);

            int baseDelay = 4;
            int increment = 4;

            List<ScheduledLightningTask> playerTasks = new ArrayList<>();

            for (int i = 0; i < hurtEntities.size(); i++) {
                LivingEntity target = hurtEntities.get(i);
                if (target == null || !target.isAlive()) continue;

                final int delayTicks = baseDelay + (i * increment);
                final int finalDuration = durationTicks;
                final float finalDamage = totalDamage;
                final LivingEntity finalTarget = target;

                Timer timer = new Timer();
                ScheduledLightningTask task = new ScheduledLightningTask(timer, target.getUUID());
                playerTasks.add(task);

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (finalTarget.level().getServer() != null) {
                            Objects.requireNonNull(finalTarget.level().getServer()).execute(() ->
                                    applyLightningEffect(finalTarget, finalDuration, finalDamage)
                            );
                        }
                    }
                }, delayTicks * 50L );

                if (!container.getExecutor().isLogicalClient() && target instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    EpicFightNetworkManager.sendToPlayer(SPSkillExecutionFeedback.executed(container.getSlotId()), serverPlayer);

                    SPEntityPairingPacket pairingPacket = new SPEntityPairingPacket(target.getId(), EntityPairingPacketTypes.FLASH_WHITE);
                    pairingPacket.getBuffer().writeInt(4);
                    pairingPacket.getBuffer().writeInt(20);
                    pairingPacket.getBuffer().writeInt(10);
                    pairingPacket.getBuffer().writeBoolean(false);

                    EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(pairingPacket, serverPlayer);
                }
            }

            if (!playerTasks.isEmpty()) {
                scheduledTasks.put(playerUUID, playerTasks);
            }

            event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
        });
    }

    private static int getToDamageValue(ItemStack weapon) {
        float brokenRatio = (float) weapon.getDamageValue() / weapon.getMaxDamage();
        float normalized = (brokenRatio - 0.75F) / 0.25F;
        float smooth = (float)(
                Math.pow(normalized, 1.5F) * 0.35F +
                        Math.pow(normalized, 3.0F) * 0.65F
        );

        return (int)Math.ceil(smooth * 10);
    }

    private static float getResistance(ItemStack weapon) {
        int maxDamage = Math.max(1, weapon.getMaxDamage());
        int currentDamage = Math.min(weapon.getDamageValue(), maxDamage - 1);


        float durabilityRatio = ((float) maxDamage - currentDamage) / maxDamage;


        return 10.0f + ((1.0f - durabilityRatio) * 40.0f);
    }

    private void applyLightningEffect(LivingEntity target, int durationTicks, float totalDamage) {
        if (!target.isAlive()) return;

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

        target.playSound(SoundEvents.TRIDENT_THUNDER);

        if (!target.level().isClientSide()) {
            // Slowness duration matches lightning duration
            target.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    durationTicks,
                    2,
                    false,
                    false,
                    false
            ));


            int amperageParam = Math.max(1, durationTicks / 24);

            LightningBallHandler.addLightningTarget(
                    target,
                    amperageParam,
                    (int) totalDamage,
                    lightningBall.getRuntime()
            );
        }
    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
        PlayerEventListener listener = container.getExecutor().getEventListener();
        listener.removeListener(PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_DAMAGE, EVENT_UUID);
        listener.removeListener(PlayerEventListener.EventType.ATTACK_ANIMATION_END_EVENT, FX_UUID);

        // Cancel all scheduled tasks
        for (List<ScheduledLightningTask> tasks : scheduledTasks.values()) {
            for (ScheduledLightningTask task : tasks) {
                task.cancel();
            }
        }
        scheduledTasks.clear();
        pendingLightning.clear();
    }
}