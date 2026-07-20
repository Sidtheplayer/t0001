package sid.base.skill.identity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import sid.base.events.ExecutionHandle;
import sid.base.gameasset.animations.UltimateAnimations;
import sid.base.gameasset.animations.t0001Animations;
import sid.base.main.t0001;
import sid.base.skill.t0001SkillDataKeys;
import sid.base.utils.HelperUtils;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.IdentifierProvider;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.registry.entries.EpicFightMobEffects;
import yesman.epicfight.skill.*;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/*
* ultimate mode set = 0 -> normal
* ultimate mode set = 10 -> normal active
* ultimate mode set = 1 -> ultimate
* ultimate mode set = 11 -> ultimate active
* */

public class FangCounterSkill extends Skill {

    public final IdentifierProvider fcskillcast = IdentifierProvider.constant("fcskillcast");

    public static Builder createFangCounterSkillBuilder() {
        return new Builder(FangCounterSkill::new)
                .addMotion(WeaponCategories.FIST, (item, player) -> t0001Animations.FANG_COUNTER)
                .addMotion(WeaponCategories.DAGGER, (item, player) -> t0001Animations.FANG_COUNTER)
                .addMotion(WeaponCategories.UCHIGATANA, (item, player) -> t0001Animations.FANG_COUNTER)
                .setCategory(SkillCategories.IDENTITY)
                .setActivateType(ActivateType.ONE_SHOT) //activation types are very important
                .setResource(Resource.COOLDOWN);
    }

    public static class Builder extends SkillBuilder<FangCounterSkill.Builder> {
        protected final Map<WeaponCategory,
                BiFunction<CapabilityItem, PlayerPatch<?>,
                        AnimationAccessor<? extends StaticAnimation>>> motions = Maps.newHashMap();

        public Builder(Function<Builder, ? extends Skill> constructor) {
            super(constructor);
        }

        public Builder addMotion(
                WeaponCategory category,
                BiFunction<CapabilityItem, PlayerPatch<?>,
                        AnimationAccessor<? extends StaticAnimation>> motion
        ) {
            this.motions.put(category, motion);
            return this;
        }
    }

    protected final Map<WeaponCategory,
            BiFunction<CapabilityItem, PlayerPatch<?>,
                    AnimationAccessor<? extends StaticAnimation>>> motions;

    protected final Map<EntityType<?>, Integer> killIncrement = Maps.newHashMap();
    protected int COST;
    protected int MAX_SUPER_STACKS;
    protected int ONEINCHCOUNTERCOST;
    protected int AWAKENED_BUFF;


    public FangCounterSkill(Builder builder) {
        super(builder);
        this.motions = builder.motions;
    }


    @Override
    public void loadDatapackParameters(CompoundTag parameters) {
        super.loadDatapackParameters(parameters);

        killIncrement.clear();
        MAX_SUPER_STACKS = parameters.getInt("max_super_stacks");
        COST = parameters.getInt("cost");
        ONEINCHCOUNTERCOST = parameters.getInt("one_inch_counter_cost");
        AWAKENED_BUFF = parameters.getInt("awakened_buff");

        CompoundTag increments = parameters.getCompound("KillIncrement");
        for (String key : increments.getAllKeys()) {
            EntityType.byString(key).ifPresentOrElse(
                    type -> killIncrement.put(type, increments.getInt(key)),
                    () -> t0001.LOGGER.warn(
                            "FangCounterSkill: unknown entity type {}", key
                    )
            );
        }
    }

