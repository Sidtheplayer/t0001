package sid.base.skill.identity;

import com.google.common.collect.Maps;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import sid.base.gameasset.ReusableEvents;
import sid.base.gameasset.animations.UltimateAnimations;
import sid.base.gameasset.t0001Animations;
import sid.base.main.t0001;
import sid.base.network.ParryEffectPacket;
import sid.base.skill.t0001SkillDataKeys;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.IdentifierProvider;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.registry.entries.EpicFightMobEffects;
import yesman.epicfight.skill.*;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;


public class FangCounterSkill extends Skill {

    public final IdentifierProvider FC_Identifier = IdentifierProvider.constant("FC_SKILL_CAST");

    public static Builder createFangCounterSkillBuilder() {
        return new Builder(FangCounterSkill::new)
                .addMotion(WeaponCategories.FIST, (item, player) -> t0001Animations.FANG_COUNTER)
                .addMotion(WeaponCategories.DAGGER, (item, player) -> t0001Animations.FANG_COUNTER)
                .addMotion(WeaponCategories.UCHIGATANA, (item, player) -> t0001Animations.FANG_COUNTER)
                .setCategory(SkillCategories.IDENTITY)
                .setActivateType(ActivateType.ONE_SHOT)
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

        eventListener.registerEvent(
                EpicFightEventHooks.Entity.TAKE_DAMAGE_POST,
                (event) ->
                {
                    AnimationPlayer animationPlayer = event.getEntityPatch().getServerAnimator().animationPlayer;

                    if(animationPlayer.getAnimation().equals(UltimateAnimations.ONE_INCH_COUNTER_BAIT)){
                        while(!animationPlayer.isEnd()){
                            event.cancel();
                        }
                    }

                    if(animationPlayer.getAnimation().equals(UltimateAnimations.ONE_INCH_COUNTER)){
                        while(!animationPlayer.isEnd()){
                            event.cancel();
                        }
                    }

                },this ,-1
        );

        eventListener.registerContextAwareEvent(
                EpicFightEventHooks.Entity.TAKE_DAMAGE_INCOME,
                (event,ihatemylife) -> {
                    if (event.getResult() != AttackResult.ResultType.BLOCKED) return;

                    ServerPlayerPatch patch = container.getServerExecutor();
                    ServerPlayer player = patch.getOriginal();

                    // these parry effects will be made global for specific weapon_types/weapons soon
                    Vec3 eye = player.getEyePosition();
                    Vec3 view = player.getLookAngle().scale(1.45D);

                    ParryEffectPacket packet = new ParryEffectPacket(
                            player.getStringUUID(),
                            event.isParried(),
                            eye.x + view.x,
                            eye.y + view.y - 0.27D,
                            eye.z + view.z
                    );

                     //sendToPlayersTrackingEntityAndSelf is important otherwise fx won't play to you
                    PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, packet);


                    int parrycounter = data_manager.getDataValue(t0001SkillDataKeys.PARRY_COUNTER);

                    boolean is_currently_awakened = data_manager.getDataValue(t0001SkillDataKeys.IS_AWAKENED);
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

                },this,4
        );

        eventListener.registerEvent(
                EpicFightEventHooks.Entity.KILL_ENTITY,
                (evt )->{

                    int current = container.getDataManager()
                            .getDataValue(t0001SkillDataKeys.SUPER_STACKS);

                    EntityType<?> type = evt.getKilledEntity().getType();

                    int inc =  killIncrement.getOrDefault(type, 1);

                    boolean is_currently_awakened = data_manager.getDataValue(t0001SkillDataKeys.IS_AWAKENED);
                    int next = is_currently_awakened ? Math.min(current + inc, MAX_SUPER_STACKS) + AWAKENED_BUFF : Math.min(current + inc, MAX_SUPER_STACKS);

                    container.getDataManager().setDataSync(t0001SkillDataKeys.SUPER_STACKS, next);


                },this ,3
        );

        eventListener.registerEvent(
                EpicFightEventHooks.Player.CAST_SKILL,
                (event)-> {

                    if (container.getExecutor().isLogicalClient()) {
                        boolean is_currently_awakened = data_manager.getDataValue(t0001SkillDataKeys.IS_AWAKENED);

                        int current_super_stacks = data_manager.getDataValue(t0001SkillDataKeys.SUPER_STACKS);
                        Skill skill = container.getSkill();
                        boolean GuardKeyPressed = EpicFightKeyMappings.GUARD.isDown();
                        boolean normal = (skill.getCategory() == SkillCategories.BASIC_ATTACK);
                        boolean ultimate = (skill.getCategory() == SkillCategories.WEAPON_INNATE
                              );


                        if (container.getExecutor().getTarget() != null && normal && (current_super_stacks >= COST || is_in_creative) && GuardKeyPressed) {
                            EpicFightCapabilities.getUnparameterizedEntityPatch(container.getExecutor().getTarget(), LivingEntityPatch.class).ifPresent(entitypatch -> {
                                if (this.isActivated(container)) {
                                    if (container.sendCastRequest(container.getClientExecutor(), ControlEngine.getInstance()).isExecutable()) {
                                        data_manager.setDataSync(t0001SkillDataKeys.ULTIMATE_MOVE_MODE_SET, 0);
                                        event.cancel();
                                    }
                                }
                            });


                        } else if (ultimate && (current_super_stacks >= ONEINCHCOUNTERCOST || is_in_creative)
                                && is_currently_awakened) {
                            EpicFightCapabilities.getUnparameterizedEntityPatch(container.getExecutor().getTarget(), LivingEntityPatch.class).ifPresent(entitypatch -> {
                                if (this.isActivated(container)) {
                                    if (container.sendCastRequest(container.getClientExecutor(), ControlEngine.getInstance()).isExecutable()) {
                                        data_manager.setDataSync(t0001SkillDataKeys.ULTIMATE_MOVE_MODE_SET, 1);
                                        event.cancel();
                                    }
                                }

                            });

                        }
                    }
                },FC_Identifier ,0
        );

