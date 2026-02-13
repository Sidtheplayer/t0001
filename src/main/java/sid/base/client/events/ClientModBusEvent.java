package sid.base.client.events;



import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import sid.base.client.model.AmogusModel;
import sid.base.client.model.darkness;
import sid.base.client.particle.BuzzHitParticle;
import sid.base.client.particle.PhotonSwingParticle;
import sid.base.client.particle.t0001Particle;
import sid.base.client.renderer.NAmogusRenderer;
import sid.base.client.renderer.NDarknessEntityRenderer;
import sid.base.gameasset.t0001Entities;
import sid.base.main.t0001;
import sid.base.particle.t0001Particles;
import sid.base.utils.VideoRendererUtil;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;


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
     VideoRendererUtil.preloadVideo("t0001:video/hit_skullbreak_cg2.mov");
    }

    @SubscribeEvent
    public static void onShutdownClient(GameShuttingDownEvent event){
        VideoRendererUtil.shutdown();
    }





}

