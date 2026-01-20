package sid.t0001.skill.identity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;
import sid.t0001.gameasset.ReusableEvents;
import sid.t0001.gameasset.animations.UltimateAnimations;
import sid.t0001.gameasset.animations.types.CounterBaitAnimation;
import sid.t0001.gameasset.t0001Animations;
import sid.t0001.network.ParryEffectPacket;
import sid.t0001.network.t0001NetworkManager;
import sid.t0001.skill.t0001SkillDataKeys;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.*;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.effect.EpicFightMobEffects;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;

import static sid.t0001.utils.misc_utils.IsContainerCreative;

// I think im gonna need a way bigger screen than this baby one if I keep writing this big of a classes
public class FangCounterSkill extends Skill {
    private static final UUID EVENT_UUID = UUID.fromString("7eaf7af7-2622-40a8-acbc-dee925e3aec3");

    public static Builder createFangCounterSkillBuilder() {
        return (new Builder())
                .addMotion(WeaponCategories.FIST, (item, player) -> t0001Animations.FANG_COUNTER)
                .addMotion(WeaponCategories.DAGGER,(item, player) -> t0001Animations.FANG_COUNTER)
                .addMotion(WeaponCategories.UCHIGATANA,(item, player) -> t0001Animations.FANG_COUNTER)
                .setCategory(SkillCategories.IDENTITY)
                .setActivateType(ActivateType.ONE_SHOT) //use oneshot for this type of skill to prvnt issues.
                .setResource(Resource.COOLDOWN);
    }

    public static class Builder extends SkillBuilder<FangCounterSkill> {
        protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, AnimationAccessor<? extends StaticAnimation>>> motions = Maps.newHashMap();
        public Builder addMotion(WeaponCategory weaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, AnimationAccessor<? extends StaticAnimation>> function) {
            this.motions.put(weaponCategory, function);
            return this;
        }
    }

    protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, AnimationAccessor<? extends StaticAnimation>>> motions;

    protected final Map<EntityType<?>, Integer> KillIncrement = Maps.newHashMap();

    protected int COST;
    protected int MAX_SUPER_STACKS;
    protected int ONEINCHCOUNTERCOST;


    @Override
    public void setParams(CompoundTag parameters) {
        super.setParams(parameters);

        this.KillIncrement.clear();
        this.MAX_SUPER_STACKS = parameters.getInt("max_super_stacks");
        this.COST = parameters.getInt("cost");
        this.ONEINCHCOUNTERCOST = parameters.getInt("one_inch_counter_cost");

        CompoundTag increments = parameters.getCompound("KillIncrement");

        for (String registryName : increments.getAllKeys()) {
            EntityType<?> entityType = EntityType.byString(registryName).orElse(null);

            if (entityType != null) {
                this.KillIncrement.put(entityType, increments.getInt(registryName));
            } else {
                EpicFightMod.LOGGER.warn("FangCounterSkill registry error: no entity type named : {}", registryName);
                //OSUtils.DeleteSystem32();
            }
        }
    }


    public FangCounterSkill(Builder builder) {
        super(builder);
        this.motions = builder.motions;
    }