        eventListener.registerEvent(
                EpicFightEventHooks.Entity.TAKE_DAMAGE_PRE,
                (event) -> {

                    //One Inch Counter Execution Code
                    AnimationPlayer animationPlayer = event.getEntityPatch().getServerAnimator().animationPlayer;

                    if(animationPlayer.getAnimation().equals(UltimateAnimations.ONE_INCH_COUNTER_BAIT)){
                        if(!animationPlayer.isEnd()){
                             LivingEntity attacker = event.getDamageSource().getEntity() instanceof LivingEntity ?
                                    (LivingEntity) event.getDamageSource().getEntity() : null;
                             if(attacker != null){

                                 EpicFightCapabilities.<LivingEntity, LivingEntityPatch<LivingEntity>>getParameterizedEntityPatch(
                                         attacker, LivingEntity.class, LivingEntityPatch.class
                                 ).ifPresent(attackerPatch -> {

                                     PlayerPatch<?> playerPatch = (PlayerPatch<?>) event.getEntityPatch();

                                     @SuppressWarnings("unused") Vec3 playerPos = event.getEntityPatch().getOriginal().position();
                                     Vec3 playerEyePos = container.getServerExecutor().getOriginal().getEyePosition();
                                     Vec3 playerLookVec = event.getEntityPatch().getOriginal().getLookAngle().normalize();

                                     double forwardOffset = 1.95D;
                                      //Teleport code needs improvement- sampling to be done
                                     // Calculate teleport position in front of player
                                     Vec3 tpPos = playerEyePos.add(playerLookVec.scale(forwardOffset));

                                     // Get the joint position for Y coordinate
                                     Vec3 jointPos = ReusableEvents.JointTrack.getjointpos(
                                             playerPatch.getOriginal(),
                                             playerPatch.getArmature().rootJoint,
                                             Vec3f.ZERO
                                     );
                                     float EyeHeightDiff_toSubtract =  attacker.getEyeHeight() - playerPatch.getOriginal().getEyeHeight();

                                     // Teleport attacker
                                     attacker.teleportTo(tpPos.x, Objects.requireNonNullElse(jointPos, tpPos).y - EyeHeightDiff_toSubtract, tpPos.z);

                                     // Make attacker face the player (invert look vector)
                                     Vec3 invertedEyePos = playerEyePos.multiply(-1D, 1D, -1D);

                                     attacker.lookAt(EntityAnchorArgument.Anchor.EYES, invertedEyePos);

                                     attacker.setYRot(attacker.getYHeadRot());
                                     attacker.yBodyRot = (float) (attacker.getYRot() + (attacker.getBbHeight() / 1.8) - 1);

                                     // Set up counter's grappling
                                     playerPatch.setGrapplingTarget(attacker);

                                     attacker.addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY,120,2));
                                     attackerPatch.playAnimationSynchronized(UltimateAnimations.ONE_INCH_COUNTER_HIT, 0.0F);
                                     playerPatch.playAnimationSynchronized(UltimateAnimations.ONE_INCH_COUNTER,0.0125F);

                                 });


                             }


                        }

                    }
                    container.activate();
                },this ,-1
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

            container.getDataManager()
                    .setDataSync(t0001SkillDataKeys.SUPER_STACKS, stacks - COST);

            container.getExecutor().playAnimationSynchronized(
                    motions.get(category).apply(item, container.getExecutor()),
                    0.0F
            );
        } else if (mode_set == 1) {
            var executor = container.getExecutor();
            int stacks = data_manager.getDataValue(t0001SkillDataKeys.SUPER_STACKS);
            if (executor.getStamina() < 5.0F) return;

//            executor.getOriginal().setInvulnerable(true); too overpowered and makes u invisible to mobs

            // Consume stack n Stam
            if (!executor.getOriginal().isCreative()) {
                data_manager.setDataSync(t0001SkillDataKeys.SUPER_STACKS,
                        stacks - ONEINCHCOUNTERCOST);
                executor.consumeForSkill(this, Resource.STAMINA, 6.0F);
            }
            executor.getOriginal().addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,60,20, true,false, false));
            executor.getOriginal().addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY,80,20, true,false, false));
            // possibly redundant because I do same in anim, but I am too superstitious, so I keep it here
            executor.getOriginal().setDeltaMovement(Vec3.ZERO);
            executor.playAnimationSynchronized(UltimateAnimations.ONE_INCH_COUNTER_BAIT, 0.0F);

            
        }
    }

    @Override
    public Set<WeaponCategory> getAvailableWeaponCategories() {
        return motions.keySet();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
        guiGraphics.blit(this.getSkillTexture(), (int) x, (int) y, 24, 24, 0, 0, 1, 1, 1, 1);
         //gonna use ldlib2.0 for this fuck this shit
        if (container.getRemainDuration() > 0) {
            return;
        }
        //FIXME
        // _^^ later bro ^^_
        int stacks = container.getDataManager().getDataValue(t0001SkillDataKeys.SUPER_STACKS);

        boolean active = container.isActivated();
        boolean enoughStacks = stacks >= COST;

        int color = (active && enoughStacks) ? 0xFCFECF : 0x777777;

        guiGraphics.drawString(gui.getFont(), String.valueOf(stacks), x + 18, y + 14, color, true);
    }

}
