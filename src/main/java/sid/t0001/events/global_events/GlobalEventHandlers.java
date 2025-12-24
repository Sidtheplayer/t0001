package sid.t0001.events.global_events;


import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import sid.t0001.gameasset.t0001Sounds;
import yesman.epicfight.api.utils.LevelUtil;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.StunType;


@Mod.EventBusSubscriber(modid = "t0001", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GlobalEventHandlers {

    @SubscribeEvent
    public static void onSlammingFallEvent(LivingHurtEvent event) {

        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        boolean isSlammingFall = entity.getTags().contains("SetToFallBoom");

        if (isSlammingFall && source == entity.damageSources().fall()) {
            float originalDamage = event.getAmount();
            float reducedDamage = originalDamage * 0.55f;
            event.setAmount(reducedDamage);
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
                    3.0D,   // radius of slam effect
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
}
