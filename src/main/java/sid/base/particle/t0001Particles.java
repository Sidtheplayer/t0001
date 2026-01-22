package sid.base.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import sid.base.main.t0001;
import yesman.epicfight.particle.HitParticleType;


public class t0001Particles {

    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, t0001.MODID);

    public static final DeferredHolder<ParticleType<?>, HitParticleType> BUZZ_HIT = PARTICLES.register("buzz_hit", () -> new HitParticleType(true, HitParticleType.RANDOM_WITHIN_BOUNDING_BOX, HitParticleType.CENTER_OF_TARGET));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FAST_AFTERIMAGE = PARTICLES.register("fast_afterimage", () -> new SimpleParticleType(true));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PHOTON_SWING_TRAIL = PARTICLES.register("photon_swing_trail", () -> new SimpleParticleType(true)); // this shit right here is waste of 4 hours of my time


}
