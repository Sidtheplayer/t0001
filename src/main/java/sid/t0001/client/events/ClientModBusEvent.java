package sid.t0001.client.events;



import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import sid.t0001.client.model.AmogusModel;
import sid.t0001.client.model.darkness;
import sid.t0001.client.particle.BuzzHitParticle;
import sid.t0001.client.particle.PhotonSwingParticle;
import sid.t0001.client.particle.t0001Particle;
import sid.t0001.client.renderer.NAmogusRenderer;
import sid.t0001.client.renderer.NDarknessEntityRenderer;
import sid.t0001.gameasset.t0001Entities;
import sid.t0001.main.t0001;
import sid.t0001.particle.t0001Particles;


@EventBusSubscriber(modid= t0001.MODID, value= Dist.CLIENT)
public class ClientModBusEvent {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onParticleRegistry(final RegisterParticleProvidersEvent event) {

        event.registerSpriteSet(t0001Particles.BUZZ_HIT.get(), BuzzHitParticle.Provider::new);

        event.registerSpecial(t0001Particles.FAST_AFTERIMAGE.get(), new t0001Particle.T0001WhiteAfterimageProvider());

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

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event){
     RenderEngine.init();
    }






}

