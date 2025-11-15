package sid.t0001.client.particle;

import java.util.List;
import java.util.Optional;

import com.lowdragmc.photon.client.fx.*;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import com.google.common.collect.Lists;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.client.particle.AbstractTrailParticle;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

// Photon imports
import com.lowdragmc.photon.client.gameobject.IFXObject;

@OnlyIn(Dist.CLIENT)
public class PhotonSwingParticle extends AbstractTrailParticle<LivingEntityPatch<?>> {
    protected final AssetAccessor<? extends StaticAnimation> animation;
    protected final List<IFXObject> fxObjects;

    private final EntityEffect blockEffect;
    private final Joint joint;
    private FXRuntime runtime;

    protected PhotonSwingParticle(ClientLevel level, LivingEntityPatch<?> owner, Joint joint, AssetAccessor<? extends StaticAnimation> animation, TrailInfo trailInfo) {
        super(level, owner, trailInfo);

        this.joint = joint;
        this.animation = animation;
        this.fxObjects = Lists.newArrayList();

        ResourceLocation fxRes = null;
        try {
            if (this.trailInfo.texturePath() != null) {
                fxRes = ResourceLocation.tryParse(String.valueOf(this.trailInfo.texturePath()));
            }
        } catch (Throwable ignored) {
        }

        if (fxRes == null) {
            fxRes = new ResourceLocation("photon:firetrail");
        }

        FX fx = FXHelper.getFX(fxRes);

        blockEffect = new EntityEffect(fx, this.level, owner.getOriginal(), EntityEffect.AutoRotate.NONE);
        blockEffect.setScale(1, 1, 1);
        blockEffect.setOffset(-0.5, -0.5, -0.5);
        blockEffect.setRotation(0, 0, 0);
        blockEffect.setDelay(1);
        blockEffect.setAllowMulti(true);
        blockEffect.setForcedDeath(true);

        blockEffect.start();
        this.runtime = blockEffect.getRuntime();

        if (this.runtime != null && this.runtime.root != null) {
            this.runtime.root.updatePos(new Vector3f((float) this.x, (float) this.y, (float) this.z));
            this.fxObjects.addAll(this.runtime.fxData.objects());
        }
    }

    @Deprecated /** This constructor is only for Model Previewer **/
    protected PhotonSwingParticle(yesman.epicfight.api.model.Armature armature, LivingEntityPatch<?> owner, Joint joint, AssetAccessor<? extends StaticAnimation> animation, TrailInfo trailInfo) {
        super(owner, trailInfo);

        this.joint = joint;
        this.animation = animation;
        this.fxObjects = Lists.newArrayList();

        // In Model Previewer context, we don't have a ClientLevel, so blockEffect will be null
        this.blockEffect = null;
        this.runtime = null;
    }

    private net.minecraft.core.BlockPos getBlockPos() {
        return net.minecraft.core.BlockPos.containing(this.x, this.y, this.z);
    }

    private Vector3f getVector3f() {
        return new Vector3f((float) this.x, (float) this.y, (float) this.z);
    }

    @Override
    protected boolean canContinue() {
        return this.owner.getOriginal().isAlive();
    }

    @Override
    protected boolean canCreateNextCurve() {
        return false;
    }

    @Override
    protected void createNextCurve() {
    }

