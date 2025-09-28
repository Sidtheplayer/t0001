package sid.t0001.client.events;


import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xame.t0001;
import sid.t0001.client.particle.t0001Particle;
import sid.t0001.particle.t0001Particles;


@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid= t0001.MODID, value= Dist.CLIENT, bus= Mod.EventBusSubscriber.Bus.MOD)
public class ClientModBusEvent {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onParticleRegistry(final RegisterParticleProvidersEvent event) {

        event.registerSpecial(t0001Particles.FAST_AFTERIMAGE.get(), new t0001Particle.FastWhiteAfterimageProvider());



    }
}
