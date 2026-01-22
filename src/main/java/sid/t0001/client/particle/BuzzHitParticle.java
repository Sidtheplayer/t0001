package sid.t0001.client.particle;


import com.lowdragmc.photon.client.fx.BlockEffectExecutor;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXHelper;
import com.lowdragmc.photon.client.fx.FXRuntime;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import yesman.epicfight.client.particle.HitParticle;
import yesman.epicfight.registry.entries.EpicFightParticles;


@OnlyIn(Dist.CLIENT)
public class BuzzHitParticle extends HitParticle {

    public BuzzHitParticle(ClientLevel world, double x, double y, double z, double argX, double argY, double argZ, SpriteSet animatedSprite) {
        super(world, x, y, z, animatedSprite);

        this.xd = argX;
        this.yd = argY;
        this.zd = argZ;

        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        this.quadSize = 1.0F;
        this.lifetime = 2;
        double d = 1.0F;


        BlockPos effectPos = new BlockPos((int) x, (int) y, (int) z);

        FX hitParryFX = FXHelper.getFX(ResourceLocation.parse("photon:buzzhit"));
        FXRuntime runtime;
        if (hitParryFX != null) {
            runtime = StartFXandGetFxRuntime(hitParryFX, effectPos);
        }
        else throw new RuntimeException("buzzhitparticle FX is null");

        if (runtime.root != null) {
            runtime.root.updatePos(new Vector3f((float) this.x, (float) this.y, (float) this.z));
            runtime.root.updateRotation(new Vector3f((float) 0 + (float)(this.x/2), (float) Math.asin(Math.random()),0));
        }

        for (int i = 0; i < 8; i++) {
            double particleMotionX = this.random.nextDouble() * d;
            d = d * (this.random.nextBoolean() ? 1.0D : -1.0D);
            double particleMotionZ = this.random.nextDouble() * d;
            d = d * (this.random.nextBoolean() ? 1.0D : -1.0D);
            this.level.addParticle(EpicFightParticles.BLOOD.get(), this.x, this.y, this.z, particleMotionX, this.random.nextDouble() * 0.5D, particleMotionZ);
        }
    }

    private @NotNull FXRuntime StartFXandGetFxRuntime(FX hitParryFX, BlockPos effectPos) {
        BlockEffectExecutor BuzzHitEffect = new BlockEffectExecutor(hitParryFX, this.level, effectPos);
        BuzzHitEffect.setScale(0.75, 0.75, 0.75);
        BuzzHitEffect.setOffset(-0.5 + this.x, -0.5 +this.y, -0.5 + this.z); //subtract -0.5 to account for 0.5 offset minecraft normally puts
        BuzzHitEffect.setRotation(0, 0, 0);
        BuzzHitEffect.setAllowMulti(true);
        BuzzHitEffect.setCheckState(true);


        BuzzHitEffect.start();
        assert BuzzHitEffect.getRuntime() != null;
        return BuzzHitEffect.getRuntime();
    }

    @OnlyIn(Dist.CLIENT)
        public record Provider(SpriteSet spriteSet) implements ParticleProvider<SimpleParticleType> {

        @Override
            public Particle createParticle(@NotNull SimpleParticleType typeIn, @NotNull ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
                return new BuzzHitParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
            }
        }
}
