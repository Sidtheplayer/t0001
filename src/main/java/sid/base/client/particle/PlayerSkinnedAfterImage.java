package sid.base.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.utils.EntitySnapshot;
import yesman.epicfight.client.particle.EntityAfterimageParticle;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.function.Consumer;


@OnlyIn(Dist.CLIENT)
public class PlayerSkinnedAfterImage extends EntityAfterimageParticle {

    private static ResourceLocation getEntityTexture(Entity entity) {
        return entity instanceof AbstractClientPlayer
                ? ((AbstractClientPlayer) entity).getSkin().texture()
                : Minecraft.getInstance()
                .getEntityRenderDispatcher()
                .getRenderer(entity)
                .getTextureLocation(entity);
    }

     final Entity entity;


    public PlayerSkinnedAfterImage(ClientLevel level, double x, double y, double z,
                                   double xd, double yd, double zd,
                                   EntitySnapshot<?> entitySnapshot,
                                   Consumer<EntityAfterimageParticle> ticktask,
                                   Entity entity) {

        super(level, x, y, z, xd, yd, zd, entitySnapshot, ticktask);

        this.entity = entity;
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        this.alphaO = 1.0F;
        this.alpha = 1.0F;
        this.yawO = entitySnapshot.getYRot();
        this.yaw = entitySnapshot.getYRot();
    }


    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float partialTick) {
        float alpha = Mth.lerp(partialTick, this.alphaO, this.alpha);
        int lightColor = this.getLightColor(partialTick);

        PoseStack poseStack = new PoseStack();
        this.setupPoseStack(poseStack, camera, partialTick);

        MultiBufferSource.BufferSource buffers =
                Minecraft.getInstance().renderBuffers().bufferSource();

        ResourceLocation skin = getEntityTexture(this.entity);

        this.entitySnapshot.renderTextured(poseStack, buffers,
                texture -> RenderType.entityTranslucent(skin),
                Mesh.DrawingFunction.NEW_ENTITY,
                lightColor, this.rCol, this.gCol, this.bCol, alpha);

        this.entitySnapshot.renderItems(poseStack, buffers,
                EpicFightRenderTypes.itemAfterimageTranslucent(),
                Mesh.DrawingFunction.NEW_ENTITY,
                lightColor, alpha);

        buffers.endLastBatch();
        this.revert(poseStack);
    }

    @Override
    public void tick() {
        super.tick();
        this.alpha = (float)(this.lifetime - this.age) / this.lifetime;
    }


    public static class T0001WhiteAfterimageProvider
            implements ParticleProvider<SimpleParticleType> {

        @Override
        public Particle createParticle(
                @NotNull SimpleParticleType type,
                ClientLevel level,
                double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed
        ) {
            Entity entity = level.getEntity((int)Double.doubleToLongBits(xSpeed));
            LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);

            if (patch == null) return null;

            EntitySnapshot<?> snapshot = patch.captureEntitySnapshot();
            if (snapshot == null) return null;


            PlayerSkinnedAfterImage particle =
                    new PlayerSkinnedAfterImage(
                            level, x, y, z,
                            xSpeed, ySpeed, zSpeed,
                            snapshot,
                            p -> {
                            },
                            entity
                    );

            particle.setLifetime(6);

            return particle;
        }

    }





}

