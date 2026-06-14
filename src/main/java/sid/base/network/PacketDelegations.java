package sid.base.network;

import com.lowdragmc.photon.client.fx.BlockEffectExecutor;
import com.lowdragmc.photon.client.fx.EntityEffectExecutor;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3f;
import org.watermedia.WaterMedia;
import sid.base.client.events.CameraAnimator;
import sid.base.main.t0001;
import sid.base.particle.t0001Particles;
import sid.base.utils.ReusableAnimEvents;
import sid.base.utils.VideoRendererUtil;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class PacketDelegations {

    @SuppressWarnings("ExtractMethodRecommender")
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
                //recover decimal blockPos lost in transit
                float fracX = entityPos.x - (float)Math.floor(entityPos.x);
                float fracY = entityPos.y - (float)Math.floor(entityPos.y);
                float fracZ = entityPos.z - (float)Math.floor(entityPos.z);

                BlockEffectExecutor blockEffect = new BlockEffectExecutor(
                        fx2, level,
                        new BlockPos((int) Math.floor(entityPos.x), (int) Math.floor(entityPos.y), (int) Math.floor(entityPos.z))
                );
                blockEffect.setOffset(fracX, 0.5D + fracY, fracZ);
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

    @ApiStatus.Internal
    /// forcedeath, fxlocation, entityID || uses fxRuntimeTable won't work for other shit or the table fx id is overwritten
    public static void destroyFX(boolean forceDeath, String fxLocation, int id){
        FXRuntime toDestroy = ReusableAnimEvents.fxRuntimeTable.remove(id, fxLocation);
        if (toDestroy != null) {
            toDestroy.destroy(forceDeath);
        }
    }


}
