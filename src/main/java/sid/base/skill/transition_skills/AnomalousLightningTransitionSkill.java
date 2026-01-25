package sid.base.skill.transition_skills;


import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.photon.client.fx.EntityEffectExecutor;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import sid.base.skill.t0001SkillCategories;
import sid.base.skill.t0001SkillDataKeys;
import sid.base.utils.VFX_Ids;
import sid.base.utils.ldlib2_utils.widgetstuff.UltimateMeterWidget;
import sid.base.world.item.t0001Tab;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.IdentifierProvider;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;

import java.util.*;
import java.util.function.Function;

/*
 * Lightning skill that follows Ohm's Law: V = I * R and current density principles
 * - Sweeping Edge increases Current (I) - amperage/duration
 * - Durability affects Resistance (R) via cross-sectional area
 *   * Damaged weapon = reduced cross-section = HIGHER resistance (R = ρL/A)
 * - Voltage (V) = final damage output
 */
public class AnomalousLightningTransitionSkill extends Skill {
    public static final String LightningFXPacketID = "AnomalousTransitionSkillLightningFXPacket";
    private static final IdentifierProvider FX_ID = IdentifierProvider.constant("6048213c-0277-4fad-ba0c-7431c858ee24");
    private final Set<ResourceLocation> blacklistedItems = new HashSet<>();

    ModularUI cachedUI;
    int MAX_ULTIMATE_METER = 100;


    public AnomalousLightningTransitionSkill(SkillBuilder builder) {
        super(builder);
    }

    public static Builder createAnomalousLightningSkillBuilder() {
        return new Builder(AnomalousLightningTransitionSkill::new)
                .setCreativeTab(t0001Tab.T0001_TAB.get())
                .setCategory(t0001SkillCategories.INNER_TRANSITION);
    }

    public static class Builder extends SkillBuilder<Builder> {
        public Builder(Function<Builder, ? extends Skill> constructor) {
            super(constructor);
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
                EpicFightEventHooks.Entity.DELIVER_DAMAGE_POST,
                (event) -> {
                    if (event.getDamageSource().is(EpicFightDamageTypeTags.WEAPON_INNATE)) {

                        ItemStack weapon = event.getEntityPatch().getOriginal().getMainHandItem();
                        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(weapon.getItem());

                        if (blacklistedItems.contains(itemId)) return;
                        if (weapon.getItem() instanceof TieredItem tieredItem) {
                            if (tieredItem.getTier() == Tiers.WOOD || tieredItem.getTier() == Tiers.STONE)
                                return;
                        }

                        if (event.getDamageSource().getAnimation().checkType(AttackAnimation.class)) {
                            container.getDataManager().setDataSync(t0001SkillDataKeys.ACTIVATION_KEY, true);
                        }


                    }


                }, FX_ID, 1
        );

        eventListener.registerEvent(
                EpicFightEventHooks.Animation.END,
                event -> {

                    boolean activatetheskill = container.getDataManager().getDataValue(t0001SkillDataKeys.ACTIVATION_KEY);

                    if (activatetheskill) {
                        LivingEntity e = event.getEntityPatch().getOriginal();
                        ItemStack weapon = event.getEntityPatch().getOriginal().getMainHandItem();

                        List<LivingEntity> hurtEntities = event.getEntityPatch().getCurrentlyActuallyHitEntities();
                        if (hurtEntities.isEmpty()) {
                            System.out.println("HURT ENTITY LIST EMPTY");
                            container.getDataManager().setData(t0001SkillDataKeys.ACTIVATION_KEY, false);
                            return;
                        }


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

                        // process server side
                        boolean isServerSide = !container.getExecutor().isLogicalClient();
                        ServerLevel serverLevel = isServerSide ? (ServerLevel) event.getEntityPatch().getOriginal().level() : null;
                        MinecraftServer server;
                        if (serverLevel != null) {
                            server = serverLevel.getServer();
                        } else {
                            server = null;
                        }


                        for (int i = 0; i < hurtEntities.size(); i++) {
                            LivingEntity victim = hurtEntities.get(i);
                            if (victim == null || !victim.isAlive()) continue;

                            int delayTicks = baseDelay + (i * increment);

                            if (serverLevel != null) {
                                serverLevel.getServer().tell(
                                        new TickTask(
                                                server.getTickCount() + delayTicks,
                                                () -> applyLightningEffectStatic(victim, durationTicks, totalDamage)
                                        )
                                );
                            }

                            //Sen packets for fx (server-side)
                            if (victim instanceof ServerPlayer serverPlayer) {

                                SPEntityPairingPacket pairingPacket = new SPEntityPairingPacket(victim.getId(), EntityPairingPacketTypes.FLASH_WHITE);
                                pairingPacket.buffer().writeInt(4);
                                pairingPacket.buffer().writeInt(20);
                                pairingPacket.buffer().writeInt(10);
                                pairingPacket.buffer().writeBoolean(false);

                                EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(pairingPacket, serverPlayer);
                            }
                        }
                        container.getDataManager().setDataSync(t0001SkillDataKeys.ACTIVATION_KEY, false);
                    }
                }, FX_ID, 2
        );

    }


