package sid.base.events.global_events;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.photon.command.BlockEffectCommand;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import sid.base.events.QuickTimeEventHandle;
import sid.base.events.event_hook.MyEventHooks;
import sid.base.gameasset.animations.CustomSynchedAnimationVariablekeys;
import sid.base.gameasset.animations.DragonGodSwordAnimations;
import sid.base.gameasset.animations.MiscAnimations;
import sid.base.gameasset.animations.types.ProtectedHitAnimation;
import sid.base.world.t0001Sounds;
import sid.base.main.Config;
import sid.base.main.t0001;
import sid.base.network.ParryEffectPacket;
import sid.base.particle.t0001Particles;
import sid.base.skill.awakening.JunAwaken;
import sid.base.skill.t0001SkillDataKeys;
import sid.base.skill.t0001SkillSlots;
import sid.base.utils.ReusableAnimEvents;
import sid.base.network.RPC.RpcPacketIds;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.LevelUtil;
import yesman.epicfight.registry.entries.EpicFightAttributes;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.damagesource.StunType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@EventBusSubscriber(modid = "t0001")
public class GlobalEventHandlers {

    @SubscribeEvent
    public static void SetupEventHooksAtStartup(FMLCommonSetupEvent commonSetupEvent) {

        MyEventHooks.MISC.QTE_RESULT.registerEvent(event -> {
            QuickTimeEventHandle.QuickTimeResult result = event.getResult();

            ServerPlayer player = (ServerPlayer) event.getEntityPatch().getOriginal();

            LivingEntityPatch<?> patch = event.getEntityPatch();

            if (result.QuickTimeContext().equals("phantom_severance")) {
                if (!result.isQTESuccess()) {
                    player.playNotifySound(t0001Sounds.UI_ERROR.value(), SoundSource.MASTER, 3f, 1f);

                    LivingEntity lastAttacker = player.getLastHurtByMob();

                    if (lastAttacker == null) {
                        player.hurt(player.damageSources().genericKill(), Float.MAX_VALUE);

                    } else {
                        EpicFightDamageSource damageSource = lastAttacker instanceof Player enemy
                                ? EpicFightDamageSources.playerAttack(enemy)
                                : EpicFightDamageSources.mobAttack(lastAttacker);
                        damageSource.addRuntimeTag(DamageTypeTags.BYPASSES_INVULNERABILITY);
                        player.hurt(damageSource, Float.MAX_VALUE);
                    }

                    Vec3 pos = player.getEyePosition().subtract(0,0.25,0);

                    player.level().addParticle(t0001Particles.BUZZ_HIT.value(),pos.x,pos.y,pos.z,0,0,0);

                    Optional<Integer> entityID = patch.getAnimator().getVariables().get(CustomSynchedAnimationVariablekeys.KILLER_ENTITY.value(), DragonGodSwordAnimations.SHINJI_DODGES);

                    if(entityID.isPresent()){
                        Entity entity = player.level().getEntity(entityID.get());
                        if(entity instanceof LivingEntity lv){
                            LivingEntityPatch<?> Lpp = EpicFightCapabilities.getEntityPatch(lv, LivingEntityPatch.class);
                            Lpp.stopPlaying(DragonGodSwordAnimations.TOO_EASY_IDLE);
                        }
                    }

                    patch.playAnimationSynchronized(MiscAnimations.RAG_DOLL_STUN_UP, 0.0f);

                    RPCPacketDistributor.rpcToPlayer(player, RpcPacketIds.DESTROY_VFX_PACKET.id, true, "photon:phantom_realm", player.getId());
                } else {
                    player.playNotifySound(SoundEvents.COMPARATOR_CLICK, SoundSource.NEUTRAL, 3f, 1f);
                    player.playNotifySound(SoundEvents.LARGE_AMETHYST_BUD_PLACE, SoundSource.NEUTRAL, 3f, 1f);
                }
            }

            if (result.QuickTimeContext().equals("test_quickTime")) {

                if (!result.isQTESuccess()) {
                    player.playNotifySound(t0001Sounds.UI_ERROR.value(), SoundSource.MASTER, 10f, 1f);
                } else {
                    player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.MASTER, 10f, 1f);
                }
            }


        });



