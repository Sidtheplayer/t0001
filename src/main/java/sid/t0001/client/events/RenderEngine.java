package sid.t0001.client.events;

import net.minecraft.client.renderer.entity.EntityRendererProvider;


import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import sid.t0001.client.renderer.AmogusRenderer;
import sid.t0001.client.renderer.DarknessEntityRenderer;
import sid.t0001.gameasset.t0001Entities;
import sid.t0001.main.t0001;
import yesman.epicfight.api.client.neoevent.PatchedRenderersEvent;


@EventBusSubscriber(modid = t0001.MODID, value = Dist.CLIENT)
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