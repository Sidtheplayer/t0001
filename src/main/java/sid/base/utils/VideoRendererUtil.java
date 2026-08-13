package sid.base.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.joml.Matrix4f;
import org.watermedia.WaterMedia;
import org.watermedia.api.player.PlayerAPI;
import org.watermedia.api.player.videolan.VideoPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sid.base.main.t0001;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Utility class for playing videos on screen with WaterMedia
 * Can play videos for specific targets or globally
 * Usage:
 * VideoRendererUtil.playVideo("t0001:video/my_video.mp4", targetEntity, 0.5f);
 */


@EventBusSubscriber(modid = t0001.MODID, value = Dist.CLIENT)
public class VideoRendererUtil {

    private static final AtomicInteger try_count = new AtomicInteger(0);
    private static final int max_tries = 5;

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoRendererUtil.class);
    private static final Map<UUID, ActiveVideo> activeVideos = new ConcurrentHashMap<>();
    private static final Map<String, Path> cachedVideoPaths = new ConcurrentHashMap<>();
    private static final Map<String, VideoPlayer> preloadedPlayers = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1, r -> {
        Thread t = new Thread(r, "VideoRendererUtil-Scheduler");
        t.setDaemon(true);
        return t;
    });

    public static final String SendVideoToPlayer = "SendVIdeodjgkGs_siej345f";

    private static boolean registered = false;


    private static class ActiveVideo {
        VideoPlayer player;
        LivingEntity target;
        boolean isReady;
        float speed;

        ActiveVideo(VideoPlayer player, LivingEntity target, float speed) {
            this.player = player;
            this.target = target;
            this.speed = speed;
            this.isReady = false;
        }
    }


    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        if (ModList.get().isLoaded(WaterMedia.ID)) {
            SCHEDULER.schedule(() -> {
                preloadVideo("t0001:video/hit_skullbreak_cg2.mov");
                preloadVideo("t0001:video/impact_frames/one_inch/frame0impact.mp4");
            }, 5, TimeUnit.SECONDS);
        }
    }

    @SubscribeEvent
    public static void onShutdownClient(GameShuttingDownEvent event){
        VideoRendererUtil.shutdown();
    }

    /**
     * Preload a video for instant playback later
     * Call this during mod initialization
     *
     * @param videoLocation ResourceLocation format: "modid:video/filename.mp4"
     **/
    public static void preloadVideo(String videoLocation) {
        SCHEDULER.schedule(() -> {
            if (!PlayerAPI.isReady() && try_count.get() != max_tries) {
                LOGGER.error("PlayerAPI not ready, retrying preload for {}", videoLocation);
                preloadVideo(videoLocation);
                try_count.incrementAndGet(); // thread leak fix
                return;
            } else if (try_count.get() >= max_tries) {
                LOGGER.error("System may not have VLC Installed, please install VLC Media Player");
                return;
            }


            try {
                Path videoPath = extractVideoFromResource(videoLocation);
                if (videoPath == null) {
                    LOGGER.error("Failed to extract video for preload: {}", videoLocation);
                    return;
                }

                Minecraft.getInstance().execute(() -> {
                    try {
                        URI videoUri = videoPath.toUri();
                        VideoPlayer player = new VideoPlayer(PlayerAPI.getFactory(), Minecraft.getInstance());
                        player.startPaused(videoUri);
                        preloadedPlayers.put(videoLocation, player);
                        LOGGER.info("Video preloaded: {}", videoLocation);
                    } catch (Exception e) {
                        LOGGER.error("Failed to preload video", e);
                    }
                });
            } catch (Exception e) {
                LOGGER.error("Error during video preload", e);
            }
        }, 3L, TimeUnit.SECONDS);
    }


   public static void playVideo(String videoLocation, int PlayerId, float speed) {

        assert Minecraft.getInstance().level != null;
        Player target = (Player) Minecraft.getInstance().level.getEntity(PlayerId);
        if (target == null) {
            LOGGER.warn("Cannot play video: target is null");
            return;
        }

        AtomicReference<Path> videoPath = new AtomicReference<>();

        //Handle I/O on non-render thread
        SCHEDULER.submit(() ->
                        videoPath.set(extractVideoFromResource(videoLocation))
                );

        Minecraft.getInstance().execute(() -> {
            try {
                if (!PlayerAPI.isReady()) {
                    LOGGER.warn("PlayerAPI not ready");
                    return;
                }

                // Stop existing video if any for this target
                UUID targetUUID = target.getUUID();
                stopVideo(targetUUID);

                // Get or extract video

                if (videoPath.get() == null) {
                    LOGGER.error("Failed to get video path");
                    return;
                }

                VideoPlayer videoPlayer = null;

                // Use preloaded player if available
                try {
                    if (preloadedPlayers.containsKey(videoLocation)) {
                        videoPlayer = preloadedPlayers.remove(videoLocation);
                        videoPlayer.setRepeatMode(false);
                        videoPlayer.setSpeed(speed);
                        videoPlayer.play();
                        LOGGER.info("Using preloaded video: {}", videoLocation);
                    } else {
                        // Create new player
                        URI videoUri = videoPath.get().toUri();
                        videoPlayer = new VideoPlayer(PlayerAPI.getFactory(), Minecraft.getInstance());
                        videoPlayer.setRepeatMode(false);
                        videoPlayer.setSpeed(speed);
                        videoPlayer.start(videoUri);
                        LOGGER.info("Created new video player: {}", videoLocation);
                    }
                } catch (Exception e) {
                    if (videoPlayer != null) {
                        videoPlayer.release();
                    }
                    LOGGER.error(e.getMessage());
                    return;
                }

                // Store active video
                ActiveVideo activeVideo = new ActiveVideo(videoPlayer, target, speed);
                activeVideos.put(targetUUID, activeVideo);

                // Register renderer if needed
                if (!registered) {
                    NeoForge.EVENT_BUS.register(VideoRendererUtil.class);
                    registered = true;
                }

            } catch (Exception e) {
                LOGGER.error("Failed to play video", e);
            }
        });
    }

    /**
     * Stop video for a specific target
     */
    public static void stopVideo(UUID targetUUID) {
        ActiveVideo video = activeVideos.remove(targetUUID);
        if (video != null && video.player != null) {
            try {
                video.player.release();
                LOGGER.info("Stopped video for target: {}", targetUUID);
            } catch (Exception e) {
                LOGGER.error("Error stopping video: {}", e.getMessage());
            }
        }
    }


    /**
     * Stop all videos
     */
    public static void stopAllVideos() {
        for (UUID uuid : new HashSet<>(activeVideos.keySet())) {
            stopVideo(uuid);
        }

        for(VideoPlayer player : preloadedPlayers.values()){
            if(player != null){
                try {
                    player.release();
                } catch (Exception ignore) {
                }
            }
        }
        preloadedPlayers.clear();

    }

    /**
     * Shutdown the executor service - call this on game shutdown
     */
    public static void shutdown() {
        stopAllVideos();
        if (!SCHEDULER.isShutdown()) {
            SCHEDULER.shutdown();
            try {
                if (!SCHEDULER.awaitTermination(5, TimeUnit.SECONDS)) {
                    SCHEDULER.shutdownNow();
                }
                cachedVideoPaths.clear();
            } catch (InterruptedException e) {
                SCHEDULER.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event){
        try {
            stopVideo(event.getEntity().getUUID());
        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Pre event) {
        //Note to self: RenderStuff on RenderGuiEvent.Pre to blend subsequent gui properly
        if (!activeVideos.isEmpty() && LOGGER.isDebugEnabled()) {
            LOGGER.debug("RENDER EVENT FIRED -  activeVideos: {}", activeVideos.size());
        }

        Minecraft mc = Minecraft.getInstance();

        // Render target-specific videos (if local player matches)
        if (mc.player != null) {
            UUID playerUUID = mc.player.getUUID();
            ActiveVideo targetVideo = activeVideos.get(playerUUID);

            if (targetVideo != null) {
                renderVideo(event.getGuiGraphics(), targetVideo, mc);
            }
        }
    }

    private static void renderVideo(GuiGraphics graphics, ActiveVideo video, Minecraft mc) {
        if (video.player == null || !video.player.isSafeUse()) {
            return;
        }

        try {
            // Check if ready
            if (!video.isReady && video.player.isReady()) {
                video.isReady = true;
                LOGGER.info("Video ready! Duration: {}ms", video.player.getDuration());
            }

            if (!video.isReady) {
                return;
            }

            // Check if ended
            if (video.player.isEnded() || video.player.isStopped() || video.player.isBroken()) {
                LOGGER.info("Video ended, cleaning up");
              if (video.target != null) {
                    stopVideo(video.target.getUUID());
                }
                return;
            }


            // Render fullscreen
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();

            renderVideoTexture(graphics, video.player.texture(), 0, 0, screenWidth, screenHeight);

        } catch (Exception e) {
            LOGGER.error("Error rendering video", e);
        }
    }

    private static void renderVideoTexture(GuiGraphics graphics, int textureId, int x, int y, int width, int height) {
        //push and translate the pose to Hide Chat
        graphics.pose().pushPose();
        graphics.pose().translate(0,0,250f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, textureId);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        // Draw a fullscreen quad with  UV mapping
        buffer.addVertex(matrix, x, y, 0).setUv(0, 0);
        buffer.addVertex(matrix, x, y + height, 0).setUv(0, 1);
        buffer.addVertex(matrix, x + width, y + height, 0).setUv(1, 1);
        buffer.addVertex(matrix, x + width, y, 0).setUv(1, 0);

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
    }

    private static Path extractVideoFromResource(String videoLocation) {
        // Check cache first if not exist remove stales
        if (cachedVideoPaths.containsKey(videoLocation)) {
            Path cached = cachedVideoPaths.get(videoLocation);
            if (Files.exists(cached)) {
                return cached;
            }else cachedVideoPaths.remove(videoLocation);
        }

        try {
            // Parse resource location: "modid:video/filename.mp4"
            String[] parts = videoLocation.split(":", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid video location format. Use: modid:video/filename.ext");
            }

            String modId = parts[0];
            String videoPath = parts[1];
            String filename = videoPath.substring(videoPath.lastIndexOf('/') + 1);

            String resourcePath = "/assets/" + modId + "/" + videoPath;

            // Extract to game directory
            Path videoDir = FMLPaths.GAMEDIR.get().resolve(modId).resolve("video");
            Files.createDirectories(videoDir);
            Path videoFile = videoDir.resolve(filename);

            // Extract if not exists
            if (!Files.exists(videoFile)) {
                try (InputStream in = VideoRendererUtil.class.getResourceAsStream(resourcePath)) {
                    if (in == null) {
                        throw new FileNotFoundException("Video not found: " + resourcePath);
                    }
                    Files.copy(in, videoFile, StandardCopyOption.REPLACE_EXISTING);
                    LOGGER.info("Video extracted to: {}", videoFile);
                }
            }

            // Cache and return
            cachedVideoPaths.put(videoLocation, videoFile);
            return videoFile;

        } catch (Exception e) {
            LOGGER.error("Failed to extract video", e);
            return null;
        }
    }

}