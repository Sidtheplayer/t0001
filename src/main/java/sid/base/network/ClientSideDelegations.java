package sid.base.network;

import com.lowdragmc.photon.client.fx.EntityEffectExecutor;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXHelper;
import com.lowdragmc.photon.client.fx.FXRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;
import sid.base.utils.ReusableAnimEvents;
import sid.base.world.entity.JunKunaiEntity;

public class ClientSideDelegations {

    public static void initJunKunaiClient(Entity entity){
        EntityEffectExecutor executor = new EntityEffectExecutor(
                FXHelper.getFX(ResourceLocation.parse("photon:jun_kunai")),
                entity.level(),
                entity,
                EntityEffectExecutor.AutoRotate.FORWARD
        );
        executor.setScale(1, 1, 1);
        executor.setRotation(0, 0, 0);
        executor.setOffset(0, 0, 0);
        executor.setForcedDeath(true);
        executor.setAllowMulti(false);
        executor.setDelay(0);
        executor.start();

        ReusableAnimEvents.putFXExec(entity.getId(), "photon:jun_kunai", executor);

        ReusableAnimEvents.putRuntime(entity.getId(), "photon:jun_kunai", executor.getRuntime());
    }

    public static void tickJunKunai(JunKunaiEntity entity) {
        if (entity.level().isClientSide) {

            FX fx = FXHelper.getFX(ResourceLocation.parse("photon:jun_kunai"));

            if (fx == null) return;

            FXRuntime cachedRuntime = ReusableAnimEvents.fxRuntimeTable.get(entity.getId(), "photon:jun_kunai");


            if (cachedRuntime == null || !cachedRuntime.isValid()) {

                cachedRuntime = fx.createRuntime();
                cachedRuntime.emit(ReusableAnimEvents.ifxExecutorTable.get(entity.getId(), "photon:jun_kunai"));

            } else if (!entity.isGrounded()) {

                var emitter = cachedRuntime.root;

                Quaternionf cachedRot = emitter.transform().rotation();

                float spin = (entity.getrOt() * 0.87f) % ((float) (Math.PI * 2.0));

                Quaternionf newRot = new Quaternionf(cachedRot).rotateY(spin);

                emitter.updateRotation(newRot);

                entity.setrOt(entity.getrOt() + 1);

            }

        }
    }


}
