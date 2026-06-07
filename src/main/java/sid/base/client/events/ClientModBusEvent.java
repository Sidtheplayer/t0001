package sid.base.client.events;



import com.google.common.base.Suppliers;
import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.gui.hud.ModularHudLayer;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import sid.base.client.model.AmogusModel;
import sid.base.client.model.darkness;
import sid.base.client.particle.*;
import sid.base.client.renderer.NAmogusRenderer;
import sid.base.client.renderer.NDarknessEntityRenderer;
import sid.base.gameasset.t0001Entities;
import sid.base.main.t0001;
import sid.base.network.KeyMapHandle;
import sid.base.particle.t0001Particles;
import sid.base.skill.awakening.JunAwaken;
import sid.base.skill.awakening.SunSwordZenith;
import sid.base.utils.VideoRendererUtil;


@EventBusSubscriber(modid= t0001.MODID, value= Dist.CLIENT)
public class ClientModBusEvent {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onParticleRegistry(final RegisterParticleProvidersEvent event) {

        event.registerSpecial(t0001Particles.VFX_PROXY.get(), new EmitterProxy.Provider());

        event.registerSpriteSet(t0001Particles.BUZZ_HIT.get(), BluntImpactParticle.Provider::new);

        event.registerSpecial(t0001Particles.BLOODY_CUT.get(), new BloodyCutParticle.Provider());

        event.registerSpecial(t0001Particles.TEX_AFTERIMAGE.get(), new PlayerSkinnedAfterImage.T0001WhiteAfterimageProvider());

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
     VideoRendererUtil.preloadVideo("t0001:video/impact_frames/one_inch/frame0impact.mp4");
    }

    @SubscribeEvent
    public static void onShutdownClient(GameShuttingDownEvent event){
        VideoRendererUtil.shutdown();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event)
    {
        if (Minecraft.getInstance().getOverlay() == null && Minecraft.getInstance().screen == null) {
            KeyMapHandle.handleKeybinds();
        }



    }

    @SubscribeEvent
    public static void reg_ui(RegisterGuiLayersEvent event){
        var dragon_booster = Suppliers.memoize(()-> SunSwordZenith.createUI(Minecraft.getInstance().player));
        var mui_cache = Suppliers.memoize(()-> JunAwaken.createUI(Minecraft.getInstance().player));

        event.registerAboveAll(LDLib2.id("jun_hud"), (ModularHudLayer) mui_cache::get);
        event.registerAboveAll(t0001.identifier("solar_hud"), (ModularHudLayer) dragon_booster::get);

    }





}

