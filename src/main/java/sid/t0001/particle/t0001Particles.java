package sid.t0001.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.xame.t0001;


public class t0001Particles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, t0001.MODID);

    public static final RegistryObject<SimpleParticleType> FAST_AFTERIMAGE = PARTICLES.register("fast_afterimage", () -> new SimpleParticleType(true));



}
