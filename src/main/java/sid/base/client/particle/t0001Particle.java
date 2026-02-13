package sid.base.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.utils.EntitySnapshot;
import yesman.epicfight.client.particle.EntityAfterimageParticle;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.function.Consumer;


@OnlyIn(Dist.CLIENT)
public class t0001Particle extends EntityAfterimageParticle {



    public t0001Particle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, EntitySnapshot<?> entitySnapshot, Consumer<EntityAfterimageParticle> ticktask) {
        super(level, x, y, z, xd, yd, zd, entitySnapshot, ticktask);
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        this.alphaO = 0.5F;
        this.alpha = 0.5F;
        this.yawO = entitySnapshot.getYRot();
        this.yaw = entitySnapshot.getYRot();

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
            LivingEntityPatch<?> patch =
                    EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);

            if (patch == null) return null;

            EntitySnapshot<?> snapshot = patch.captureEntitySnapshot();
            if (snapshot == null) return null;


            t0001Particle particle =
                    new t0001Particle(
                            level, x, y, z,
                            xSpeed, ySpeed, zSpeed,
                            snapshot,
                            Particle -> {}
                    );

            particle.setLifetime(4);
            particle.rCol = 0.25F;
            particle.gCol = 0.25F;
            particle.bCol = 0.75F;
            particle.alpha = 0.75F;


            return particle;
        }

    }




}

