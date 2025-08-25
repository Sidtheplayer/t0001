package sid.t0001.gameasset;

import com.lowdragmc.photon.client.fx.BlockEffect;
import com.lowdragmc.photon.client.fx.EntityEffect;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.particle.EpicFightParticles;

public class ReusableEvents {
   public static final AnimationEvent.E0 AFTER_IMAGE = (entitypatch, self, params) -> {
        LivingEntity entity = entitypatch.getOriginal();
        entity.level().addParticle(
                EpicFightParticles.WHITE_AFTERIMAGE.get(),
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                Double.longBitsToDouble(entity.getId()),
                0,
                0
        );


    };

    private static final FX RXS = FXHelper.getFX(new ResourceLocation("photon:fire"));
    
    public static class MyFxHelpers {
        // ----------------
        // ENTITY FX
        // ----------------
        public static AnimationEvent.InTimeEvent entityFX(ResourceLocation fxLoc, float startTime) {
            return AnimationEvent.InTimeEvent.create(startTime, (entitypatch, self, params) -> {
                LivingEntity entity = entitypatch.getOriginal();
                FX fx = FXHelper.getFX(fxLoc);

                if (fx != null) {
                    new EntityEffect(fx, entity.level(), entity, EntityEffect.AutoRotate.NONE).start();
                }
            }, AnimationEvent.Side.CLIENT);
        }

        /*public static List<AnimationEvent.InTimeEvent> entityFXWithStop(ResourceLocation fxLoc, float startTime, float endTime) {
            return Arrays.asList(
                    // Start event
                    AnimationEvent.InTimeEvent.create(startTime, (entitypatch, self, params) -> {
                        LivingEntity entity = entitypatch.getOriginal();
                        FX fx = FXHelper.getFX(fxLoc);

                        if (fx != null) {
                            EntityEffect effect = new EntityEffect(fx, entity.level(), entity, EntityEffect.AutoRotate.NONE);
                            effect.start();
                            // Store the effect instance on the params for later stop
                            self.getEventData().put("photon_fx", effect);
                        }
                    }, AnimationEvent.Side.CLIENT),

                    // Stop event
                    AnimationEvent.InTimeEvent.create(endTime, (entitypatch, self, params) -> {
                        Object obj = self.getEventData().get("photon_fx");
                        if (obj instanceof EntityEffect effect && effect.getRuntime() != null) {
                            effect.getRuntime().destroy(true);
                        }
                    }, AnimationEvent.Side.CLIENT)
            );
        }*/

        // ----------------
        // BLOCK FX
        // ----------------
        public static AnimationEvent.InTimeEvent blockFX(ResourceLocation fxLoc, float startTime) {
            return AnimationEvent.InTimeEvent.create(startTime, (entitypatch, self, params) -> {
                LivingEntity entity = entitypatch.getOriginal();
                FX fx = FXHelper.getFX(fxLoc);

                if (fx != null) {
                    BlockPos pos = entity.blockPosition();
                    new BlockEffect(fx, entity.level(), pos).start();
                }
            }, AnimationEvent.Side.CLIENT);
        }

        /*public static List<AnimationEvent.InTimeEvent> blockFXWithStop(ResourceLocation fxLoc, float startTime, float endTime) {
            return Arrays.asList(
                    // Start event
                    AnimationEvent.InTimeEvent.create(startTime, (entitypatch, self, params) -> {
                        LivingEntity entity = entitypatch.getOriginal();
                        FX fx = FXHelper.getFX(fxLoc);

                        if (fx != null) {
                            BlockPos pos = entity.blockPosition();
                            BlockEffect effect = new BlockEffect(fx, entity.level(), pos);
                            effect.start();
                            self.getEventData().put("photon_blockfx", effect);
                        }
                    }, AnimationEvent.Side.CLIENT),

                    // Stop event
                    AnimationEvent.InTimeEvent.create(endTime, (entitypatch, self, params) -> {
                        Object obj = self.getEventData().get("photon_blockfx");
                        if (obj instanceof BlockEffect effect && effect.getRuntime() != null) {
                            effect.getRuntime().destroy(true);
                        }
                    }, AnimationEvent.Side.CLIENT)
            );
        }*/
       /* public static class WeaponProxy {
            FX swordTrail = FXHelper.createProxyTrail(
                    new ResourceLocation("modid", "textures/fx/sword_trail.png"),
                    Armatures.BIPED.get().handR,
                    10, // trail length
                    0.2F // fade time
            );


        }*/



    }


}