    @Override
    public void tick() {
        super.tick();

        // Update particle position to follow the joint in real-time
        if (this.joint != null && this.owner != null && this.owner.getOriginal() != null) {
            try {
                // Get current pose and model matrix
                yesman.epicfight.api.animation.Pose currentPose = this.owner.getAnimator().getPose(1.0F);
                net.minecraft.world.phys.Vec3 entityPos = this.owner.getOriginal().getPosition(1.0F);

                // Build the model transformation matrix
                yesman.epicfight.api.utils.math.OpenMatrix4f modelTf =
                        yesman.epicfight.api.utils.math.OpenMatrix4f.createTranslation(
                                        (float)entityPos.x, (float)entityPos.y, (float)entityPos.z)
                                .rotateDeg(180.0F, yesman.epicfight.api.utils.math.Vec3f.Y_AXIS)
                                .mulBack(this.owner.getModelMatrix(1.0F));

                // Get joint transformation
                yesman.epicfight.api.utils.math.OpenMatrix4f jointTf =
                        this.owner.getArmature().getBoundTransformFor(currentPose, this.joint).mulFront(modelTf);

                // Calculate world position of the joint center (using trail start/end midpoint)
                net.minecraft.world.phys.Vec3 startPos = yesman.epicfight.api.utils.math.OpenMatrix4f.transform(
                        jointTf, this.trailInfo.start());
                net.minecraft.world.phys.Vec3 endPos = yesman.epicfight.api.utils.math.OpenMatrix4f.transform(
                        jointTf, this.trailInfo.end());

                // Update particle position to joint center
                this.x = (startPos.x + endPos.x) / 2.0;
                this.y = (startPos.y + endPos.y) / 2.0;
                this.z = (startPos.z + endPos.z) / 2.0;
            } catch (Throwable e) {
                // If joint tracking fails, fall back to current position
            }
        }

        // Only update Photon FX if we're not in Model Previewer context
        if (this.blockEffect != null) {
            if (this.runtime == null) {
                this.runtime = this.blockEffect.getRuntime();
            }

            Vector3f fxPos = getVector3f();

            if (this.runtime != null && this.runtime.root != null) {
                this.runtime.root.updatePos(fxPos);
            } else {
                try {
                    this.blockEffect.setOffset(fxPos.x - Math.floor(fxPos.x), fxPos.y - Math.floor(fxPos.y), fxPos.z - Math.floor(fxPos.z));
                } catch (Throwable ignored) {}
            }
        }

        if (this.owner != null) {
            assert this.owner.getOriginal() != null;
            if (!this.owner.getOriginal().isAlive() || this.age > this.lifetime) {
                this.remove();
            }
        }

        if (this.removed && this.blockEffect != null) {
            try {
                if (this.runtime != null) {
                    this.runtime.destroy(true);
                }
            } catch (Throwable ignored) {}

            try {
                this.blockEffect.setForcedDeath(true);
            } catch (Throwable ignored) {}
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {

        @Override
        public Particle createParticle(@NotNull SimpleParticleType typeIn, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            int eid = (int) Double.doubleToRawLongBits(x);
            int animid = (int) Double.doubleToRawLongBits(z);
            int jointId = (int) Double.doubleToRawLongBits(xSpeed);
            int idx = (int) Double.doubleToRawLongBits(ySpeed);
            Entity entity = level.getEntity(eid);

            if (entity == null) {
                return null;
            }

            LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);

            if (entitypatch == null) {
                return null;
            }

            AnimationAccessor<? extends StaticAnimation> animation = AnimationManager.byId(animid);

            if (animation == null) {
                return null;
            }

            Optional<List<TrailInfo>> trailInfo = animation.get().getProperty(ClientAnimationProperties.TRAIL_EFFECT);

            if (trailInfo.isEmpty()) {
                return null;
            }

            TrailInfo result = trailInfo.get().get(idx);

            if (result.hand() != null) {
                try {
                    yesman.epicfight.world.capabilities.item.CapabilityItem cap = null;
                } catch (Throwable ignored) {}
            }

            result = entitypatch.getEntityDecorations().getModifiedTrailInfo(result, result.hand() == null ? yesman.epicfight.world.capabilities.item.CapabilityItem.EMPTY : entitypatch.getAdvancedHoldingItemCapability(result.hand()));

            if (!result.playable()) {
                return null;
            }

            // FIX: Add null check for joint
            Joint joint = entitypatch.getArmature().searchJointById(jointId);
            if (joint == null) {
                // Log error or return null if joint is critical
                System.err.println("PhotonSwingParticle: Joint with ID " + jointId + " not found in armature!");
                return null;
            }

            return new PhotonSwingParticle(level, entitypatch, joint, animation, result);
        }
    }
}