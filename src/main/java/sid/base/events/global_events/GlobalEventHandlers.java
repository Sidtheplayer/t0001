package sid.base.events.global_events;


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
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import sid.base.gameasset.animations.UltimateAnimations;
import sid.base.gameasset.t0001Skills;
import sid.base.gameasset.t0001Sounds;
import sid.base.skill.t0001SkillDataKeys;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.utils.LevelUtil;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.damagesource.StunType;

import java.util.Objects;


@EventBusSubscriber(modid = "t0001")
public class GlobalEventHandlers {

    @SubscribeEvent
    public static void onSlammingFallEvent(LivingDamageEvent.Pre event) {

        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        boolean isSlammingFall = entity.getTags().contains("SetToFallBoom");

        if (isSlammingFall && source == entity.damageSources().fall()) {
            float originalDamage = event.getOriginalDamage();
            float reducedDamage = originalDamage * 0.55f;
            event.setNewDamage(reducedDamage);
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
                    3.0D * originalDamage,   // radius of slam effect
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
                        if (playerPatch != null && !playerPatch.getSkill(SkillSlots.IDENTITY).isEmpty() && playerPatch.getSkill(SkillSlots.IDENTITY).hasSkill(t0001Skills.FANG_COUNTER.get())) {
                            entity.removeTag("awaken");
                            playerPatch.getSkill(t0001Skills.FANG_COUNTER.get()).getDataManager().setDataSync(t0001SkillDataKeys.IS_AWAKENED,true);
                            boolean getval = playerPatch.getSkill(t0001Skills.FANG_COUNTER.get()).getDataManager().getDataValue(t0001SkillDataKeys.IS_AWAKENED);
                            player.server.getPlayerList().broadcastSystemMessage(
                                    Component.literal(player.getScoreboardName() + " has awakened " + getval)
                                            .withStyle(ChatFormatting.BOLD,ChatFormatting.DARK_RED),
                                    false
                            );

                            player.level().playSound(null, entity.blockPosition(),SoundEvents.WITHER_SPAWN, SoundSource.WEATHER);
                        }
                    }
                }
               }
        ));

    }

}
