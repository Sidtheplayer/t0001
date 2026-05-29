package sid.base.events.global_events;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;


public class SkillEventOverrides {


 //   @EventBusSubscriber(modid = t0001.MODID, value = Dist.CLIENT)
    public static class ClientOverrides {


      //  @SubscribeEvent
        public static void OverrideWithEventHook(FMLClientSetupEvent evt) {

            evt.enqueueWork(() ->
                    EpicFightClientEventHooks.Render.PREPARE_MODEL_TO_RENDER.registerEvent(
                            (event) -> {


                                if (!(event.getEntityPatch().getOriginal() instanceof LocalPlayer patch)) {
                                    return;
                                }
                                var entityPatch = event.getEntityPatch();

                                {

                                    LivingEntity entity = event.getEntityPatch().getOriginal();


                                    ResourceLocation texture = entity instanceof AbstractClientPlayer player
                                            ? player.getSkin().texture()
                                            : Minecraft.getInstance()
                                            .getEntityRenderDispatcher()
                                            .getRenderer(entity)
                                            .getTextureLocation(entity);

                                    entityPatch.overrideRender();

                                    event.getMesh().draw(
                                            event.getPoseStack(),
                                            event.getBuffer(),
                                            getDepthStrippedRenderType(texture),
                                            event.getPackedLight(),
                                            1.0F, 1.0F, 1.0F, 1.0F,
                                            OverlayTexture.NO_OVERLAY,
                                            entityPatch.getArmature(),
                                            entityPatch.getArmature().getPoseMatrices()
                                    );

                                    entityPatch.overrideRender();

                                }
                            }
                    )
            );


        }

        public static RenderType getDepthStrippedRenderType(ResourceLocation texture) {
            RenderType.CompositeState state = RenderType.CompositeState.builder()

                    .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)

                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)

                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .createCompositeState(false);

            return RenderType.create("epicfight_depth_stripped",
                    DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.TRIANGLES,
                    256,
                    true,
                    true,
                    state);
        }


    }


}
