package sid.base.network;

import com.lowdragmc.photon.client.fx.BlockEffectExecutor;
import com.lowdragmc.photon.client.fx.EntityEffectExecutor;
import com.lowdragmc.photon.client.fx.FX;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import org.joml.Vector3f;
import org.watermedia.WaterMedia;
import sid.base.client.events.CameraAnimator;
import sid.base.main.t0001;
import sid.base.particle.t0001Particles;
import sid.base.utils.VideoRendererUtil;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class PacketDelegations {

    public static void triggeranomalouslightnin(int entityID, Vector3f entityPos){
        FX fx = t0001.getmodfx("white_lightning_ball");
        FX fx2 = t0001.getmodfx("electric_finish");
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (fx != null && level != null) {
            Entity entity = level.getEntity(entityID);
            if (entity != null && entity.onGround()) {
                EntityEffectExecutor lightning = new EntityEffectExecutor(fx, entity.level(), entity, EntityEffectExecutor.AutoRotate.NONE);
                lightning.setOffset(0.0D, -0.65D, 0.0D);
                lightning.setRotation(0, 0, 0);
                lightning.setScale(1, 1, 1);
                lightning.setDelay(0);
                lightning.setForcedDeath(false);
                lightning.setAllowMulti(true);
                lightning.start();
            }else if (fx2 != null){
                BlockEffectExecutor blockEffect = new BlockEffectExecutor(fx2,level,new BlockPos((int) entityPos.x, (int) entityPos.y, (int) entityPos.z));
                blockEffect.setOffset(0.0D,0.5D,0.0D);
                blockEffect.setRotation(0,0,0);
                blockEffect.setScale(1,1,1);
                blockEffect.setDelay(0);
                blockEffect.setForcedDeath(false);
                blockEffect.setAllowMulti(true);
                blockEffect.setCheckState(false);
                blockEffect.start();
            }
        }
    }


    public static void setSendTexturedAfterImage(int entityID) {
        Minecraft Mc = Minecraft.getInstance();
        if (Mc.level != null) {
            LivingEntityPatch<?> entityPatch = EpicFightCapabilities.getEntityPatch(Mc.level.getEntity(entityID), LivingEntityPatch.class);
            if (entityPatch == null) return;
            LivingEntity entity = entityPatch.getOriginal();

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

    public static void startVidOnClient(String videoLocation, int PlayerId, float speed){
        if(ModList.get().isLoaded(WaterMedia.ID)){
            VideoRendererUtil.playVideo(videoLocation, PlayerId,  speed);
        }
    }

    public static void startCamAnimOnClient(String AnimName,boolean loop, boolean lockCamera){
        CameraAnimator.getInstance().playWithOption(AnimName, loop, lockCamera);
    }


}
