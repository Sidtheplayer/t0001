package sid.t0001.skill.weaponinnate;

import com.lowdragmc.photon.client.fx.EntityEffect;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXHelper;
import com.lowdragmc.photon.client.fx.FXRuntime;
import com.lowdragmc.photon.client.gameobject.IFXObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLPaths;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.watermedia.api.player.PlayerAPI;
import org.watermedia.api.player.videolan.VideoPlayer;
import sid.t0001.events.LightningBallHandler;
import sid.t0001.gameasset.ReusableEvents;
import sid.t0001.gameasset.t0001Animations;
import sid.t0001.gameasset.t0001Sounds;
import sid.t0001.utils.JointTrackedEntityEffect;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationParameters;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.effect.EpicFightMobEffects;
import yesman.epicfight.world.entity.eventlistener.DealDamageEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;
import yesman.epicfight.world.entity.eventlistener.TakeDamageEvent;


import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static com.google.common.math.Quantiles.scale;
import static net.minecraft.world.effect.MobEffects.SLOW_FALLING;
import static sid.t0001.gameasset.ReusableEvents.MyFxHelpers.JointTrack.getJointWithTranslation;


public class t0001InnateOne extends WeaponInnateSkill {
    private static final UUID EVENT_UUID = UUID.fromString("2b9a70cf-893d-47a7-9dd3-c82000b6f080");

    public final AssetAccessor<? extends AttackAnimation> first;
    public final AssetAccessor<? extends AttackAnimation> second;
    public final AssetAccessor<? extends AttackAnimation> third;
    public final AssetAccessor<? extends AttackAnimation> fourth;
    public final AssetAccessor<? extends AttackAnimation> fifth;
    public final AnimationManager.AnimationAccessor<StaticAnimation> fail;

    public t0001InnateOne(SkillBuilder<? extends WeaponInnateSkill> builder) {
        super(builder);
        this.first = t0001Animations.TFU1;
        this.second = t0001Animations.TFU2;
        this.third = t0001Animations.TFU4_COPY;
        this.fourth = t0001Animations.TFU4;
        this.fifth = t0001Animations.TFU5_REMADE;
        this.fail = Animations.BIPED_IDLE;
    }

