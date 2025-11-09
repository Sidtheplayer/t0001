package sid.t0001.client.events;


import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xame.t0001;
import sid.t0001.client.model.AmogusModel;
import sid.t0001.client.particle.HitParryParticle;
import sid.t0001.client.particle.t0001Particle;
import sid.t0001.client.renderer.NAmogusRenderer;
import sid.t0001.gameasset.t0001Entities;
import sid.t0001.particle.t0001Particles;
import sid.t0001.world.entity.Amogus;
import sid.t0001.world.entity.AmogusPatch;
import yesman.epicfight.api.forgeevent.EntityPatchRegistryEvent;


@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid= t0001.MODID, value= Dist.CLIENT, bus= Mod.EventBusSubscriber.Bus.MOD)
public class ClientModBusEvent {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onParticleRegistry(final RegisterParticleProvidersEvent event) {

        event.registerSpecial(t0001Particles.HIT_PARRY.get(), new HitParryParticle.Provider(null));

        event.registerSpecial(t0001Particles.FAST_AFTERIMAGE.get(), new t0001Particle.FastWhiteAfterimageProvider());

    }

    @SubscribeEvent
    public static void registerRenderersEvent(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(t0001Entities.AMOGUS.get(), NAmogusRenderer::new);
    }// register amogus vanilla renderer

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event  ){
        event.registerLayerDefinition(AmogusModel.LAYER_LOCATION, AmogusModel::createBodyLayer);
    }//amogus model layer

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(t0001Entities.AMOGUS.get(), Amogus.createAttributes().build());
    }//register amogus vanilla attributes
    @SubscribeEvent
    public static void registerEntityPatch(EntityPatchRegistryEvent event) {
        event.getTypeEntry().put(t0001Entities.AMOGUS.get(), (entityIn) -> AmogusPatch::new);
    }// you know what it says

    @SubscribeEvent
    public static void registerEFAttribute(EntityAttributeModificationEvent event) {
        AmogusPatch.initAttributes(event);
    }/* ifykyk */





}

