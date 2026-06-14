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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import sid.base.gameasset.animations.MiscAnimations;
import sid.base.main.t0001;
import sid.base.world.ExtraSpecialDamageTypeTags;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.registry.entries.EpicFightMobEffects;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;


public class SkillEvents {

    @EventBusSubscriber(modid = t0001.MODID)
    public static class ServerEvents {

        @SubscribeEvent
        public static void EventsAfterServerStart(ServerStartedEvent event){

            EpicFightEventHooks.Entity.ON_STUNNED.registerEvent(stun_event -> {
                EpicFightDamageSource dmgEventDamageSource = stun_event.getDamageSource();
                LivingEntityPatch<?> entityPatch = stun_event.getEntityPatch();
                if(!(dmgEventDamageSource instanceof EpicFightDamageSource) || stun_event.isCanceled() || entityPatch.getOriginal().getControllingPassenger() == null){
                    return;
                }
                boolean has_stun_immunity = entityPatch.getOriginal().getControllingPassenger().hasEffect(EpicFightMobEffects.STUN_IMMUNITY);

                if(dmgEventDamageSource.is(ExtraSpecialDamageTypeTags.RAG_DOLL_STUN) && !has_stun_immunity){
                   entityPatch.playAnimation(MiscAnimations.RAG_DOLL_STUN_UP, 0.0f);
                } else if (dmgEventDamageSource.is(ExtraSpecialDamageTypeTags.RAG_DOLL_LAUNCH)) {
                    entityPatch.playAnimation(MiscAnimations.RAG_DOLL_BACK,0.0f);
                } else if (dmgEventDamageSource.is(ExtraSpecialDamageTypeTags.RAG_DOLL_LAUNCH_UP) && !has_stun_immunity) {
                    entityPatch.playAnimation(MiscAnimations.RAG_DOLL_UP_HIGH, 0.1f);
                }

            });

        }
    }



 //   @EventBusSubscriber(modid = t0001.MODID, value = Dist.CLIENT)
    @SuppressWarnings("unused")
    public static class ClientOverrides {


      //  @SubscribeEvent
        public static void OverrideWithEventHook(FMLClientSetupEvent evt) {

            evt.enqueueWork(() ->
                    EpicFightClientEventHooks.Render.PREPARE_MODEL_TO_RENDER.registerEvent(
                            (event) -> {


                                if (!(event.getEntityPatch().getOriginal() instanceof LocalPlayer patch) ||
                                event.getEntityPatch().getOriginal().getTags().contains("")
                                ) {
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

            @SuppressWarnings("removal") RenderType.CompositeState state = RenderType.CompositeState.builder()

                    .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(EpicFightMod.identifier("textures/common/white.png"), false, false))
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
