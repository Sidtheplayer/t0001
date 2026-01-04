package sid.t0001.client.events;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xame.t0001;

import sid.t0001.client.renderer.AmogusRenderer;
import sid.t0001.client.renderer.DarknessEntityRenderer;
import sid.t0001.gameasset.t0001Entities;
import yesman.epicfight.api.client.forgeevent.PatchedRenderersEvent;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = t0001.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RenderEngine {

    @SubscribeEvent
    public static void onRegisterRenderers(PatchedRenderersEvent.Add event) {
        EntityRendererProvider.Context context = event.getContext();

        event.addPatchedEntityRenderer(
                t0001Entities.AMOGUS.get(),
                (entityType) -> new AmogusRenderer(context, entityType).initLayerLast(context, entityType)
        );
        event.addPatchedEntityRenderer(
                t0001Entities.DARKNESS_ENTITY.get(),
                (entityType) -> new DarknessEntityRenderer(context, entityType)
                        .initLayerLast(context,entityType));
    }
}