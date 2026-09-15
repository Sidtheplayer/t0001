package sid.base.client.particle;



import com.lowdragmc.photon.client.fx.FXHelper;
import com.lowdragmc.photon.client.gameobject.emitter.data.material.MaterialContext;
import com.lowdragmc.photon.client.gameobject.emitter.data.material.TextureMaterial;
import com.lowdragmc.photon.client.gameobject.emitter.particle.ParticleEmitter;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4f;
import sid.base.client.photon.fx.EFTrailExecutor;
import sid.base.main.t0001;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;
import java.util.Optional;

//MIT CODE FROM: https://github.com/dfdyz/EpicVFX

public class EmitterProxy {

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(@NotNull SimpleParticleType typeIn, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            int eid = (int)Double.doubleToRawLongBits(x);
            int animid = (int)Double.doubleToRawLongBits(z);
            int jointId = (int)Double.doubleToRawLongBits(xSpeed);
            int idx = (int)Double.doubleToRawLongBits(ySpeed);
            Entity entity = level.getEntity(eid);

            if (entity == null) {
                return null;
            }

            LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);

            if (entitypatch == null) {
                return null;
            }

            AnimationManager.AnimationAccessor<? extends StaticAnimation> animation =
                    AnimationManager.byId(animid);

            if (animation == null) {
                return null;
            }

            Optional<List<TrailInfo>> trailInfo = animation.get().getProperty(ClientAnimationProperties.TRAIL_EFFECT);

            if (trailInfo.isEmpty()) {
                return null;
            }

            TrailInfo result = trailInfo.get().get(idx);

            if (result.hand() != null) {
                ItemStack stack = entitypatch.getOriginal().getItemInHand(result.hand());
                RenderItemBase renderItemBase = RenderEngine.getInstance().getItemRenderer(stack);

                if (renderItemBase != null && renderItemBase.trailInfo() != null) {
                    result = renderItemBase.trailInfo().overwrite(result);
                }

            }

            //result = entitypatch.getEntityDecorations().getModifiedTrailInfo(result, result.hand() == null ? CapabilityItem.EMPTY : entitypatch.getAdvancedHoldingItemCapability(result.hand()));

            if (result.playable()) {
                var jt = entitypatch.getArmature().searchJointById(jointId);

                if (entitypatch.isMirrorMode()) {
                    Joint mirroredJoint = entitypatch.getArmature().searchJointByName(yesman.epicfight.api.animation.PoseMirror.mirrorJointName(jt.getName()));
                    if (mirroredJoint != null) jt = mirroredJoint;
                }

                var fx = FXHelper.getFX(result.texturePath());
                var backUpFx = FXHelper.getFX(t0001.identifier("backup_trail"));
                if(fx == null){
                    t0001.LOGGER.error("No FX named: {}", result.texturePath() + "Trying Reverting to fallback Default");
                    if(backUpFx != null){
                        var exe = new EFTrailExecutor(backUpFx, level,
                                entitypatch, jt, animation, result
                        );
                        exe.start();

                        if (exe.getRuntime() != null) {
                            var object = exe.getRuntime().findObject("trail");
                            if(object instanceof ParticleEmitter emitter){
                                TextureMaterial material = new TextureMaterial(result.texturePath());
                                material.setHdrMode(TextureMaterial.HDRMode.MULTIPLICATIVE);
                                material.setHdr(new Vector4f(1f));
                                material.setDiscardThreshold(0.001f);
                                material.setupUniform(MaterialContext.ARA_TRAIL_INSTANCE);
                                emitter.config.renderer.getMaterials().getFirst().setMaterial(
                                        material
                                );
                            }
                        }
                    }
                    return null;
                }

                var exe = new EFTrailExecutor(fx, level,
                        entitypatch, jt, animation, result
                );
                exe.start();

            }
            return null;
        }
    }




}