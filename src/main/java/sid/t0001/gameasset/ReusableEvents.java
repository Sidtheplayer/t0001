package sid.t0001.gameasset;

import com.lowdragmc.photon.client.fx.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import sid.t0001.particle.t0001Particles;

//its a big jungle

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

    //we use custom afterimage because lot of normal epicfight afterimages will crash my fucking pc and probably yours too

    public static final AnimationEvent.E0 FASTER_AFTERIMAGE = (entitypatch, self, params) -> {
        LivingEntity entity = entitypatch.getOriginal();
        entity.level().addParticle(
                t0001Particles.FAST_AFTERIMAGE.get(),
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                Double.longBitsToDouble(entity.getId()),
                0,
                0
        );


    };


    // this has no use but let it stay here
    @SuppressWarnings("removal")
    public static final FX RXS = FXHelper.getFX(new ResourceLocation("photon:fire"));

    // why did i even make this many
    @SuppressWarnings("rawtypes")
    public static class MyFxHelpers {

        // ENTITY FX
        // I am an imposter. 😔
        public static AnimationEvent.InTimeEvent entityFX(ResourceLocation fxLoc, float startTime) {
            return AnimationEvent.InTimeEvent.create(startTime, (entitypatch, self, params) -> {
                LivingEntity entity = entitypatch.getOriginal();
                FX fx = FXHelper.getFX(fxLoc);

                if (fx != null) {
                    new EntityEffect(fx, entity.level(), entity, EntityEffect.AutoRotate.NONE).start();
                }
            }, AnimationEvent.Side.CLIENT);
        }

        public static AnimationEvent.SimpleEvent entityFXS(ResourceLocation fxLoc) {
            return AnimationEvent.SimpleEvent.create((entitypatch, self, params) -> {
                LivingEntity entity = entitypatch.getOriginal();
                FX fx = FXHelper.getFX(fxLoc);

                if (fx != null) {
                    new EntityEffect(fx, entity.level(), entity, EntityEffect.AutoRotate.NONE).start();

                }
            }, AnimationEvent.Side.CLIENT);
        }

        public static AnimationEvent.SimpleEvent entityFXSL(ResourceLocation fxLoc) {
            return AnimationEvent.SimpleEvent.create((entitypatch, self, params) -> {
                LivingEntity entity = entitypatch.getOriginal();
                FX fx = FXHelper.getFX(fxLoc);

                if (fx != null) {
                    new EntityEffect(fx, entity.level(), entity, EntityEffect.AutoRotate.LOOK).start();
                }
            }, AnimationEvent.Side.CLIENT);
        }

        public static AnimationEvent.SimpleEvent entityFXSX(ResourceLocation fxLoc) {
            return AnimationEvent.SimpleEvent.create((entitypatch, self, params) -> {
                LivingEntity entity = entitypatch.getOriginal();
                FX fx = FXHelper.getFX(fxLoc);

                if (fx != null) {
                    new EntityEffect(fx, entity.level(), entity, EntityEffect.AutoRotate.XROT).start();
                }
            }, AnimationEvent.Side.CLIENT);
        }

        public static AnimationEvent.InTimeEvent entityFXattack(ResourceLocation fxLoc, float startTime) {
            return AnimationEvent.InTimeEvent.create(startTime, (entitypatch, self, params) -> {
                if (entitypatch.isLastAttackSuccess()) {
                    LivingEntity entity = entitypatch.getOriginal();
                    FX fx = FXHelper.getFX(fxLoc);

                    if (fx != null) {

                        EntityEffect effect1 = new EntityEffect(fx, entity.level(), entity, EntityEffect.AutoRotate.FORWARD);
                        effect1.setAllowMulti(true);
                        effect1.start();

                    }

                }
            }, AnimationEvent.Side.BOTH);
        }


        public static AnimationEvent.InTimeEvent entityFXtoolr(ResourceLocation fxLoc, float startTime) {
            return AnimationEvent.InTimeEvent.create(startTime, (entitypatch, self, params) -> {
                if (entitypatch.isLastAttackSuccess()) {
                    LivingEntity entity = entitypatch.getOriginal();

                    FX fx = FXHelper.getFX(fxLoc);

                    Vec3 BasePos = JointTrack.getJointWithTranslation((LocalPlayer) entity, entity, new Vec3f(0.1F, 0.2F, 0.3F), Armatures.BIPED.get().toolR);

                    if (fx != null) {

                        EntityEffect effect1 = new EntityEffect(fx, entity.level(), entity, EntityEffect.AutoRotate.FORWARD);
                        effect1.setAllowMulti(true);
                        effect1.setOffset(BasePos.x(), BasePos.y(), BasePos.z());
                        effect1.start();
                        System.out.println(BasePos.x() + BasePos.y() + BasePos.z());


                    }

                }
            }, AnimationEvent.Side.BOTH);
        }


        // BLOCK FX
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

        // thanks to yonchi for this code 😉

        public static class JointTrack {
            public static Vec3 getJointWithTranslation(LocalPlayer renderer, Entity ent, Vec3f translation, Joint joint) {
                if (renderer != null && ent != null && translation != null) {
                    if (renderer.level().isClientSide) {
                        LivingEntityPatch entitypatch = EpicFightCapabilities.getEntityPatch(ent, LivingEntityPatch.class);
                        if (entitypatch != null) {
                            float interpolation = 0.0F;
                            OpenMatrix4f transformMatrix;
                            transformMatrix = entitypatch.getArmature().getBoundTransformFor(entitypatch.getAnimator().getPose(interpolation), joint);
                            transformMatrix.translate(translation);
                            OpenMatrix4f.mul((new OpenMatrix4f()).rotate(-((float) Math.toRadians((double) (((LivingEntity) entitypatch.getOriginal()).yBodyRotO + 180.0F))), new Vec3f(0.0F, 1.0F, 0.0F)), transformMatrix, transformMatrix);
                            return new Vec3(
                                    (double) transformMatrix.m30 + (entitypatch.getOriginal()).getX(),
                                    (double) transformMatrix.m31 + ((entitypatch.getOriginal()).getY() + (ent.getBbHeight() / 1.8) - 1),
                                    (double) transformMatrix.m32 + (entitypatch.getOriginal()).getZ()
                            );
                        }
                    }
                }
                return null;
            }
        }

        /**
         * replaces item with another item on breakage, for now handle the detection of item breakage on your own
         *
         * @param entity    if you don't know this uninstall rn!!$
         * @param itemStack use .get().getdefaultinstance if you cant get itemstack(item) to work.
         * @param breaksound custom break sound
         */
        public static void handleBreak(LivingEntity entity, ItemStack itemStack, SoundEvent breaksound) {
            LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);

            SoundEvent sound = breaksound != null ? breaksound : EpicFightSounds.NEUTRALIZE_MOBS.get();

            if (patch != null) {
                patch.getOriginal().setItemInHand(InteractionHand.MAIN_HAND, itemStack);
                Level level = entity.level();
                level.playSound(
                        null,
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        sound,
                        SoundSource.PLAYERS,
                        1.0F,
                        0.75F
                );
                level.playSound(
                        null,
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        EpicFightSounds.CLASH.get(),
                        SoundSource.PLAYERS,
                        1.0F,
                        4.55F
                );
                //todo:add broken sword fragments flying effect
            }
        }


        /**
         * replaces item with another item on breakage, for now handle the detection of item breakage on your own
         *
         * @param entity    if you don't know this uninstall rn!!$
         * @param itemStack use .get().getdefaultinstance if you cant get itemstack(item) to work.
         *
         */
        public static void handleBreak(LivingEntity entity, ItemStack itemStack) {
            LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);

            SoundEvent sound = EpicFightSounds.NEUTRALIZE_MOBS.get();

            if (patch != null) {
                patch.getOriginal().setItemInHand(InteractionHand.MAIN_HAND, itemStack);
                Level level = entity.level();
                level.playSound(
                        null,
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        sound,
                        SoundSource.PLAYERS,
                        1.0F,
                        0.55F
                );
            }


        }


    }
}
