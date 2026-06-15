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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import sid.base.gameasset.animations.MiscAnimations;
import sid.base.main.t0001;
import sid.base.world.ExtraSpecialDamageTypeTags;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.LongHitAnimation;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.registry.entries.EpicFightMobEffects;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class SkillEvents {

    @EventBusSubscriber(modid = t0001.MODID)
    public static class ServerEvents {

        @SubscribeEvent
        public static void damageEvent(FMLCommonSetupEvent event) {

            EpicFightEventHooks.Entity.TAKE_DAMAGE_INCOME.registerContextAwareEvent((stun_event, context) -> {
                DamageSource dmgEventDamageSource = stun_event.getDamageSource();
                LivingEntityPatch<?> entityPatch = stun_event.getEntityPatch();
                if (stun_event.isParried() || stun_event.getResult() == AttackResult.ResultType.BLOCKED || entityPatch.isStunned()) {
                    return;
                }
                boolean has_stun_immunity = entityPatch.getOriginal().hasEffect(EpicFightMobEffects.STUN_IMMUNITY);
                float impact = dmgEventDamageSource instanceof EpicFightDamageSource source ? source.calculateImpact() : 0.0f;

                if(impact >= 8.0D){
                    entityPatch.getOriginal().addTag("SetToFallBoom");
                }

                List<AnimationManager.AnimationAccessor<LongHitAnimation>> ragdoll_list = List.of(
                        MiscAnimations.RAG_DOLL_STUN_UP,
                        MiscAnimations.RAG_DOLL_UP_HIGH
                );

                ThreadLocalRandom random = ThreadLocalRandom.current();

                if(dmgEventDamageSource.is(ExtraSpecialDamageTypeTags.RAG_DOLL_LAUNCH_UP_RAND) && !has_stun_immunity){
                    entityPatch.playAnimationSynchronized(ragdoll_list.get(random.nextInt(ragdoll_list.size())),0.0f);
                }
                
                if (dmgEventDamageSource.is(ExtraSpecialDamageTypeTags.RAG_DOLL_STUN) && !has_stun_immunity) {
                    entityPatch.playAnimationSynchronized(MiscAnimations.RAG_DOLL_STUN_UP, 0.0f);
                    entityPatch.getOriginal().addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY));
                } else if (dmgEventDamageSource.is(ExtraSpecialDamageTypeTags.RAG_DOLL_LAUNCH)) {
                    entityPatch.playAnimationSynchronized(MiscAnimations.RAG_DOLL_BACK, 0.0f);
                    entityPatch.getOriginal().addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY));
                } else if (dmgEventDamageSource.is(ExtraSpecialDamageTypeTags.RAG_DOLL_LAUNCH_UP) && !has_stun_immunity) {
                    entityPatch.playAnimationSynchronized(MiscAnimations.RAG_DOLL_UP_HIGH, 0.1f);
                    entityPatch.getOriginal().addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY));
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