    private static float getResistance(ItemStack weapon) {
        int maxDamage = Math.max(1, weapon.getMaxDamage());
        int currentDamage = Math.min(weapon.getDamageValue(), maxDamage - 1);
        float durabilityRatio = ((float) maxDamage - currentDamage) / maxDamage;
        return 10.0f + (durabilityRatio * 40.0f);
    }


    public static void applyLightningEffectStatic(LivingEntity target, int durationTicks, float totalDamage) {
        if (!target.isAlive()) return;

        // Server logic
        if (target.level() instanceof ServerLevel serverLevel) {
            target.playSound(SoundEvents.TRIDENT_THUNDER.value());

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

            target.hurt(target.damageSources().lightningBolt(), totalDamage * amperageParam);

            RPCPacketDistributor.rpcToTracking(serverLevel, target.chunkPosition(), VFX_Ids.WHITE_LIGHTNING.id, target.getId());

        }
    }

    @RPCPacket(LightningFXPacketID)
    public static void SendLightningFXPacket(Integer entityID) {
        FX fx = FXHelper.getFX(ResourceLocation.parse("photon:white_lightning_ball"));
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (fx != null && entityID != null && level != null) {
            Entity Entity = level.getEntity(entityID);
            if (Entity != null) {
                EntityEffectExecutor lightning = new EntityEffectExecutor(fx, Entity.level(), Entity, EntityEffectExecutor.AutoRotate.NONE);
                lightning.setOffset(0.0D, -0.65D, 0.0D);
                lightning.setRotation(0, 0, 0);
                lightning.setScale(1, 1, 1);
                lightning.setDelay(0);
                lightning.setForcedDeath(false);
                lightning.setAllowMulti(true);
                lightning.start();
            }
            System.out.println("RECEIVED PACKET FROM SERVER first");
        }

    }


    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);

        var listener = container.getExecutor().getEventListener();

        listener.removeListenersBelongTo(this);
        listener.removeListenersBelongTo(FX_ID);
    }


    @Override
    public boolean shouldDraw(SkillContainer container) {
        return false;
    }

    @Override
    public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {

        if (cachedUI == null) {
            var root = new UIElement();
            root.layout(layout -> layout.width(150).height(20));

            var ultimateMeter = new UltimateMeterWidget(
                    // Progress supplier (0.0 to 1.0)
                    () -> {
                        int currentMeter = container.getDataManager().getDataValue(t0001SkillDataKeys.ULTIMATE_METER);
                        return (float) currentMeter / MAX_ULTIMATE_METER;
                    },
                    // Is ultimate active? (hides bar when true)
                    () -> container.getDataManager().getDataValue(t0001SkillDataKeys.IS_AWAKENED),
                    // On trigger callback (player clicks when full)
                    (widget) -> {
                        // Send activation request to server
                        if (container.sendCastRequest(
                                container.getClientExecutor(),
                                ControlEngine.getInstance()
                        ).isExecutable()) {
                            // Activate ultimate on client side
                            container.getDataManager().setDataSync(t0001SkillDataKeys.IS_AWAKENED, true);
                        }
                    },
                    150, // width
                    20   // height
            );

            root.addChild(ultimateMeter);

            var ui = UI.of(root);
            cachedUI = ModularUI.of(ui);
            cachedUI.init(150, 20);
        }


        float centerX = 0;
        float baseY = 0; // above XP bar
        if (Minecraft.getInstance().screen != null && !Minecraft.getInstance().screen.isPauseScreen()) {
            centerX = Minecraft.getInstance().screen.width / 2f;
            baseY = Minecraft.getInstance().screen.height - 49;
        }

        float xx = centerX - 75;
        float yy = baseY - 12;

        // Render
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(xx, yy, 0);
        cachedUI.getWidget().render(guiGraphics, 0, 0, partialTick);
        poseStack.popPose();

    }
}