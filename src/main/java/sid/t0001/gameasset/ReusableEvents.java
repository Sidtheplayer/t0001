package sid.t0001.gameasset;

import com.lowdragmc.photon.client.fx.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import net.minecraft.core.BlockPos;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.gameasset.Armatures;
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

    public class MyFxHelpers {
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


       /* public class WeaponFxHelper {

             // Attach a Photon FX to a weapon bone (TOOL_R, TOOL_L, etc).

            public static AnimationEvent.InTimeEvent weaponFX(ResourceLocation fxLoc, float startTime, String boneName) {
                return AnimationEvent.InTimeEvent.create(startTime, (entitypatch, self, params) -> {
                    LivingEntity entity = entitypatch.getOriginal();
                    FX fx = FXHelper.getFX(fxLoc);

                    if (fx != null) {
                        Armature armature = Armatures.BIPED.get();
                        Joint joint = armature.searchJointByName(boneName);

                        if (joint != null) {
                            // Create effect
                            EntityEffect effect = new EntityEffect(fx, entity.level(), entity, EntityEffect.AutoRotate.NONE);
                            effect.start();

                            // Update effect position each tick to follow the joint
                            entity.level().getEntity().execute(() -> {
                                if (FXEffect() != null && !effect.getRuntime().isAlive()) {
                                    // Calculate world pos of the joint
                                    // EpicFight provides helper to get world transform
                                    var pos = joint.getLocalTransform(entity).getTranslation();
                                    effect.getRuntime().fxData.();
                                }
                            });
                        }
                    }
                }, AnimationEvent.Side.CLIENT);
            } */
        }


    }





