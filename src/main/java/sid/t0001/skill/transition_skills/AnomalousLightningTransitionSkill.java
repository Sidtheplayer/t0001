package sid.t0001.skill.transition_skills;

import com.lowdragmc.photon.client.fx.EntityEffect;
import com.lowdragmc.photon.client.fx.FXHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import sid.t0001.events.LightningBallHandler;
import sid.t0001.skill.t0001SkillCategories;
import sid.t0001.world.item.t0001Tab;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.*;

public class AnomalousLightningTransitionSkill extends Skill {
    private static final UUID EVENT_UUID = UUID.fromString("607cb7a8-bb2c-4cc3-8839-993d34c584ae");
    private final Set<ResourceLocation> blacklistedItems = new HashSet<>();

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

        listener.addEventListener(PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_HURT, EVENT_UUID, (event) -> {
            if (!event.getDamageSource().is(EpicFightDamageTypeTags.WEAPON_INNATE)) {
                return;
            }

            // Copy target list before EpicFight clears it
            List<LivingEntity> hurtEntities = new ArrayList<>(event.getPlayerPatch().getCurrentlyActuallyHitEntities());

            // fallback if list empty
            if (hurtEntities.isEmpty() && event.getTarget() != null && event.getTarget().isAlive()) {
                hurtEntities.add(event.getTarget());
            }

            if (hurtEntities.isEmpty()) {
                return;
            }

            ItemStack weapon = event.getPlayerPatch().getOriginal().getMainHandItem();
            ResourceLocation itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(weapon.getItem());

            // Skip blacklisted items
            if (itemId != null && blacklistedItems.contains(itemId)) {
                event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
                return;
            }

            boolean blacklistTier = false;
            if (weapon.getItem() instanceof TieredItem tieredItem) {
                blacklistTier = tieredItem.getTier() == Tiers.WOOD || tieredItem.getTier() == Tiers.STONE;
            }

            int sweepingLevel = weapon.getEnchantmentLevel(Enchantments.SWEEPING_EDGE);
            int maxDamage = Math.max(1, weapon.getMaxDamage());
            int currentDamage = Math.min(weapon.getDamageValue(), maxDamage - 1);

            float resistance = Math.max(0.25f, ((float) maxDamage - currentDamage) / maxDamage);
            int base = Math.round(Math.min((sweepingLevel * resistance * 30.0f) + 3, 50));
            int selectiveAmperage = Math.max(3, (int) Math.pow(base, 0.75));

            if (blacklistTier) {
                selectiveAmperage = 0;
            }

            float sweepDamageScale = 0.5f + (sweepingLevel * 0.15f);
            float resistanceFactor = 0.5f + (resistance * 0.5f);
            float selectiveDamageAmp = Math.min(2.0f, sweepDamageScale * resistanceFactor);

            // delay config
            int delayPerTarget = 60; // 3s total if 3 targets (20 ticks/sec)
            int firstTargetDelay = 1;

            // looping through targets
            for (int i = 0; i < hurtEntities.size(); i++) {
                LivingEntity target = hurtEntities.get(i);
                if (target == null || !target.isAlive()) continue;

                int delayTicks = i == 0 ? firstTargetDelay : (i * delayPerTarget);

                EntityEffect fx = new EntityEffect(
                        FXHelper.getFX(ResourceLocation.parse("photon:yellow_lightning_ball")),
                        target.level(),
                        target,
                        EntityEffect.AutoRotate.NONE
                );
                fx.setOffset(0, 0, 0);
                fx.setScale(1, 1, 1);
                fx.setAllowMulti(false);
                fx.setForcedDeath(true);
                fx.setDelay(delayTicks);
                fx.start();

                if (!target.level().isClientSide() && target.level() instanceof ServerLevel srv) {
                    final int amperage = selectiveAmperage;
                    final float dmgAmp = selectiveDamageAmp;
                    final var runtime = fx.getRuntime();

                    srv.getServer().tell(new net.minecraft.server.TickTask(
                            srv.getServer().getTickCount() + delayTicks,
                            () -> applyEffects(target, amperage, dmgAmp, runtime)
                    ));
                }
            }

            // Delay clearing by small buffer to avoid concurrent modification (fix for first entity not getting the fx)
            if (!hurtEntities.isEmpty() && !hurtEntities.get(0).level().isClientSide() &&
                    hurtEntities.get(0).level() instanceof ServerLevel srv) {
                srv.getServer().tell(new net.minecraft.server.TickTask(
                        srv.getServer().getTickCount() + 10,
                        () -> event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear()
                ));
            }
        });
    }

    private void applyEffects(LivingEntity target, int amperage, float damageAmp, com.lowdragmc.photon.client.fx.FXRuntime runtime) {
        if (target.isAlive()) {
            target.playSound(SoundEvents.TRIDENT_THUNDER);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, amperage * 18, 2, false, false, false));
            LightningBallHandler.addLightningTarget(target, amperage, (int) (damageAmp * 2.0f), runtime);
        }
    }

//    @Override to add passive sparks to player soon
//    public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
//        super.executeOnServer(container, args);
//    }



    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
        container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_HURT, EVENT_UUID);
    }
}