    /* ---------------- EVENTS ---------------- */

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);

        var data_manager = container.getDataManager();

       

        boolean is_in_creative = container.getExecutor().getOriginal().isCreative();

        //use planned priorities on event listeners when using more than one event listener
        // or use more than 1 IdentifierProvider and then remove those identifiers manually
        // (look onRemoved() method below)


        eventListener.registerContextAwareEvent(
                EpicFightEventHooks.Entity.TAKE_DAMAGE_INCOME,
                (event,ihatemylife) -> {

                    if (event.getResult() != AttackResult.ResultType.BLOCKED) return;
                    int parrycounter = data_manager.getDataValue(t0001SkillDataKeys.PARRY_COUNTER);

                    boolean is_currently_awakened = HelperUtils.is_Awakened(container.getExecutor());


                    if(event.isParried() && is_currently_awakened){
                        parrycounter++;
                        if(parrycounter % 5 == 0){
                            int current_stack = data_manager.getDataValue(t0001SkillDataKeys.SUPER_STACKS);
                            int incur = current_stack + AWAKENED_BUFF;
                            data_manager.setDataSync(t0001SkillDataKeys.SUPER_STACKS, incur);
                            parrycounter =  parrycounter >= 20 ? 0 : parrycounter;
                        }
                        data_manager.setDataSync(t0001SkillDataKeys.PARRY_COUNTER, parrycounter);


                    }

                },this
        );



        eventListener.registerEvent(
                EpicFightEventHooks.Entity.KILL_ENTITY,
                (evt )->{

                    int current = container.getDataManager().getDataValue(t0001SkillDataKeys.SUPER_STACKS);

                    EntityType<?> type = evt.getKilledEntity().getType();

                    int inc =  killIncrement.getOrDefault(type, 1);

                    boolean is_currently_awakened = HelperUtils.is_Awakened(evt.getEntityPatch());


                    int next = is_currently_awakened ? current + inc + AWAKENED_BUFF : Math.clamp(current + inc, 0, MAX_SUPER_STACKS);

                    container.getDataManager().setDataSync(t0001SkillDataKeys.SUPER_STACKS, next);


                },this
        );

        eventListener.registerEvent(
                EpicFightEventHooks.Player.CAST_SKILL,
                (event)-> {

                    if (container.getExecutor().isLogicalClient()) {
                        boolean is_currently_awakened = HelperUtils.is_Awakened(event.getEntityPatch());

                        int current_super_stacks = data_manager.getDataValue(t0001SkillDataKeys.SUPER_STACKS);
                        Skill skill = event.getSkillContainer().getSkill(); //imp
                        boolean GuardKeyPressed = EpicFightKeyMappings.GUARD.isDown();
                        boolean normal = (skill.getCategory() == SkillCategories.BASIC_ATTACK);
                        boolean ultimate = (skill.getCategory() == SkillCategories.WEAPON_INNATE);


                        if (container.getExecutor().getTarget() != null && normal && (current_super_stacks >= COST || is_in_creative) && GuardKeyPressed) {
                            EpicFightCapabilities.getUnparameterizedEntityPatch(container.getExecutor().getTarget(), LivingEntityPatch.class).ifPresent(entitypatch -> {
                                 {
                                    if (container.sendCastRequest(container.getClientExecutor(), ControlEngine.getInstance()).isExecutable()) {
                                        data_manager.setDataSync(t0001SkillDataKeys.ULTIMATE_MOVE_MODE_SET, 0);
                                      //  t0001.LOGGER.debug("NORMAL SKILL CAST REQUEST");
                                        event.cancel();
                                    }
                                }// if (this.isActivated(container)) can break cast req
                            });


                        } else if (ultimate && (current_super_stacks >= ONEINCHCOUNTERCOST || is_in_creative)
                                && is_currently_awakened && GuardKeyPressed) {
                            EpicFightCapabilities.getUnparameterizedEntityPatch(container.getExecutor().getTarget(), LivingEntityPatch.class).ifPresent(entitypatch -> {
                              {
                                    if (container.sendCastRequest(container.getClientExecutor(), ControlEngine.getInstance()).isExecutable()) {
                                        data_manager.setDataSync(t0001SkillDataKeys.ULTIMATE_MOVE_MODE_SET, 1);
                                     //   t0001.LOGGER.debug("ULTIMATE SKILL CAST REQUEST");
                                        event.cancel();
                                    }
                              }

                            });

                        }
                    }
                },fcskillcast
        );

        eventListener.registerEvent(
                EpicFightEventHooks.Entity.TAKE_DAMAGE_PRE,
                (event) -> {

                    //One Inch Counter Execution Code
                    AnimationPlayer animationPlayer = event.getEntityPatch().getServerAnimator().animationPlayer;

                    if(animationPlayer.getAnimation().equals(UltimateAnimations.ONE_INCH_COUNTER_BAIT)){
                        if(!animationPlayer.isEnd()) {
                            LivingEntity attacker = event.getDamageSource().getEntity() instanceof LivingEntity ?
                                    (LivingEntity) event.getDamageSource().getEntity() : null;
                            if (attacker != null) {

                                ExecutionHandle.setup_simple_forward_execution(
                                        0.66D,
                                        attacker,container.getExecutor(),
                                        UltimateAnimations.ONE_INCH_COUNTER,
                                        UltimateAnimations.ONE_INCH_COUNTER_HIT
                                );

                            }
                        }
                    }
                },this
        );


    }

    @Override
    public void executeOnServer(SkillContainer container, CompoundTag arguments) {
        super.executeOnServer(container,arguments);
        var data_manager = container.getDataManager();
        int mode_set = data_manager.getDataValue(t0001SkillDataKeys.ULTIMATE_MOVE_MODE_SET);
        if (mode_set == 0) {
            CapabilityItem item =
                    container.getExecutor().getHoldingItemCapability(InteractionHand.MAIN_HAND);

            if (item == null) return;

            int stacks = container.getDataManager()
                    .getDataValue(t0001SkillDataKeys.SUPER_STACKS);

            WeaponCategory category = item.getWeaponCategory();
            if (category == null || stacks < COST || !motions.containsKey(category)) return;
            data_manager.setDataSync(t0001SkillDataKeys.ULTIMATE_MOVE_MODE_SET, 10);
            container.getDataManager()
                    .setDataSync(t0001SkillDataKeys.SUPER_STACKS, stacks - COST);

            container.getExecutor().playAnimationSynchronized(
                    motions.get(category).apply(item, container.getExecutor()),
                    0.0F
            );
            container.getExecutor().getOriginal()
                    .addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY,30,2, true,false, false));


        } else if (mode_set == 1) {
            var executor = container.getExecutor();
            int stacks = data_manager.getDataValue(t0001SkillDataKeys.SUPER_STACKS);
            if (executor.getStamina() < 5.0F) return;
            data_manager.setDataSync(t0001SkillDataKeys.ULTIMATE_MOVE_MODE_SET, 11);

//            executor.getOriginal().setInvulnerable(true); too overpowered and makes u invisible to mobs

            // Consume stack n Stam
            if (!executor.getOriginal().isCreative()) {
                data_manager.setDataSync(t0001SkillDataKeys.SUPER_STACKS,
                        stacks - ONEINCHCOUNTERCOST);
                executor.consumeForSkill(this, Resource.STAMINA, 6.0F);
            }
            executor.getOriginal().addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,260,20, true,false, false));
            executor.getOriginal().addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY,180,20, true,false, false));
            executor.getOriginal().setDeltaMovement(Vec3.ZERO);
            executor.playAnimationSynchronized(UltimateAnimations.ONE_INCH_COUNTER_BAIT, 0.0F);
        }
    }

    @Override
    public Set<WeaponCategory> getAvailableWeaponCategories() {
        return motions.keySet();
    }

    @Override
    public boolean shouldDraw(SkillContainer container) {
        return container.getExecutor() != null;
    }

    @Override
    @ClientOnly
    public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {

        int stacks = container.getDataManager().getDataValue(t0001SkillDataKeys.SUPER_STACKS);
        int color = calculateStackColor(stacks, COST);
        boolean isShiny = stacks >= 25;

        guiGraphics.blit(this.getSkillTexture(), (int) x, (int) y, 24, 24, 0, 0, 1, 1, 1, 1);

        if (container.getRemainDuration() > 0) {
            return;
        }


        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        if (isShiny) {
            double time = System.currentTimeMillis() / 1000.0;
            float scale = 1.0f + (float) Math.sin(time * 3.0) * 0.1f;
            float rotation = (float) Math.sin(time * 2.0) * 2.0f;

            // Move to text position, apply transformations, then draw
            poseStack.translate(x + 18 + 4, y + 14 + 4, 0); // +4 to rotate around center
            poseStack.scale(scale, scale, 1.0f);
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));
            poseStack.translate(-4, -4, 0);

            guiGraphics.drawString(gui.getFont(), String.valueOf(stacks), 0, 0, color, true);
        } else {
            guiGraphics.drawString(gui.getFont(), String.valueOf(stacks), (int)(x + 18), (int)(y + 14), color, true);
        }

        poseStack.popPose();
    }


    private int calculateStackColor(int stacks, int cost) {
        if (stacks < cost) {
            // Gray if not enough stacks
            // Gradually brighten from dark gray to light gray as nearing cost
            float progress = (float) stacks / cost;
            int gray = (int) (0x77 + (0xAA - 0x77) * progress);
            return (gray << 16) | (gray << 8) | gray;

        } else if (stacks < 25) {
            // Whitey color when enough but not shiny
            // Gradually transition
            float progress = (float) (stacks - cost) / (25 - cost);

            // Start color
            int r1 = 0xFC, g1 = 0xFE, b1 = 0xCF;
            // End color
            int r2 = 0xFF, g2 = 0x66, b2 = 0x66;

            int r = (int) (r1 + (r2 - r1) * progress);
            int g = (int) (g1 + (g2 - g1) * progress);
            int b = (int) (b1 + (b2 - b1) * progress);

            return (r << 16) | (g << 8) | b;

        } else {
            // Full red when at or above shiny threshold
            // Pulsate between bright red and darker red
            float pulse = (float) Math.sin(System.currentTimeMillis() / 200.0) * 1.5f + 0.5f;
            int red = (int) (0xFF * (0.7f + 0.3f * pulse));
            int green = (int) (0x66 * (0.7f + 0.3f * pulse));
            int blue = (int) (0x66 * (0.7f + 0.3f * pulse));

            return (red << 16) | (green << 8) | blue;
        }
    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
        container.getExecutor().getEventListener().removeListenersBelongTo(fcskillcast);
    }



}
