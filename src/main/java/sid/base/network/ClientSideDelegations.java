package sid.base.network;

import com.lowdragmc.photon.client.fx.EntityEffectExecutor;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXHelper;
import com.lowdragmc.photon.client.fx.FXRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import sid.base.events.QuickTimeEventHandle;
import sid.base.utils.ReusableAnimEvents;
import sid.base.world.entity.JunKunaiEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientSideDelegations {


    public static void addQuickTime(int KeyMapCode, Float Time, String QuickTimeContext) {
        QuickTimeEventHandle.addQuickTime(new QuickTimeEventHandle.QuickTimeData(KeyMapCode, Time, QuickTimeContext));
    }

    public static void initJunKunaiClient(Entity entity) {
        EntityEffectExecutor executor = new EntityEffectExecutor(
                FXHelper.getFX(ResourceLocation.parse("photon:jun_kunai")),
                entity.level(),
                entity,
                EntityEffectExecutor.AutoRotate.NONE
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

    //there maybe meeny tiny leak, but I don't want to fix it
    public static final Map<Entity, Quaternionf> kunaiRotMap = new ConcurrentHashMap<>();

    private static final float RAD_PER_TICK = 0.15f * 7f;

    private static final float TWO_PI = (float) (Math.PI * 2.0);

    private static final Vector3f LONG_AXIS = new Vector3f(1f, 0f, 0f);



    public static void tickJunKunai(JunKunaiEntity entity, float partialTick) {
        if (!entity.level().isClientSide) return;

        final String fxId = "photon:jun_kunai";
        final ResourceLocation fxLoc = ResourceLocation.parse(fxId);

        FX fx = FXHelper.getFX(fxLoc);
        if (fx == null) return;


        FXRuntime runtime = ReusableAnimEvents.fxRuntimeTable.get(entity.getId(), fxId);
        if (runtime == null || !runtime.isValid()) {
            runtime = fx.createRuntime();
            runtime.emit(ReusableAnimEvents.ifxExecutorTable.get(entity.getId(), fxId));
            if (!runtime.isValid()) return;
            // new runtime >>> any cached base is stale
            kunaiRotMap.remove(entity);
        }

        var emitter = runtime.findObject("kunai");
        if (emitter == null) return;


        Quaternionf base = kunaiRotMap.get(entity);

        if (base == null) {
            base = new Quaternionf(emitter.transform().localRotation());
            kunaiRotMap.put(entity, base);
        }


        if (entity.isGrounded()) return;


        float yawRad   = Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) * Mth.DEG_TO_RAD;
        float pitchRad = Mth.lerp(partialTick, entity.xRotO, entity.getXRot()) * Mth.DEG_TO_RAD;

        float cp = Mth.cos(pitchRad), sp = Mth.sin(pitchRad);
        float cy = Mth.cos(yawRad),   sy = Mth.sin(yawRad);

        Vector3f velocityDir = new Vector3f(cp * sy, sp, cp * cy);

        //  if for some reason yaw/pitch are zero (just-spawned frame) then fall back
        //  to the actual delta movement so the first rendered frame already points correctly
        if (velocityDir.lengthSquared() < 1e-8f) {
            Vec3 v = entity.getDeltaMovement();
            if (v.lengthSqr() > 1e-8) {
                velocityDir.set((float) v.x, (float) v.y, (float) v.z).normalize();
            } else {
                velocityDir.set(0f, 0f, 1f);
            }
        }



        float t    = entity.tickCount + partialTick;
        float spin = (t * RAD_PER_TICK) % TWO_PI;
        if (spin < 0f) spin += TWO_PI;


        Vector3f restAxis = new Vector3f(LONG_AXIS).rotate(base);

        Quaternionf align = new Quaternionf().rotationTo(restAxis, velocityDir);

        Quaternionf spinQ = new Quaternionf()
                .rotateAxis(spin, LONG_AXIS.x, LONG_AXIS.y, LONG_AXIS.z);


        Quaternionf newRot = new Quaternionf(align)
                .mul(base)
                .mul(spinQ);

        emitter.updateRotation(newRot);
    }

}