        EpicFightEventHooks.Animation.END.registerEvent(event -> {

            if(event.getAnimation().checkType(ProtectedHitAnimation.class)){
                ProtectedHitAnimation.ProtectedEntities.remove(event.getEntityPatch().getOriginal());
            }

        });


        //Parry VFX
        EpicFightEventHooks.Entity.TAKE_DAMAGE_INCOME.registerContextAwareEvent(
                (event, eventContext) -> {

                    if (event.getResult() != AttackResult.ResultType.BLOCKED) return;
                    if (!(event.getEntityPatch().getOriginal() instanceof ServerPlayer player)) return;

                    // these parry effects will be made global for specific weapon_types/weapons soon
                    Vec3 eye = player.getEyePosition();
                    Vec3 view = player.getLookAngle().scale(1.95D); // prev: 1.45 dihh

                    ParryEffectPacket packet = new ParryEffectPacket(
                            player.getStringUUID(),
                            event.isParried(),
                            eye.x + view.x,
                            eye.y + view.y + 0.95,
                            eye.z + view.z
                    );

                    //sendToPlayersTrackingEntityAndSelf is important otherwise fx won't play to you
                    if (Config.parryParticle) {
                        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, packet);
                    }


                });

        EpicFightEventHooks.Player.TICK_EPICFIGHT_MODE.registerContextAwareEvent((tickPlayerEpicFightModeEvent, eventContext) ->
        {
            try {
                LivingEntity entity = tickPlayerEpicFightModeEvent.getPlayerPatch().getOriginal();
                if (entity.getTags().contains("texaf")) {
                    if (entity.tickCount % 5 == 0) {
                        entity.level().addParticle(
                                t0001Particles.TEX_AFTERIMAGE.get(),
                                entity.getX(),
                                entity.getY(),
                                entity.getZ(),
                                Double.longBitsToDouble(entity.getId()),
                                0, 0
                        );
                    }
                }
                if (tickPlayerEpicFightModeEvent.getPlayerPatch().getSkill(t0001SkillSlots.AWAKENING).getDataManager().hasData(t0001SkillDataKeys.ULTIMATE_METER)) {
                    SkillContainer container = tickPlayerEpicFightModeEvent.getPlayerPatch().getSkill(t0001SkillSlots.AWAKENING);
                    if(entity.getTags().contains("awaken")){
                        entity.getTags().remove("awaken");
                        container.getDataManager().setDataSync(t0001SkillDataKeys.ULTIMATE_METER, JunAwaken.Meter_Capacity);
                    }

                    if (entity.getTags().contains("fillupmeter")) {
                        if (container.getDataManager().getDataValue(t0001SkillDataKeys.ULTIMATE_METER) >= JunAwaken.Meter_Capacity) {
                            entity.removeTag("fillupmeter");
                            container.getDataManager().setDataSync(t0001SkillDataKeys.ULTIMATE_METER, JunAwaken.Meter_Capacity);
                        }
                        float meter_val = tickPlayerEpicFightModeEvent.getPlayerPatch().getSkill(t0001SkillSlots.AWAKENING).getDataManager().getDataValue(t0001SkillDataKeys.ULTIMATE_METER);
                        float set_val = meter_val + 0.20f;
                        container.getDataManager().setDataSync(t0001SkillDataKeys.ULTIMATE_METER, set_val);
                    }
                }
            } catch (Exception ignored) {
            }

        });


    }


    @SubscribeEvent
    public static void onSlammingFallEvent(LivingDamageEvent.Pre event) {

        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        boolean isSlammingFall = entity.getTags().contains("SetToFallBoom");

        if (isSlammingFall && source == entity.damageSources().fall()) {
            float originalDamage = event.getOriginalDamage();
            float reducedDamage = originalDamage * 0.55f;
            //reduce damage for try and prevent aura loss
            event.setNewDamage(reducedDamage);


            LivingEntityPatch<?> opponent = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
            assert opponent != null;

            Vec3 slamPos = entity.position();


            BlockPos blockPos = BlockPos.containing(slamPos.x, slamPos.y - 0.1, slamPos.z);

            if (!LevelUtil.canTransferShockWave(entity.level(), blockPos, entity.level().getBlockState(blockPos))) {
                blockPos = blockPos.below();
            }

            Vec3 fracturePos = Vec3.atCenterOf(blockPos);

            double radius = 1.399D + (Objects.requireNonNull(entity.getAttribute(EpicFightAttributes.WEIGHT.getDelegate())).getValue()  * 0.02);   // radius of slam effect


                LevelUtil.circleSlamFracture(
                        entity,
                        entity.level(),
                        fracturePos,
                        radius,
                        true,
                        false,
                        false
                );

                BlockEffectCommand packet = new BlockEffectCommand();
                packet.setLocation(t0001.identifier("shockwave_fracture"));
                packet.setPos(blockPos);
                packet.setOffset(new Vec3(0D, 0.3D, 0D));
                packet.setRotation(Vec3.ZERO);
                packet.setScale(ReusableAnimEvents.NORMAL_SCALE);
                packet.setAllowMulti(true);
                packet.setForcedDeath(false);
                packet.setCheckState(false);

                PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, packet);




            opponent.applyStun(StunType.KNOCKDOWN, 4.0F);


            entity.level().playSound(
                    null,
                    entity.blockPosition(),
                    t0001Sounds.SLAM_SFX.get(),
                    entity.getSoundSource(),
                    1.0F,
                    1.0F
            );

            entity.removeTag("SetToFallBoom");
        }

    }

    //Might lag?
    @SubscribeEvent
    public static void TickEvents(ServerTickEvent.Post event) {

        DelayedTaskScheduler.tick(event.getServer());


        event.getServer().getAllLevels().forEach(a -> a.getEntities().getAll().forEach(
                entity -> {
                    {
                        if (entity.getTags().contains("playvideo")) {
                            if (entity instanceof ServerPlayer player) {
                                RPCPacketDistributor.rpcToPlayer(player, RpcPacketIds.SEND_VIDEO.id, "t0001:video/testvideo.webm", player.getId(), 1.0f);
                            }
                            entity.removeTag("playvideo");
                        }

                        if (entity.getTags().contains("quicktimetest") && entity instanceof ServerPlayer player) {
                            entity.removeTag("quicktimetest");

                            int keyCode = QuickTimeEventHandle.QuickTimeKeys.get(player.getRandom().nextInt(QuickTimeEventHandle.QuickTimeKeys.size()));

                            RPCPacketDistributor.rpcToPlayer(player, RpcPacketIds.SEND_QUICKTIME.id, keyCode, 30f, "test_quickTime");
                        }

                        if (entity.getTags().contains("texaf")) {
                            if (event.getServer().getTickCount() % 2 == 0) {
                                if (entity.level() instanceof ServerLevel level) {
                                    ChunkPos chunkPos = entity.chunkPosition();
                                    RPCPacketDistributor.rpcToTracking(level, chunkPos, RpcPacketIds.SEND_TEXTURED_AFTER_IMAGE.id, entity.getId());
                                }
                            }
                        }
                    }

                }
        ));

    }


    @SubscribeEvent
    public static void onServerStop(ServerStoppingEvent event) {
        DelayedTaskScheduler.clear();
    }

    //because doing delayed tick tasks in skill didn't work ♿
    public static class DelayedTaskScheduler {

        private static final List<ScheduledTask> PENDING = new ArrayList<>();

        private record ScheduledTask(int targetTick, Runnable task) {
        }

        public static void schedule(MinecraftServer server, int delayTicks, Runnable task) {
            int targetTick = server.getTickCount() + delayTicks;
            PENDING.add(new ScheduledTask(targetTick, task));
        }

        public static void tick(MinecraftServer server) {
            int currentTick = server.getTickCount();

            List<Runnable> due = new ArrayList<>();

            PENDING.removeIf(scheduled -> {
                if (currentTick >= scheduled.targetTick()) {
                    due.add(scheduled.task());
                    return true;
                }
                return false;
            });


            for (Runnable task : due) {
                task.run();
            }
        }

        public static void clear() {
            PENDING.clear(); // clear on server stop to avoid leaks
        }
    }


}
