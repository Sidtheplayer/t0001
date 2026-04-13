package sid.base.skill.transition_skills;


import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.photon.client.fx.EntityEffectExecutor;
import com.lowdragmc.photon.client.fx.FX;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.joml.Vector3f;
import sid.base.events.global_events.GlobalEventHandlers;
import sid.base.main.t0001;
import sid.base.skill.t0001SkillCategories;
import sid.base.skill.t0001SkillDataKeys;
import sid.base.network.PacketDelegations;
import sid.base.utils.RpcPacketIds;
import sid.base.world.item.t0001Tab;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.IdentifierProvider;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;

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

    //not necessary to use a custom IdentifierProvider, but it might be uninterruptible by other skills, etc. (this is just my hunch)
    private static final IdentifierProvider FX_ID = IdentifierProvider.constant("6048213c-0277-4fad-ba0c-7431c858ee24");
    public static final String LightningFXPacketID = "anomalousfxpacket";
    private final Set<ResourceLocation> blacklistedItems = new HashSet<>();

    boolean enableDelay;
    int baseDelay;
    int delayIncrement;

    // Keep ModularUI to use the widget render method; root element set to ABSOLUTE so positioning works
//    ModularUI cachedUI;
//    int MAX_ULTIMATE_METER = 100;


    public AnomalousLightningTransitionSkill(SkillBuilder builder) {
        super(builder);
    }

    public static Builder createAnomalousLightningSkillBuilder() {
        return new Builder(AnomalousLightningTransitionSkill::new)
                .setCreativeTab(t0001Tab.T0001_TAB.get())
                .setResource(Resource.COOLDOWN)
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
        if (parameters != null && parameters.contains("blacklisted_items", Tag.TAG_LIST)) {
            ListTag list = parameters.getList("blacklisted_items", Tag.TAG_STRING);
            for (Tag t : list) {
                String s = t.getAsString();
                if (!s.isEmpty()) {
                    blacklistedItems.add(ResourceLocation.tryParse(s));
                }
            }
        }
        if (parameters != null) {
            enableDelay = parameters.getBoolean("enable_delay");
            baseDelay = parameters.getInt("base_delay");
            delayIncrement = parameters.getInt("delay_increment");
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
                        //blacklist tiers and items
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

                    boolean SkillActivationKey = container.getDataManager().getDataValue(t0001SkillDataKeys.ACTIVATION_KEY);

                    if (SkillActivationKey) {
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

                        int baseDelay = this.baseDelay;
                        int increment = this.delayIncrement;

                        // clearly I am not good with math, and that is exactly why I am going to study high school math to college calculus by 2026 february
                        //oh my god I didn't learn shit -- 3 months later

                        // process server side
                        boolean isServerSide = !container.getExecutor().isLogicalClient();
                        if(!isServerSide)return;
                        ServerLevel serverLevel = (ServerLevel) event.getEntityPatch().getOriginal().level();
                        MinecraftServer server = serverLevel.getServer();

                        for (int i = 0; i < hurtEntities.size(); i++) {
                            LivingEntity victim = hurtEntities.get(i);
                            if (victim == null || !victim.isAlive()) continue;

                            int delayTicks = enableDelay
                                    ?  baseDelay + (i * increment)
                                    : 0;


                            GlobalEventHandlers.DelayedTaskScheduler.schedule(server, delayTicks,
                                    () -> {
                                        if (victim.isAlive())
                                            applyLightningEffectStatic(victim, durationTicks, totalDamage);
                                    });


                            if (victim instanceof ServerPlayer serverPlayer) {
                                GlobalEventHandlers.DelayedTaskScheduler.schedule(server, delayTicks,
                                        () -> {
                                            if (!serverPlayer.isAlive()) return;

                                            SPEntityPairingPacket pairingPacket =
                                                    new SPEntityPairingPacket(victim.getId(), EntityPairingPacketTypes.FLASH_WHITE);
                                            pairingPacket.buffer().writeInt(4);
                                            pairingPacket.buffer().writeInt(20);
                                            pairingPacket.buffer().writeInt(10);
                                            pairingPacket.buffer().writeBoolean(false);

                                            EpicFightNetworkManager
                                                    .sendToAllPlayerTrackingThisEntityWithSelf(pairingPacket, serverPlayer);
                                        });
                            }
                        }
                        container.getDataManager().setDataSync(t0001SkillDataKeys.ACTIVATION_KEY, false);
                    }
                }, FX_ID, 2
        );

    }

    @ClientOnly
    @Override
    public void onInitiateClient(SkillContainer container) {
        super.onInitiateClient(container);
        Player player = container.getExecutor().getOriginal();


        container.getClientExecutor().getEntityDecorations().addParticleGenerator(this, ()-> {

            RandomSource random = player.getRandom();
            float chance = Mth.clampedLerp(0.0F, 0.04F, (1.0F - Math.min(1.0F, player.getLastHurtMobTimestamp() / 40.0F)));
            if(random.nextBoolean() && random.nextFloat() < chance){
                    FX fx = t0001.getmodfx("passive_lightning_ans");
                if (fx != null) {
                   EntityEffectExecutor pl  = new EntityEffectExecutor(fx, player.level(), player, EntityEffectExecutor.AutoRotate.NONE);
                   pl.setOffset(0,0,0);
                   pl.setScale(1,1,1);
                   pl.setRotation(0,0,0);
                   pl.setForcedDeath(false);
                   pl.setAllowMulti(true);
                   pl.setDelay(0);
                }

            }
            return false;
        });

    }

    private static float getResistance(ItemStack weapon) {
        int maxDamage = Math.max(1, weapon.getMaxDamage());
        int currentDamage = Math.min(weapon.getDamageValue(), maxDamage - 1);
        float durabilityRatio = ((float) maxDamage - currentDamage) / maxDamage;
        return 10.0f + (durabilityRatio * 40.0f);
    }

    //Using Ldlib2's networking methods here, it's a lib mod, and it is much more than that, recommend check it out
    @RPCPacket(LightningFXPacketID)
    public static void SendLightningFXPacket(Integer entityID, Vector3f entityPos) {
        PacketDelegations.triggeranomalouslightnin(entityID, entityPos);
    }


    public static void applyLightningEffectStatic(LivingEntity target, int durationTicks, float totalDamage) {
        if (!target.isAlive()) return;

        // Server logic
        if (target.level() instanceof ServerLevel serverLevel) {
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

            Vector3f targetPos = target.getPosition(0.1f).toVector3f();

            RPCPacketDistributor.rpcToTracking(serverLevel, target.chunkPosition(), RpcPacketIds.WHITE_LIGHTNING_VFX.id, target.getId(), targetPos);


            EpicFightCapabilities.<LivingEntity, LivingEntityPatch<LivingEntity>>getParameterizedEntityPatch(target, LivingEntity.class, LivingEntityPatch.class)
                    .ifPresentOrElse(
                            victimPatch -> {
                                StunType stunType;
                                float strength;
                                SoundEvent sound;
                                float pitch;
                                RandomSource data = victimPatch.getOriginal().getRandom();

                                /// TODO: USE CUSTOM SFX
                                if (data.nextInt() <= 5) {
                                    stunType = StunType.FALL;
                                    strength = Math.min(1.6f, totalDamage * 0.15f);
                                    sound = SoundEvents.TRIDENT_THUNDER.value();
                                    pitch = 1.0f;
                                } else {
                                    stunType = StunType.SHORT;
                                    strength = Math.max(0.5f, totalDamage * 0.08f);
                                    sound = target.level().getRandom().nextBoolean()
                                            ? SoundEvents.LAVA_EXTINGUISH
                                            : SoundEvents.FIRE_EXTINGUISH;
                                    pitch = 0.8f + target.level().getRandom().nextFloat() * 0.4f;
                                }

                                victimPatch.applyStun(stunType, strength);
                                target.playSound(sound, 1.0f, pitch);


                                target.hurt(target.damageSources().lightningBolt(), totalDamage * amperageParam);


                            }, () -> {

                                SoundEvent sound;
                                float pitch;
                                RandomSource data = target.getRandom();

                                if (data.nextInt() >= 5) {

                                    sound = SoundEvents.TRIDENT_THUNDER.value();
                                    pitch = 1.0f;
                                } else {
                                    sound = target.level().getRandom().nextBoolean()
                                            ? SoundEvents.LAVA_EXTINGUISH
                                            : SoundEvents.FIRE_EXTINGUISH;
                                    pitch = 0.8f + target.level().getRandom().nextFloat() * 0.4f;
                                }


                                target.playSound(sound, 1.0f, pitch);

                                target.hurt(target.damageSources().lightningBolt(), totalDamage * amperageParam);
                            }
                    );


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

  /*  @Override
    public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {

        int BAR_WIDTH = 150;
        int BAR_HEIGHT = 20;

        // Get screen dimensions from Window
        Window window = Minecraft.getInstance().getWindow();
        int screenWidth = window.getGuiScaledWidth();
        int screenHeight = window.getGuiScaledHeight();

        // Initialize UI once with FULL screen dimensions OR reinit if screen size changed
        if (cachedUI == null) {
            var ui = makemeterui(container, BAR_WIDTH, BAR_HEIGHT);
            cachedUI = ModularUI.of(ui);
            cachedUI.init(screenWidth, screenHeight);
        } else {
            // Re-initialize if screen dimensions changed (handles window resize)
            cachedUI.init(screenWidth, screenHeight);
        }

        // Calculate position: centered horizontally, above XP bar
        int centerX = (screenWidth - BAR_WIDTH) / 2;
        int yPos = screenHeight - 49 - 12;  // 49 from bottom + 12 offset

        // Update root element position
        cachedUI.ui.rootElement.layout(layout -> layout.left(centerX).top(yPos));

        cachedUI.getWidget().render(guiGraphics, 0, 0, partialTick);
    }

    private @NotNull UI makemeterui(SkillContainer container, int BAR_WIDTH, int BAR_HEIGHT) {
        var root = new UIElement();
        // Make root ABSOLUTE so drawOnGui can place it using left/top
        root.layout(layout -> layout.width(BAR_WIDTH).height(BAR_HEIGHT).positionType(YogaPositionType.ABSOLUTE));

        var ultimateMeter = new UltimateMeterWidget(
                () -> {
                    int currentMeter = container.getDataManager().getDataValue(t0001SkillDataKeys.ULTIMATE_METER);
                    return (float) currentMeter / MAX_ULTIMATE_METER;
                },
                () -> container.getDataManager().getDataValue(t0001SkillDataKeys.IS_AWAKENED),
                BAR_WIDTH,
                BAR_HEIGHT,
                "PlaceHolder ??"
        );

        root.addChild(ultimateMeter);

        return UI.of(root);
    }*/

}
