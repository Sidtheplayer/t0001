package sid.base.client.particle;

import com.lowdragmc.photon.client.fx.BlockEffectExecutor;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXHelper;
import com.lowdragmc.photon.client.fx.FXRuntime;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import yesman.epicfight.registry.entries.EpicFightParticles;


public class BloodyCutParticle extends NoRenderParticle {


    protected BloodyCutParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);


        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        this.lifetime = 2;
        double d = 1.0F;


        BlockPos effectPos = new BlockPos((int) x, (int) y, (int) z);

        FX bloodyCutFX = FXHelper.getFX(ResourceLocation.parse("photon:bloodycutz"));
        FXRuntime runtime;
        if (bloodyCutFX != null) {
            runtime = StartFXandGetFxRuntime(bloodyCutFX, effectPos);
        }
        else throw new RuntimeException("bloody FX is null");

        if (runtime.root != null) {
            runtime.root.updatePos(new Vector3f((float) this.x, (float) this.y, (float) this.z));
            runtime.root.updateRotation(new Vector3f((float) 0 + (float)(this.x/2), (float) Math.asin(Math.random()),0));
        }

        for (int i = 0; i < 8; i++) {
            double particleMotionX = this.random.nextDouble() * d;
            d = d * (this.random.nextBoolean() ? 0.52D : -1.0D);
            double particleMotionZ = this.random.nextDouble() * d;
            d = d * (this.random.nextBoolean() ? 0.5D : -0.52D);
            this.level.addParticle(EpicFightParticles.BLOOD.get(), this.x, this.y - 0.5f, this.z, particleMotionX, this.random.nextDouble() * 0.25D, particleMotionZ);
        }
    }

    private @NotNull FXRuntime StartFXandGetFxRuntime(FX fxLocation, BlockPos effectPos) {
        BlockEffectExecutor BloodyCutEffect = new BlockEffectExecutor(fxLocation, this.level, effectPos);
        BloodyCutEffect.setScale(1.25, 1.25, 1.25);
        BloodyCutEffect.setOffset( this.x, this.y, this.z);
        BloodyCutEffect.setRotation(0, 0, 0);
        BloodyCutEffect.setAllowMulti(true);
        BloodyCutEffect.setCheckState(true);


        BloodyCutEffect.start();
        assert BloodyCutEffect.getRuntime() != null;
        return BloodyCutEffect.getRuntime();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {

        @Override
        public @NotNull Particle createParticle(@NotNull SimpleParticleType simpleParticleType, @NotNull ClientLevel clientLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new BloodyCutParticle(clientLevel,x,y,z);
        }


    }


}
