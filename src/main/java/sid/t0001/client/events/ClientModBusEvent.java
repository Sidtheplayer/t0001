package sid.t0001.client.events;


import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xame.t0001;
import sid.t0001.client.model.AmogusModel;
import sid.t0001.client.model.darkness;
import sid.t0001.client.particle.BuzzHitParticle;
import sid.t0001.client.particle.PhotonSwingParticle;
import sid.t0001.client.particle.t0001Particle;
import sid.t0001.client.renderer.NAmogusRenderer;
import sid.t0001.client.renderer.NDarknessEntityRenderer;
import sid.t0001.gameasset.t0001Entities;
import sid.t0001.particle.t0001Particles;


@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid= t0001.MODID, value= Dist.CLIENT, bus= Mod.EventBusSubscriber.Bus.MOD)
public class ClientModBusEvent {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onParticleRegistry(final RegisterParticleProvidersEvent event) {

        event.registerSpriteSet(t0001Particles.BUZZ_HIT.get(), BuzzHitParticle.Provider::new);

        event.registerSpecial(t0001Particles.FAST_AFTERIMAGE.get(), new t0001Particle.FastWhiteAfterimageProvider());


        event.registerSpecial(t0001Particles.PHOTON_SWING_TRAIL.get(),new PhotonSwingParticle.Provider());
    }

    @SubscribeEvent
    public static void registerRenderersEvent(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(t0001Entities.AMOGUS.get(), NAmogusRenderer::new);
        event.registerEntityRenderer(t0001Entities.DARKNESS_ENTITY.get(), NDarknessEntityRenderer::new);
    }// register amogus vanilla renderer

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event  ){
        event.registerLayerDefinition(AmogusModel.LAYER_LOCATION, AmogusModel::createBodyLayer);
        event.registerLayerDefinition(darkness.LAYER_LOCATION, darkness::createBodyLayer);
    }//amogus model layer






}

