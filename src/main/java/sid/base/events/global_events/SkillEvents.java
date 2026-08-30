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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import sid.base.events.event_hook.AwakenTickEvent;
import sid.base.events.event_hook.MyEventHooks;
import sid.base.gameasset.animations.MiscAnimations;
import sid.base.gameasset.animations.types.ProtectedHitAnimation;
import sid.base.main.t0001;
import sid.base.skill.t0001SkillDataKeys;
import sid.base.skill.t0001SkillSlots;
import sid.base.skill.t0001Skills;
import sid.base.utils.HelperUtils;
import sid.base.world.ExtraSpecialDamageTypeTags;
import sid.base.world.entity.ShadowCloneEntity;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.types.LongHitAnimation;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.registry.entries.EpicFightMobEffects;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class SkillEvents {

    @EventBusSubscriber(modid = t0001.MODID)
    public static class ServerEvents {


        @SubscribeEvent
        public static void onPlayerDeath(LivingDeathEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {

                ServerPlayerPatch playerPatch = EpicFightCapabilities.getServerPlayerPatch(player);

                if (playerPatch == null) return;

                if (HelperUtils.has_skill_orNull(playerPatch, t0001Skills.SHADOW_CLONE_SKILL.value(), t0001SkillSlots.AWAKENING_EXTRA_SKILL)) {

                    List<ShadowCloneEntity> shadowList = ShadowCloneEntity.getShadowCloneList(player);

                    if (shadowList.isEmpty()) return;

                    for (int i = 0; i < shadowList.size(); i++) {


                        ShadowCloneEntity entity = shadowList.get(i);

                        int delay = Math.max(5, i + 3);

                        MinecraftServer server = player.getServer();

                        if (server != null) {
                            //Because I have trust issues with server.tell()
                            GlobalEventHandlers.DelayedTaskScheduler.schedule(
                                    server, delay, entity::kill
                                    );

                        }


                    }

                }

            }


        }


        @SubscribeEvent
        public static void awaken_pre_tick(PlayerTickEvent.Pre event) {
            PlayerPatch<?> playerPatch = EpicFightCapabilities.getPlayerPatch(event.getEntity());
            if (playerPatch != null) {

                if (!playerPatch.getSkill(t0001SkillSlots.AWAKENING).isEmpty()) {
                    var data_manager = playerPatch.getSkill(t0001SkillSlots.AWAKENING).getDataManager();
                    boolean has_data = data_manager.hasData(t0001SkillDataKeys.IS_AWAKENED) && data_manager.hasData(t0001SkillDataKeys.ULTIMATE_METER);

                    if (has_data && data_manager.getDataValue(t0001SkillDataKeys.IS_AWAKENED)) {

                        AwakenTickEvent awakenTickEvent = new AwakenTickEvent(playerPatch);
                        MyEventHooks.Awakening.TICK.postWithListener(awakenTickEvent, playerPatch.getEventListener());

                        if (awakenTickEvent.isCanceled()) {
                            data_manager.setDataSync(t0001SkillDataKeys.IS_AWAKENED, false);
                        }


                    }
                }
            }

        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void protectHitanim(LivingDamageEvent.Pre event) {
            float damage = event.getOriginalDamage();

            boolean should_protect = false;
            LivingEntityPatch<?> targetPatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), LivingEntityPatch.class);

            if (targetPatch == null) {
                return;
            }

            AnimationPlayer player = targetPatch.getAnimator().getPlayerFor(null);

            EpicFightDamageSource damageSource = null;
            if (event.getSource() instanceof EpicFightDamageSource damageSource1) {
                damageSource = damageSource1;
            }

            if (player != null && damageSource != null) {
                should_protect = player.getAnimation().checkType(ProtectedHitAnimation.class) && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !damageSource.is(ExtraSpecialDamageTypeTags.SPECIAL_EXECUTION_FINISHER);
            }

            if (targetPatch.getOriginal().getHealth() - damage <= 1.5f) {
                System.out.println("health low, should protect: " + should_protect);
            }

            if (targetPatch.getOriginal().getHealth() - damage <= 1.5f && should_protect) {
                System.out.println("protecting!");
                event.setNewDamage(Math.max(targetPatch.getOriginal().getHealth() - 1.5f, 0.0f));
            }

        }


        @SubscribeEvent
        public static void damageEvent(FMLCommonSetupEvent Event) {

            EpicFightEventHooks.Entity.TAKE_DAMAGE_INCOME.registerContextAwareEvent((stun_event, context) -> {
                DamageSource dmgEventDamageSource = stun_event.getDamageSource();
                LivingEntityPatch<?> entityPatch = stun_event.getEntityPatch();
                if (stun_event.isParried() || stun_event.getResult() == AttackResult.ResultType.BLOCKED || entityPatch.isStunned()) {
                    return;
                }
                boolean has_stun_immunity = entityPatch.getOriginal().hasEffect(EpicFightMobEffects.STUN_IMMUNITY);
                float impact = dmgEventDamageSource instanceof EpicFightDamageSource source ? source.calculateImpact() : 0.0f;

                if (impact >= 8.0D) {
                    entityPatch.getOriginal().addTag("SetToFallBoom");
                }

                List<AnimationManager.AnimationAccessor<LongHitAnimation>> ragdoll_list = List.of(
                        MiscAnimations.RAG_DOLL_STUN_UP,
                        MiscAnimations.RAG_DOLL_UP_HIGH
                );

                ThreadLocalRandom random = ThreadLocalRandom.current();

                if (dmgEventDamageSource.is(ExtraSpecialDamageTypeTags.RAG_DOLL_LAUNCH_UP_RAND) && !has_stun_immunity) {
                    entityPatch.playAnimationSynchronized(ragdoll_list.get(random.nextInt(ragdoll_list.size())), 0.0f);
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