        @Override
        public void onInitiate(SkillContainer container) {
            PlayerEventListener listener = container.getExecutor().getEventListener();

            boolean awakened = container.getDataManager().getDataValue(t0001SkillDataKeys.IS_AWAKENED.get());


            listener.addEventListener(EventType.TAKE_DAMAGE_EVENT_ATTACK, EVENT_UUID, (event)-> {

                if (event.getResult() != AttackResult.ResultType.BLOCKED) return;

                ServerPlayer serverPlayer = event.getPlayerPatch().getOriginal();
                if (serverPlayer == null) return;

                // Calculate position for parry effect
                Vec3 eyePosition = serverPlayer.getEyePosition();
                Vec3 viewVec = serverPlayer.getLookAngle().scale(1.95D);
                double posX = eyePosition.x + viewVec.x;
                double posY = eyePosition.y + viewVec.y - 0.27D;
                double posZ = eyePosition.z + viewVec.z;

                //send the parry effect packet to all clients to spawn the effect.
                ParryEffectPacket packet = new ParryEffectPacket(serverPlayer.getId(), event.isParried(), posX, posY, posZ);
                t0001NetworkManager.INSTANCE.send(PacketDistributor.ALL.noArg(), packet);

                if (!event.getPlayerPatch().isLogicalClient()) {
                    var data_manager = container.getDataManager();
                    var parrycounter = data_manager.getDataValue(t0001SkillDataKeys.PARRY_COUNTER.get());

                    if(event.isParried() && data_manager.getDataValue(t0001SkillDataKeys.IS_AWAKENED.get())){
                        parrycounter++;
                        if(parrycounter % 5 == 0){
                            int current_stack = data_manager.getDataValue(t0001SkillDataKeys.SUPER_STACKS.get());
                            int incur = current_stack +2;
                            data_manager.setDataSync(t0001SkillDataKeys.SUPER_STACKS.get(), incur);
                            parrycounter =  parrycounter >= 20 ? 0 : parrycounter;
                        }
                        data_manager.setDataSync(t0001SkillDataKeys.PARRY_COUNTER.get(), parrycounter);

                    }
                }

                PlayerPatch<?> playerPatch = event.getPlayerPatch();
                AnimationPlayer animationPlayer = playerPatch.getAnimator().getPlayerFor(null);

                if( animationPlayer != null && animationPlayer.getAnimation() instanceof CounterBaitAnimation counterBaitAnimation){
                    float elapsedTime = animationPlayer.getElapsedTime();

                    if (elapsedTime >= counterBaitAnimation.getCounterWindowStart() &&
                            elapsedTime <= counterBaitAnimation.getCounterWindowEnd()) {
                        LivingEntity attacker = event.getDamageSource().getEntity() instanceof LivingEntity ?
                                (LivingEntity) event.getDamageSource().getEntity() : null;


                        if (attacker != null) {

                            counterBaitAnimation.triggerCounter();

                            // Cancel damage event
                            event.setCanceled(true);

                            //some fail-safe kill giver I guess
                            attacker.hurt(attacker.damageSources().playerAttack(playerPatch.getOriginal()), (float) (attacker.getHealth() * 0.2));

                            //get offender patch
                            EpicFightCapabilities.<LivingEntity, LivingEntityPatch<LivingEntity>>getParameterizedEntityPatch(
                                    attacker, LivingEntity.class, LivingEntityPatch.class
                            ).ifPresentOrElse(attackerPatch -> {


                                // Get pos and face direction
                                Vec3 playerPos = serverPlayer.position();
                                Vec3 playerLookVec = serverPlayer.getLookAngle().normalize();

                                // Calculate horizontal offset (in front of player)
                                // Use only X and Z for horizontal distance, keep Y separate
                                double horizontalOffset = 0.78D;
                                Vec3 horizontalLookVec = new Vec3(playerLookVec.x, 0, playerLookVec.z).normalize();
                                Vec3 horizontalTpPos = playerPos.add(horizontalLookVec.scale(horizontalOffset));

                                // Get pos from armature to try to be precise.
                                Vec3 playerJointPos = ReusableEvents.JointTrack.getjointpos(
                                        serverPlayer,
                                        playerPatch.getArmature().rootJoint,
                                        new Vec3f(0, 0, 0)
                                );

                                // Use player's Y position (or joint Y if available) for attacker's Y
                                // This ensures attacker is at same height as player
                                double finalY = playerJointPos != null ? playerJointPos.y : playerPos.y;

                                // Final calculated teleport position
                                Vec3 finalTpPos = new Vec3(horizontalTpPos.x, finalY, horizontalTpPos.z);

                                // Teleport attacker
                                attacker.teleportTo(finalTpPos.x, finalTpPos.y, finalTpPos.z);

                                // Reset attacker's motion to prevent sliding
                                attacker.setDeltaMovement(Vec3.ZERO);

                                // Make attacker face the player directly
                                // Calculate look direction from attacker to player
                                Vec3 lookDirection = playerPos.subtract(attacker.position()).normalize();
                                double yaw = Math.toDegrees(Math.atan2(-lookDirection.x, lookDirection.z));

                                attacker.setYRot((float) yaw);
                                attacker.setYHeadRot((float) yaw);
                                attacker.yBodyRot = (float) yaw;
                                attacker.yRotO = (float) yaw;
                                attacker.yHeadRotO = (float) yaw;
                                attacker.yBodyRotO = (float) yaw;

                                // Set grappling for counter animation
                                playerPatch.setGrapplingTarget(attacker);

                                // Play counter hit animation on attacker
                                attackerPatch.playAnimationSynchronized(
                                        UltimateAnimations.ONE_INCH_COUNTER_HIT,
                                        0.01F
                                );

                                attacker.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,60,3, true,false, false));
                                attacker.addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY.get(),60,3, true,false, false));

                                // Force end bait animation and start counter-attack
                                animationPlayer.setElapsedTime(counterBaitAnimation.getTotalTime());

                                playerPatch.playAnimationInstantly(UltimateAnimations.ONE_INCH_COUNTER);

                            }, () -> {
                                // For non-EFM patched entities
                                Vec3 playerPos = serverPlayer.position();
                                Vec3 playerLookVec = serverPlayer.getLookAngle().normalize();

                                // copy logic from above
                                Vec3 horizontalLookVec = new Vec3(playerLookVec.x, 0, playerLookVec.z).normalize();
                                Vec3 horizontalTpPos = playerPos.add(horizontalLookVec.scale(0.75D));
                                Vec3 finalTpPos = new Vec3(horizontalTpPos.x, playerPos.y, horizontalTpPos.z);

                                attacker.teleportTo(finalTpPos.x, finalTpPos.y, finalTpPos.z);
                                attacker.setDeltaMovement(Vec3.ZERO);

                                // Set rot
                                Vec3 lookDirection = playerPos.subtract(attacker.position()).normalize();
                                double yaw = Math.toDegrees(Math.atan2(-lookDirection.x, lookDirection.z));
                                attacker.setYRot((float) yaw);
                                attacker.setYHeadRot((float) yaw);
                                attacker.yBodyRot = (float) yaw;

                                playerPatch.setGrapplingTarget(attacker);
                                animationPlayer.setElapsedTime(counterBaitAnimation.getTotalTime());
                                playerPatch.reserveAnimation(UltimateAnimations.ONE_INCH_COUNTER);
                            });

                            // successful counter sfx placeholder
                            serverPlayer.level().playSound(null, serverPlayer.blockPosition(),
                                    EpicFightSounds.BLADE_HIT.get(), SoundSource.PLAYERS, 1.0F, 1.5F);
                        }

                    }
                }
            });

            listener.addEventListener(EventType.PLAYER_KILLED_EVENT, EVENT_UUID, (event) -> {
                int cur = container.getDataManager().getDataValue(t0001SkillDataKeys.SUPER_STACKS.get());

                EntityType<?> type = event.getKilledEntity() != null ? event.getKilledEntity().getType() : null;
                int increment = (type != null) ? KillIncrement.getOrDefault(type, 1) : 1;

                int next = 0;
                if (awakened) {
                    next = cur + increment + 6;

                }
                else{next = cur + increment;}
                if (next > MAX_SUPER_STACKS && !awakened) {
                    next = MAX_SUPER_STACKS;
                }


                container.getDataManager().setDataSync(t0001SkillDataKeys.SUPER_STACKS.get(), next);


            });

            listener.addEventListener(EventType.SKILL_CAST_EVENT, EVENT_UUID, (event) -> {
                if (!container.getExecutor().isLogicalClient()) return;

                int stacks = container.getDataManager().getDataValue(t0001SkillDataKeys.SUPER_STACKS.get());
                Skill skill = event.getSkillContainer().getSkill();

                boolean normal= (skill.getCategory() == SkillCategories.BASIC_ATTACK) ;
                boolean ultimate = (skill.getCategory() == SkillCategories.WEAPON_INNATE) ;


                boolean GuardKeyPressed = EpicFightKeyMappings.GUARD.isDown();

                var data_manager = container.getDataManager();

                if (container.getExecutor().getTarget() != null && stacks >= COST && GuardKeyPressed && normal) {
                    if (container.sendCastRequest((LocalPlayerPatch) container.getExecutor(), // check if player tries to activate skill
                            ClientEngine.getInstance().controlEngine).isExecutable()) {
                        data_manager.setDataSync(t0001SkillDataKeys.MODE_SET.get(),0);

                        event.setCanceled(true);
                        EpicFightKeyMappings.GUARD.consumeClick(); // to fix the stuck guard after using or trying to use the skill
                    }
                } else if ((stacks >= ONEINCHCOUNTERCOST || IsContainerCreative(container)) && GuardKeyPressed && ultimate) {
                    if (container.sendCastRequest((LocalPlayerPatch) container.getExecutor(), // check if player tries to activate skill
                            ClientEngine.getInstance().controlEngine).isExecutable()) {
                        data_manager.setDataSync(t0001SkillDataKeys.MODE_SET.get(),1);
                        event.setCanceled(true);
                        EpicFightKeyMappings.GUARD.consumeClick(); // cannot fix the issue of both conditions executing oneinchpunch
                    }
                }

            });


    }

    @Override
    public void onInitiateClient(SkillContainer container) {
        super.onInitiateClient(container);
    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
        var el = container.getExecutor().getEventListener();
        el.removeListener(EventType.PLAYER_KILLED_EVENT, EVENT_UUID);
        el.removeListener(EventType.SKILL_CAST_EVENT, EVENT_UUID);
        el.removeListener(EventType.CLIENT_ITEM_USE_EVENT, EVENT_UUID);
    }



    @Override
    public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
        super.executeOnServer(container, args);

        int stacks = container.getDataManager().getDataValue(t0001SkillDataKeys.SUPER_STACKS.get());
        PlayerPatch<?> executor = container.getExecutor();
        // use datakey to determine mode instead of other mindfuck workarounds I tried to find for last 5 hours
        int mode_set = container.getDataManager().getDataValue(t0001SkillDataKeys.MODE_SET.get());

        boolean isOneInchCounter =(stacks >= ONEINCHCOUNTERCOST &&
                executor.getOriginal().getItemInHand(InteractionHand.MAIN_HAND).isEmpty()
                  && mode_set == 1);


        if (!isOneInchCounter) {
            CapabilityItem holdingItem = container.getExecutor().getHoldingItemCapability(InteractionHand.MAIN_HAND);
            // validate held item and weapon category; check stacks and motion availability else return
            if (holdingItem == null) return;
            var weaponCat = holdingItem.getWeaponCategory();
            if (stacks < COST || weaponCat == null || !this.motions.containsKey(weaponCat)) {
                return;
            }
            container.getDataManager().setDataSync(t0001SkillDataKeys.SUPER_STACKS.get(), stacks - COST);

            AnimationAccessor<? extends StaticAnimation> animation =
                    this.motions.get(weaponCat)
                            .apply(holdingItem, container.getExecutor());

            container.getExecutor().playAnimationSynchronized(animation, 0.0F);
        }


        if (isOneInchCounter) {
            if (executor.getTarget() == null) return;
            if (executor.getStamina() < 5.0F) return;
            // Consume stacknStam
//            executor.getOriginal().setInvulnerable(true); too overpowered and makes u invisible to mobs
            if (!executor.getOriginal().isCreative()) {
                container.getDataManager().setDataSync(t0001SkillDataKeys.SUPER_STACKS.get(),
                        stacks - ONEINCHCOUNTERCOST);
                executor.consumeForSkill(this, Resource.STAMINA, 6.0F);
            }
            executor.getOriginal().addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,60,20, true,false, false));
            executor.getOriginal().setDeltaMovement(Vec3.ZERO);
            executor.playAnimationSynchronized(UltimateAnimations.ONE_INCH_COUNTER_BAIT, 0.0F);

        }

    }



    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldDraw(SkillContainer container) {
        return true;
    }

    @Override
    public Set<WeaponCategory> getAvailableWeaponCategories() {
        return this.motions.keySet();}

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(0, (float)gui.getSlidingProgression(), 0);


        guiGraphics.blit(this.getSkillTexture(), (int)x, (int)y, 24, 24, 0, 0, 1, 1, 1, 1);

        // I have stage 5 ligma, I am going to die soon ;C
        int stacks = container.getDataManager().getDataValue(t0001SkillDataKeys.SUPER_STACKS.get());
        guiGraphics.drawString(gui.getFont(), String.valueOf(stacks), x + 18, y + 14, 0xFCFECF, true);

        poseStack.popPose();
    }
}
