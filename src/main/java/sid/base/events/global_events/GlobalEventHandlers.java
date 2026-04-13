package sid.base.events.global_events;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import sid.base.gameasset.t0001Skills;
import sid.base.gameasset.t0001Sounds;
import sid.base.network.ParryEffectPacket;
import sid.base.particle.t0001Particles;
import sid.base.skill.t0001SkillDataKeys;
import sid.base.utils.RpcPacketIds;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.LevelUtil;
import yesman.epicfight.registry.entries.EpicFightAttributes;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.damagesource.StunType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@EventBusSubscriber(modid = "t0001")
public class GlobalEventHandlers {

    @SubscribeEvent
    public static void GlobalParryEvent(FMLCommonSetupEvent commonSetupEvent) {
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
                    PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, packet);


                });

        EpicFightEventHooks.Player.TICK_EPICFIGHT_MODE.registerContextAwareEvent(((tickPlayerEpicFightModeEvent, eventContext) ->
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
            } catch (Exception ignored) {
            }

        }
        ));



    }


    @SubscribeEvent
    public static void onSlammingFallEvent(LivingDamageEvent.Pre event) {

        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        boolean isSlammingFall = entity.getTags().contains("SetToFallBoom");

        if (isSlammingFall && source == entity.damageSources().fall()) {
            float originalDamage = event.getOriginalDamage();
            float reducedDamage = originalDamage * 0.55f;
            event.setNewDamage(reducedDamage);

            //to  be replaced with photon 2 effect
            entity.level().addParticle(
                    EpicFightParticles.GROUND_SLAM.get(),
                    entity.getX(), entity.getY(), entity.getZ(),
                    Double.longBitsToDouble(entity.getId()), 2, 2
            );

            LivingEntityPatch<?> opponent = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
            assert opponent != null;

            Vec3 slamPos = entity.position();

            BlockPos blockPos = BlockPos.containing(slamPos.x, slamPos.y - 0.1, slamPos.z);

            if (!LevelUtil.canTransferShockWave(entity.level(), blockPos, entity.level().getBlockState(blockPos))) {
                blockPos = blockPos.below();
            }

            Vec3 fracturePos = Vec3.atCenterOf(blockPos);

            LevelUtil.circleSlamFracture(
                    entity,
                    entity.level(),
                    fracturePos,
                    1.399D + (Objects.requireNonNull(entity.getAttribute(EpicFightAttributes.WEIGHT.getDelegate())).getValue() * 0.03),   // radius of slam effect
                    true,
                    false
            );


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


        event.getServer().getAllLevels().forEach(level ->
                level.getEntities().getAll().forEach(entity -> {
                    if (!entity.getTags().contains("awaken")) return;
                    if (!(entity instanceof ServerPlayer player)) return;

                    ServerPlayerPatch playerPatch = EpicFightCapabilities.getServerPlayerPatch(player);
                    if (playerPatch == null) return;

                    entity.removeTag("awaken"); // Remove once, up front

                    // Identity skill check
                    if (!playerPatch.getSkill(SkillSlots.IDENTITY).isEmpty()
                            && playerPatch.getSkill(SkillSlots.IDENTITY).hasSkill(t0001Skills.FANG_COUNTER.value())) {

                        playerPatch.getSkill(t0001Skills.FANG_COUNTER.get())
                                .getDataManager().setDataSync(t0001SkillDataKeys.IS_AWAKENED, true);

                        event.getServer().getPlayerList().broadcastSystemMessage(
                                Component.literal(player.getScoreboardName() + " had a rude awakening")
                                        .withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED),
                                true
                        );
                        player.level().playSound(null, entity.blockPosition(),
                                SoundEvents.WITHER_SPAWN, SoundSource.WEATHER, 1.0f, 1.0f);
                    }

                    // Weapon passive check — fix: check for the skill, not for IS_AWAKENED on a different manager
                    if (!playerPatch.getSkill(SkillSlots.WEAPON_PASSIVE).isEmpty()
                            && playerPatch.getSkill(SkillSlots.WEAPON_PASSIVE).hasSkill(t0001Skills.DGSPASSIVE_SKILL.value())) {

                        playerPatch.getSkill(t0001Skills.DGSPASSIVE_SKILL.get())
                                .getDataManager().setDataSync(t0001SkillDataKeys.IS_AWAKENED, true);
                        playerPatch.modifyLivingMotionByCurrentItem();
                    }

                })
        );


        event.getServer().getAllLevels().forEach(a -> a.getEntities().getAll().forEach(
                entity -> {
                    {
                        if (entity.getTags().contains("playvideo")) {
                            if (entity instanceof ServerPlayer player) {
                                RPCPacketDistributor.rpcToPlayer(player, RpcPacketIds.SEND_VIDEO.id, "t0001:video/testvideo.webm", player.getId(), 1.0f);
                            }
                            entity.removeTag("playvideo");
                        }

                        if (entity.getTags().contains("texaf")) {
                            if (event.getServer().getTickCount() % 2 == 0) {
                                if (entity.level() instanceof ServerLevel level) {
                                    ChunkPos chunkPos = entity.chunkPosition();
                                    RPCPacketDistributor.rpcToTracking(level, chunkPos, RpcPacketIds.SEND_TEXTURED_AFTER_IMAGE.id,entity.getId());
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

        private record ScheduledTask(int targetTick, Runnable task) {}

        public static void schedule(MinecraftServer server, int delayTicks, Runnable task) {
            int targetTick = server.getTickCount() + delayTicks;
            PENDING.add(new ScheduledTask(targetTick, task));
        }

        public static void tick(MinecraftServer server) {
            int currentTick = server.getTickCount();
            PENDING.removeIf(scheduled -> {
                if (currentTick >= scheduled.targetTick()) {
                    scheduled.task().run();
                    return true;
                }
                return false;
            });
        }

        public static void clear() {
            PENDING.clear(); // clear on server stop to avoid leaks
        }
    }


}
