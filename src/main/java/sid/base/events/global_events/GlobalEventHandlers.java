package sid.base.events.global_events;


import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import sid.base.gameasset.t0001Skills;
import sid.base.gameasset.t0001Sounds;
import sid.base.network.ParryEffectPacket;
import sid.base.skill.t0001SkillDataKeys;
import sid.base.utils.RpcPacketIds;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.LevelUtil;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.damagesource.StunType;

@EventBusSubscriber(modid = "t0001")
public class GlobalEventHandlers {

    @SubscribeEvent
   public static void GlobalParryEvent(FMLCommonSetupEvent commonSetupEvent){
       EpicFightEventHooks.Entity.TAKE_DAMAGE_INCOME.registerContextAwareEvent(
               (event, eventContext) -> {

           if (event.getResult() != AttackResult.ResultType.BLOCKED) return;
           if(!(event.getEntityPatch().getOriginal() instanceof ServerPlayer player))return;

           // these parry effects will be made global for specific weapon_types/weapons soon
           Vec3 eye = player.getEyePosition();
           Vec3 view = player.getLookAngle().scale(1.95D); // prev: 1.45 dihh

           ParryEffectPacket packet = new ParryEffectPacket(
                   player.getStringUUID(),
                   event.isParried(),
                   eye.x + view.x,
                   eye.y + view.y + 0.75,
                   eye.z + view.z
           );

           //sendToPlayersTrackingEntityAndSelf is important otherwise fx won't play to you
           PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, packet);



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
                    3.0D + Math.round((double) entity.getHealth() * 1.6D),   // radius of slam effect
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
    public static void AwakenByTag(ServerTickEvent.Post event){
       event.getServer().getAllLevels().forEach(a -> a.getEntities().getAll().forEach(
               entity -> {
                {
                    boolean tg = entity.getTags().contains("awaken");
                    if(tg && entity instanceof ServerPlayer player){
                        PlayerPatch<?> playerPatch = EpicFightCapabilities.getPlayerPatch(player);
                        if(playerPatch == null)return;

                        if (!playerPatch.getSkill(SkillSlots.IDENTITY).isEmpty() && playerPatch.getSkill(SkillSlots.IDENTITY).hasSkill(t0001Skills.FANG_COUNTER.get())) {
                            entity.removeTag("awaken");
                            playerPatch.getSkill(t0001Skills.FANG_COUNTER.get()).getDataManager().setDataSync(t0001SkillDataKeys.IS_AWAKENED,true);
                            player.server.getPlayerList().broadcastSystemMessage(
                                    Component.literal( player.getScoreboardName() + " had a rude awakening")
                                            .withStyle(ChatFormatting.BOLD,ChatFormatting.DARK_RED),
                                    true
                            );

                            player.level().playSound(null, entity.blockPosition(),SoundEvents.WITHER_SPAWN, SoundSource.WEATHER);
                        }

                        if(!playerPatch.getSkill(SkillSlots.WEAPON_PASSIVE).isEmpty() && playerPatch.getSkill(SkillSlots.WEAPON_PASSIVE).getDataManager().hasData(t0001SkillDataKeys.IS_AWAKENED)){
                            playerPatch.getSkill(t0001Skills.DGSPASSIVE_SKILL.get()).getDataManager().setDataSync(t0001SkillDataKeys.IS_AWAKENED,true);
                            ServerPlayerPatch serverPlayerPatch = EpicFightCapabilities.getServerPlayerPatch(player);
                            entity.removeTag("awaken");
                            if (serverPlayerPatch != null) {
                                serverPlayerPatch.modifyLivingMotionByCurrentItem();
                            }
                        }
                    }


                    if(entity.getTags().contains("playvideo")){
                        if(entity instanceof ServerPlayer player){
                        RPCPacketDistributor.rpcToPlayer(player, RpcPacketIds.SEND_VIDEO.id,"t0001:video/testvideo.webm",player.getId(),1.0f);
                        }
                        entity.removeTag("playvideo");
                    }
                }

               }
        ));

    }



}
