package sid.t0001.skill.weaponinnate;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLPaths;
import org.joml.Matrix4f;
import org.watermedia.api.player.PlayerAPI;
import org.watermedia.api.player.videolan.VideoPlayer;
import sid.t0001.gameasset.t0001Animations;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;
import yesman.epicfight.world.effect.EpicFightMobEffects;
import yesman.epicfight.world.entity.eventlistener.DealDamageEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;
import yesman.epicfight.world.entity.eventlistener.TakeDamageEvent;


import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static net.minecraft.world.effect.MobEffects.LEVITATION;

public class t0001InnateOne extends WeaponInnateSkill {
    private static final UUID EVENT_UUID = UUID.fromString("2b9a70cf-893d-47a7-9dd3-c82000b6f080");

    public final AssetAccessor<? extends AttackAnimation> first;
    public final AssetAccessor<? extends AttackAnimation> second;
    public final AssetAccessor<? extends AttackAnimation> third;
    public final AssetAccessor<? extends AttackAnimation> fourth;
    public final AssetAccessor<? extends AttackAnimation> fifth;
    public AssetAccessor<? extends StaticAnimation> dynamic_fail_animation = null;

    public t0001InnateOne(SkillBuilder<? extends WeaponInnateSkill> builder) {
        super(builder);
        this.first = t0001Animations.TFU1;
        this.second = t0001Animations.TFU2;
        this.third = t0001Animations.TFU4_COPY;
        this.fourth = t0001Animations.TFU4;
        this.fifth = t0001Animations.TFU5_REMADE;
    }

    //HUGE thanks to Yonichi(refm) and arcane(Ascended arts)!
    // note to self - check if statements' indentations, if something doesn't work after you add another anim.
    private boolean isTFU5Active = false;
    private LivingEntity opponentEntity = null;
    private static final UUID TAKE_DAMAGE_UUID = UUID.fromString("5e9a70cf-893d-47a7-9dd3-c82000b6f083");
    private static final UUID DAMAGE_EVENT_UUID = UUID.fromString("3c9a70cf-893d-47a7-9dd3-c82000b6f081"); // Different UUID!
    private static final UUID BEGIN_EVENT_UUID = UUID.fromString("4d9a70cf-893d-47a7-9dd3-c82000b6f082");

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);

        container.getExecutor().getEventListener().addEventListener(EventType.DEAL_DAMAGE_EVENT_HURT, DAMAGE_EVENT_UUID, (DealDamageEvent.Hurt damageEvent) -> {
                    // to make video active on hurt
                    if (isTFU5Active) {
                        if (opponentEntity != null && opponentEntity.isAlive()) {
                            opponentEntity.addEffect(new MobEffectInstance(LEVITATION, 55, 2, false, false, false));
                            opponentEntity.addTag("SetToFallBoom"); // Tag to identify for fall slam (see SlammingFallEventHandle)

                            //UNSAFE and only works on client side >> Packet sending needed from server side to client side
//                            new Timer().schedule(new TimerTask() {
//                                @Override
//                                public void run() {
//                                  //  DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> VideoOverlayRenderer::startVideo);
//                                }
//                            }, 200L);
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
            this.dynamic_fail_animation = event.getPlayerPatch().getServerAnimator().getLivingAnimation(LivingMotions.IDLE,Animations.BIPED_IDLE);

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
                    event.getPlayerPatch().reserveAnimation(this.dynamic_fail_animation);
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
                    event.getPlayerPatch().playAnimationSynchronized(this.dynamic_fail_animation,0.2F);
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
                    event.getPlayerPatch().reserveAnimation(this.dynamic_fail_animation);
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
                    event.getPlayerPatch().reserveAnimation(this.dynamic_fail_animation);
                    event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
                }
            }
            if (t0001Animations.TFU5_REMADE.equals(event.getAnimation())) {
                isTFU5Active = false;
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
                new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY.get(), 38, 2, true, false, false)
        );


    }

    //TODO: Clean up this mess later and separate into client and server classes and use it as a universal video overlay renderer
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