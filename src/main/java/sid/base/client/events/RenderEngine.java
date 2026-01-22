package sid.base.client.events;

import net.minecraft.client.renderer.entity.EntityRendererProvider;


import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import sid.base.client.renderer.AmogusRenderer;
import sid.base.client.renderer.DarknessEntityRenderer;
import sid.base.client.renderer.weapon.DragonGodSwordRenderer;
import sid.base.gameasset.t0001Entities;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.event.types.registry.RegisterPatchedRenderersEvent;
import yesman.epicfight.client.events.engine.IEventBasedEngine;
import sid.base.main.t0001;


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

    public static void regItemRenderers(RegisterPatchedRenderersEvent.Item event) {
        event.addItemRenderer(
                ResourceLocation.fromNamespaceAndPath(t0001.MODID, "dgs"), DragonGodSwordRenderer::new
        );
    }

    @Override
    public void gameEventBus(IEventBus gameEventBus) {
    }

    @Override
    public void modEventBus(IEventBus modEventBus) {
    }

    public static void init() {
        EpicFightClientEventHooks.Registry.ADD_PATCHED_ENTITY.registerEvent(
                RenderEngine::onRegisterRenderers
        );
        EpicFightClientEventHooks.Registry.PATCHED_ITEM.registerEvent(
                RenderEngine::regItemRenderers
        );
    }
}