    //HUGE thanks to Yonichi(refm) and arcane(Ascended arts)!
    // check if statements' indentations, if something doesnt work after you add another anim.
    private boolean isTFU5Active = false;
    private LivingEntity opponentEntity = null;
    private static final UUID TAKE_DAMAGE_UUID = UUID.fromString("5e9a70cf-893d-47a7-9dd3-c82000b6f083");
    private static final UUID DAMAGE_EVENT_UUID = UUID.fromString("3c9a70cf-893d-47a7-9dd3-c82000b6f081"); // Different UUID!
    private static final UUID BEGIN_EVENT_UUID = UUID.fromString("4d9a70cf-893d-47a7-9dd3-c82000b6f082");

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);

        container.getExecutor().getEventListener().addEventListener(EventType.DEAL_DAMAGE_EVENT_ATTACK, DAMAGE_EVENT_UUID, (DealDamageEvent.Attack damageEvent) -> {
                    // to make video active on hurt
                    if (isTFU5Active) {
                        if (opponentEntity != null && opponentEntity.isAlive()) {
//                    System.out.println("TRIGGERING VIDEO PLAYER!"); logging
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> VideoOverlayRenderer::startVideo);
                                }
                            }, 200L);
                            LivingEntityPatch<?> opponent = EpicFightCapabilities.getEntityPatch(opponentEntity, LivingEntityPatch.class);

                            if (opponent != null && opponentEntity.isAlive()) {
                                LivingEntity target = opponent.getOriginal();
                                MinecraftServer server = target.getServer();
                                if (server != null) {
                                    server.execute(() -> {
                                        new Timer().schedule(new TimerTask() {
                                            @Override
                                            public void run() {
                                                if (target.onGround()) {
                                                    target.level().addParticle(
                                                            EpicFightParticles.GROUND_SLAM.get(),
                                                            target.getX(), target.getY(), target.getZ(),
                                                            Double.longBitsToDouble(target.getId()), 1, 1
                                                    );
                                                    opponent.applyStun(StunType.KNOCKDOWN, 4.0F);
                                                    opponent.playSound(t0001Sounds.SLAM_SFX.get(), 0.0F, 1.0F);

                                                }
                                            }
                                        }, 500L); // half a second delay — enough for fall time
                                    });
                                }
                            }
                        }


                    }
                    else {
                        LivingEntity player = damageEvent.getPlayerPatch().getOriginal();
                        LocalPlayer localPlayer = Minecraft.getInstance().player;

                        FX fire_katana_fx = FXHelper.getFX(ResourceLocation.parse("photon:fire_katana"));
                        JointTrackedEntityEffect fire_katana = new JointTrackedEntityEffect(
                                fire_katana_fx,
                                player.level(),
                                player,
                                localPlayer,
                                Armatures.BIPED.get().toolR,
                                new Vec3f(0, 0, 0),
                                EntityEffect.AutoRotate.NONE,
                                new Vec3f(-0.3,-1.8,0)

                        );


                        fire_katana.setRotation(0, 1, 0);
                        fire_katana.setScale(1, 1, 1);
                        fire_katana.setAllowMulti(false);
                        fire_katana.setForcedDeath(true);
                        fire_katana.setDelay(0);


                        fire_katana.start();
//                        fire_katana.getRuntime().root.updateFrame(0.1F);




                        MinecraftServer server = player.getServer();
                        if (server != null && fire_katana.getRuntime() != null) {
                            server.execute(() -> {
                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        if (fire_katana.getRuntime().isAlive()) {
                                            fire_katana.getRuntime().destroy(true);
                                        }
                                    }
                                }, 15500L);
                            });
                        }
                    }

                });

        container.getExecutor().getEventListener().addEventListener(
                EventType.TAKE_DAMAGE_EVENT_HURT, TAKE_DAMAGE_UUID,
                (TakeDamageEvent.Hurt event) -> {
                    if (isTFU5Active) {
//                        System.out.println("TFU5 interrupted by damage! Resetting state."); logging
                        isTFU5Active = false;
                        opponentEntity = null;
                    }
                }
        );

        container.getExecutor().getEventListener().addEventListener(EventType.ATTACK_ANIMATION_END_EVENT, EVENT_UUID, (event) -> {

            if (t0001Animations.TFU1.equals(event.getAnimation())) {
                List<LivingEntity> hurtEntities = event.getPlayerPatch().getCurrentlyActuallyHitEntities();

                if (!hurtEntities.isEmpty() && hurtEntities.get(0).isAlive()) {

                    Objects.requireNonNull(event.getPlayerPatch().getServerAnimator().getPlayerFor(null)).reset();
                    event.getPlayerPatch().reserveAnimation(this.second);
                    //the "Haaaah!" sounds
                    event.getPlayerPatch().playSound(SoundEvents.VILLAGER_HURT, 75, 0, 155);
                    ServerPlayer player = event.getPlayerPatch().getOriginal();
                    PlayerChatMessage chatMessage = PlayerChatMessage.unsigned(player.getUUID(), "Pathetic");
                    event.getPlayerPatch().getOriginal().sendChatMessage(
                            new OutgoingChatMessage.Player(chatMessage),
                            false,//If ykyk
                            ChatType.bind(ChatType.TEAM_MSG_COMMAND_INCOMING, player)
                    );

                    event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();

                } else {
                    Objects.requireNonNull(event.getPlayerPatch().getServerAnimator().getPlayerFor(null)).reset();
                    event.getPlayerPatch().reserveAnimation(this.fail);
                    event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
                }
            }

            if (t0001Animations.TFU2.equals(event.getAnimation())) {
                List<LivingEntity> hurtEntities = event.getPlayerPatch().getCurrentlyActuallyHitEntities();
                if (!hurtEntities.isEmpty() && hurtEntities.get(0).isAlive()) {
                    Objects.requireNonNull(event.getPlayerPatch().getServerAnimator().getPlayerFor(null)).reset();
                    event.getPlayerPatch().reserveAnimation(this.third);
                    event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
                } else {
                    Objects.requireNonNull(event.getPlayerPatch().getServerAnimator().getPlayerFor(null)).reset();
                    event.getPlayerPatch().reserveAnimation(this.fail);
                    event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
                }
            }

            if (t0001Animations.TFU4_COPY.equals(event.getAnimation())) {
                // was supposed to use TFU3 but I "accidentally" broke the anim in blender
                List<LivingEntity> hurtEntities = event.getPlayerPatch().getCurrentlyActuallyHitEntities();
                if (!hurtEntities.isEmpty() && hurtEntities.get(0).isAlive()) {
                    Objects.requireNonNull(event.getPlayerPatch().getServerAnimator().getPlayerFor(null)).reset();
                    event.getPlayerPatch().reserveAnimation(this.fourth);
                    event.getPlayerPatch().getAngleTo(hurtEntities.get(0));
                    event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
                } else {
                    Objects.requireNonNull(event.getPlayerPatch().getServerAnimator().getPlayerFor(null)).reset();
                    event.getPlayerPatch().reserveAnimation(this.fail);
                    event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
                }
            }

            if (t0001Animations.TFU4.equals(event.getAnimation())) {
                List<LivingEntity> hurtEntities = event.getPlayerPatch().getCurrentlyActuallyHitEntities();
                if (!hurtEntities.isEmpty() && hurtEntities.get(0).isAlive()) {
                    Objects.requireNonNull(event.getPlayerPatch().getServerAnimator().getPlayerFor(null)).reset();
                    event.getPlayerPatch().reserveAnimation(this.fifth);
                    opponentEntity = hurtEntities.get(0);
                    isTFU5Active = true;
                    // System.out.println("TFU5_remade is activated, isTFU5Active = true"); logging
                } else {
                    Objects.requireNonNull(event.getPlayerPatch().getServerAnimator().getPlayerFor(null)).reset();
                    event.getPlayerPatch().reserveAnimation(this.fail);
                    event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
                }
            }

            if (t0001Animations.TFU5_REMADE.equals(event.getAnimation())) {
                isTFU5Active = false;
//                System.out.println("TFU5 ENDED - isTFU5Active = false"); logging
                event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
            }

            if (Animations.BATTOJUTSU_DASH.equals(event.getAnimation())) {
                List<LivingEntity> hurtEntities = event.getPlayerPatch().getCurrentlyActuallyHitEntities();

                if (!hurtEntities.isEmpty()) {
                    for (LivingEntity target : hurtEntities) {
                        if (target != null && target.isAlive()) {
                            // Spawn lightning ball FX per target
                            EntityEffect lightning_ball = new EntityEffect(
                                    FXHelper.getFX(ResourceLocation.parse("photon:yellow_lightning_ball")),
                                    target.level(),
                                    target,
                                    EntityEffect.AutoRotate.FORWARD
                            );
                            lightning_ball.setOffset(0, 1, 0);
                            lightning_ball.setRotation(0, 0, 0);
                            lightning_ball.setScale(1, 1, 1);
                            lightning_ball.setAllowMulti(false);
                            lightning_ball.setForcedDeath(true);
                            lightning_ball.start();

                            if (!target.level().isClientSide()) {
                                ItemStack weapon = event.getPlayerPatch().getOriginal().getMainHandItem();

                                int sweepingLevel = weapon.getEnchantmentLevel(Enchantments.SWEEPING_EDGE);
                                int maxDamage = Math.max(1, weapon.getMaxDamage());
                                int currentDamage = Math.min(weapon.getDamageValue(), maxDamage - 1);

                                float resistance = Math.max(0.25f, ((float) maxDamage - currentDamage) / maxDamage);

                                int selective_amplifier = Math.max(1, Math.min(1 + sweepingLevel, 3));

                                int base = Math.round(Math.min((sweepingLevel * resistance * 30.0f) + 3, 50));
                                int selective_amperage = Math.max(3, (int) Math.pow(base, 0.75));

                                float sweepDamageScale = 0.5f + (sweepingLevel * 0.15f);
                                float resistanceFactor = 0.5f + (resistance * 0.5f);
                                float selective_damage_amp = Math.min(1.0f, sweepDamageScale * resistanceFactor);

                                LightningBallHandler.addLightningTarget(target, selective_amperage, (int) (selective_damage_amp * 2.0f), lightning_ball.getRuntime());
                            }
                        }
                    }

                    // Clear after all effects for mobs have been processed
                    event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
                }

            }
            else {
                event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
            }


        });


    }

    @Override
    public void onRemoved(SkillContainer container) {
        container.getExecutor().getEventListener().removeListener(EventType.ATTACK_ANIMATION_END_EVENT, EVENT_UUID);
        container.getExecutor().getEventListener().removeListener(EventType.DEAL_DAMAGE_EVENT_ATTACK, DAMAGE_EVENT_UUID);
        container.getExecutor().getEventListener().removeListener(EventType.ANIMATION_BEGIN_EVENT, BEGIN_EVENT_UUID);
        container.getExecutor().getEventListener().removeListener(EventType.TAKE_DAMAGE_EVENT_HURT, TAKE_DAMAGE_UUID);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> VideoOverlayRenderer::stop);
    }

    @Override
    public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
        container.getExecutor().playAnimationSynchronized(this.first, 0);
        container.getExecutor().getOriginal().addEffect(
                new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY.get(), 38, 10, true, false, false)
        );
    }

    @SuppressWarnings("CallToPrintStackTrace")
    @OnlyIn(Dist.CLIENT)
    public static class VideoOverlayRenderer {
        private static VideoPlayer videoPlayer = null;
        private static VideoPlayer preloadedPlayer = null;
        private static Path cachedVideoPath = null;
        private static boolean registered = false;
        private static boolean isReady = false;

        // I am an imposter, why does this work? if this work lets not touch it - can see my dumbahh breaking this rule nex day
        public static void preloadVideo() {
            // I lost more hair while trying to get this thing to work than deriving an organic chem equation from scratch
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!PlayerAPI.isReady()) {
                        System.out.println("PlayerAPI not ready, retrying preload in 1 second...");
                        preloadVideo(); // Retry
                        return;
                    }

                    if (cachedVideoPath == null || !Files.exists(cachedVideoPath)) {
                        extractVideoFromJar();
                    }

                    if (cachedVideoPath == null) {
                        System.err.println("Failed to extract video for preload");
                        return;
                    }

                    Minecraft.getInstance().tell(() -> {
                        try {
                            if (preloadedPlayer == null) {
                                URI videoUri = cachedVideoPath.toUri();
                                preloadedPlayer = new VideoPlayer(PlayerAPI.getFactory(), Minecraft.getInstance());
                                preloadedPlayer.startPaused(videoUri);
                                System.out.println("Video preloaded successfully!");
                            }
                        } catch (Exception e) {
                            System.err.println("Failed to preload video: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                }
            }, 2000L); // pre-delay to preload :? next week im gonna forget what all this code even does
        }

        public static void startVideo() {
            // Extracts video first
            if (cachedVideoPath == null || !Files.exists(cachedVideoPath)) {
                extractVideoFromJar();
            }

            if (cachedVideoPath == null) {
                System.err.println("Failed to extract video");
                return;
            }

            Minecraft.getInstance().tell(() -> {
                try {
                    if (!PlayerAPI.isReady()) {
                        System.err.println("PlayerAPI not ready");
                        return;
                    }

                    if (videoPlayer != null) {
                        videoPlayer.release();
                    }


                    if (preloadedPlayer != null) {
                        videoPlayer = preloadedPlayer;
                        preloadedPlayer = null;
                        videoPlayer.setRepeatMode(false);
                        videoPlayer.setSpeed(0.35F);
                        videoPlayer.play();
                        isReady = true;
                        System.out.println("Using preloaded video player");
                    } else {

                        URI videoUri = cachedVideoPath.toUri();
                        videoPlayer = new VideoPlayer(PlayerAPI.getFactory(), Minecraft.getInstance());
                        videoPlayer.setRepeatMode(false);
                        videoPlayer.setSpeed(0.36F);
                        videoPlayer.start(videoUri);
                        isReady = false;
                        System.out.println("Video player created (not preloaded)");
                    }


                    if (!registered) {
                        MinecraftForge.EVENT_BUS.register(VideoOverlayRenderer.class);
                        registered = true;
                    }

                } catch (Exception e) {
                    System.err.println("Failed to start video: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }

        @SubscribeEvent
        public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
            if (videoPlayer == null || !videoPlayer.isSafeUse()) {
                return;
            }

            try {

                if (!isReady && videoPlayer.isReady()) {
                    isReady = true;
                    long duration = videoPlayer.getDuration();
                    System.out.println("Video is ready! Duration: " + duration + "ms");
                }

                if (!isReady) {
                    return;
                }


                if (videoPlayer.isEnded()) {
                    System.out.println("Video ended naturally, stopping playback");
                    stop();
                    return;
                }


                if (videoPlayer.isStopped() || videoPlayer.isBroken()) {
                    System.out.println("Video stopped or broken, cleaning up");
                    stop();
                    return;
                }

                Minecraft mc = Minecraft.getInstance();
                int screenWidth = mc.getWindow().getGuiScaledWidth();
                int screenHeight = mc.getWindow().getGuiScaledHeight();

                renderVideoTexture(event.getGuiGraphics(), videoPlayer.texture(),
                        screenWidth, screenHeight);

            } catch (Exception e) {
                System.err.println("Error rendering video: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private static void renderVideoTexture(GuiGraphics graphics, int textureId, int width, int height) {
            RenderSystem.enableBlend();
            RenderSystem.setShaderTexture(0, textureId);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);

            Matrix4f matrix = graphics.pose().last().pose();
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buffer.vertex(matrix, 0, 0, 0).uv(0, 0).endVertex();
            buffer.vertex(matrix, 0, height, 0).uv(0, 1).endVertex();
            buffer.vertex(matrix, width, height, 0).uv(1, 1).endVertex();
            buffer.vertex(matrix, width, 0, 0).uv(1, 0).endVertex();

            BufferUploader.drawWithShader(buffer.end());
            RenderSystem.disableBlend();
        }

        public static void stop() {
            if (videoPlayer != null) {
                try {
                    videoPlayer.release();
                    System.out.println("Video player stopped and released");
                } catch (Exception e) {
                    System.err.println("Error stopping video: " + e.getMessage());
                }
                videoPlayer = null;
                isReady = false;
            }
        }

        @OnlyIn(Dist.CLIENT)
        private static synchronized void extractVideoFromJar() {
            try {
                if (cachedVideoPath != null && Files.exists(cachedVideoPath)) {
                    return;
                }

                String modId = "t0001";
                String videoFilename = "hit_skullbreak_cg2.mov";
                String resourcePath = "/assets/" + modId + "/video/" + videoFilename;

                Path videoDir = FMLPaths.GAMEDIR.get().resolve(modId).resolve("video");
                Files.createDirectories(videoDir);
                Path videoFile = videoDir.resolve(videoFilename);

                try (InputStream in = VideoOverlayRenderer.class.getResourceAsStream(resourcePath)) {
                    if (in == null) {
                        throw new FileNotFoundException("Video not found: " + resourcePath);
                    }
                    Files.copy(in, videoFile, StandardCopyOption.REPLACE_EXISTING);
                    cachedVideoPath = videoFile;
                    System.out.println("Video extracted to: " + videoFile);
                }
            } catch (Exception e) {
                System.err.println("Failed to extract video: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}