package sid.t0001.client.events;

import net.minecraft.client.renderer.entity.EntityRendererProvider;


import net.neoforged.bus.api.IEventBus;
import sid.t0001.client.renderer.AmogusRenderer;
import sid.t0001.client.renderer.DarknessEntityRenderer;
import sid.t0001.gameasset.t0001Entities;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.event.types.registry.RegisterPatchedRenderersEvent;
import yesman.epicfight.client.events.engine.IEventBasedEngine;


public class RenderEngine implements IEventBasedEngine {

    private static final RenderEngine INSTANCE = new RenderEngine();

    public static RenderEngine getInstance() {
        return INSTANCE;
    }

    public static void onRegisterRenderers(RegisterPatchedRenderersEvent.AddEntity event) {
        EntityRendererProvider.Context context = event.getContext();

        event.addPatchedEntityRenderer(
                t0001Entities.AMOGUS.get(),
                type -> new AmogusRenderer(context, type)
                        .initLayerLast(context, type)
        );

        event.addPatchedEntityRenderer(
                t0001Entities.DARKNESS_ENTITY.get(),
                type -> new DarknessEntityRenderer(context, type)
                        .initLayerLast(context, type)
        );
    }

    @Override
    public void gameEventBus(IEventBus gameEventBus) {
    }

    @Override
    public void modEventBus(IEventBus modEventBus) {
        // Not needed for renderer registration
    }
     public static void init(){
         EpicFightClientEventHooks.Registry.ADD_PATCHED_ENTITY.registerEvent(
                 RenderEngine::onRegisterRenderers
         );
     }
}
