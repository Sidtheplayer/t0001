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

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TEX_AFTERIMAGE = PARTICLES.register("tex_afterimage", () -> new SimpleParticleType(true));

    public static final DeferredHolder<ParticleType<?>, HitParticleType> BLOODY_CUT = PARTICLES.register("bloody_cut",()-> new HitParticleType(true,HitParticleType.RANDOM_WITHIN_BOUNDING_BOX, HitParticleType.CENTER_OF_TARGET));

    public static final DeferredHolder<ParticleType<?>, HitParticleType> BLOODY_CUT_NORMAL = PARTICLES.register("bloody_cut_normal",()-> new HitParticleType(true,HitParticleType.RANDOM_WITHIN_BOUNDING_BOX, HitParticleType.CENTER_OF_TARGET));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> VFX_PROXY =
            PARTICLES.register("trail_proxy", () -> {
                return new SimpleParticleType(true);
            });


}
