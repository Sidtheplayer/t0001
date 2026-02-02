package sid.base.utils;



import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
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


/**
 * Utility class for playing videos on screen with WaterMedia
 * Can play videos for specific targets or globally
 * Usage:
 * VideoRendererUtil.playVideo("t0001:video/my_video.mp4", targetEntity, 0.5f);
 * VideoRendererUtil.playVideoGlobal("t0001:video/intro.mp4", 1.0f);
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = t0001.MODID, value = Dist.CLIENT)
public class VideoRendererUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoRendererUtil.class);
    private static final Map<UUID, ActiveVideo> activeVideos = new ConcurrentHashMap<>();
    private static final Map<String, Path> cachedVideoPaths = new ConcurrentHashMap<>();
    private static final Map<String, VideoPlayer> preloadedPlayers = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1, r -> {
        Thread t = new Thread(r, "VideoRendererUtil-Scheduler");
        t.setDaemon(true);
        return t;
    });

    private static boolean registered = false;

    private static ActiveVideo globalVideo = null;



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

    /**
     * Preload a video for instant playback later
     * Call this during mod initialization
     *
     * @param videoLocation ResourceLocation format: "modid:video/filename.mp4"
     **/
    public static void preloadVideo(String videoLocation) {
        SCHEDULER.schedule(() -> {
            if (!PlayerAPI.isReady()) {
                System.out.println("PlayerAPI not ready, retrying preload for " + videoLocation);
                preloadVideo(videoLocation);
                return;
            }

            try {
                Path videoPath = extractVideoFromResource(videoLocation);
                if (videoPath == null) {
                    System.err.println("Failed to extract video for preload: " + videoLocation);
                    return;
                }

                Minecraft.getInstance().tell(() -> {
                    try {
                        URI videoUri = videoPath.toUri();
                        VideoPlayer player = new VideoPlayer(PlayerAPI.getFactory(), Minecraft.getInstance());
                        player.startPaused(videoUri);
                        preloadedPlayers.put(videoLocation, player);
                        System.out.println("Video preloaded: " + videoLocation);
                    } catch (Exception e) {
                        System.err.println("Failed to preload video: " + e.getMessage());
                        LOGGER.error("Failed to preload video", e);
                    }
                });
            } catch (Exception e) {
                System.err.println("Error during preload: " + e.getMessage());
                LOGGER.error("Error during video preload", e);
            }
        }, 2000L, TimeUnit.MILLISECONDS);
    }

    /**
     * Play a video fullscreen for a specific target entity
     * Video will play when this entity is the local player
     *
     * @param videoLocation ResourceLocation format: "modid:video/filename.mp4"
     * @param target The entity this video is for (usually the player who triggered it)
     * @param speed Video playback speed (0.1 to 3.0, normal = 1.0)
     */
    @RPCPacket("VideoPacketT0001")
    public static void playVideo(String videoLocation, Player target, float speed) {
        if (target == null) {
            System.err.println("Cannot play video: target is null");
            return;
        }

        Minecraft.getInstance().tell(() -> {
            try {
                if (!PlayerAPI.isReady()) {
                    System.err.println("PlayerAPI not ready");
                    return;
                }

                // Stop existing video if any for this target
                UUID targetUUID = target.getUUID();
                stopVideo(targetUUID);

                // Get or extract video
                Path videoPath = extractVideoFromResource(videoLocation);
                if (videoPath == null) {
                    System.err.println("Failed to get video path");
                    return;
                }

                VideoPlayer videoPlayer;

                // Use preloaded player if available
                if (preloadedPlayers.containsKey(videoLocation)) {
                    videoPlayer = preloadedPlayers.remove(videoLocation);
                    videoPlayer.setRepeatMode(false);
                    videoPlayer.setSpeed(speed);
                    videoPlayer.play();
                    System.out.println("Using preloaded video: " + videoLocation);
                } else {
                    // Create new player
                    URI videoUri = videoPath.toUri();
                    videoPlayer = new VideoPlayer(PlayerAPI.getFactory(), Minecraft.getInstance());
                    videoPlayer.setRepeatMode(false);
                    videoPlayer.setSpeed(speed);
                    videoPlayer.start(videoUri);
                    System.out.println("Created new video player: " + videoLocation);
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
                System.err.println("Failed to play video: " + e.getMessage());
                LOGGER.error("Failed to play video", e);
            }
        });
    }

    /**
     * Play a video fullscreen globally (shows for everyone/local player)
     *
     * @param videoLocation ResourceLocation format: "modid:video/filename.mp4"
     * @param speed Video playback speed (0.1 to 3.0, normal = 1.0)
     */
    public static void playVideoGlobal(String videoLocation, float speed) {
        Minecraft.getInstance().tell(() -> {
            try {
                if (!PlayerAPI.isReady()) {
                    System.err.println("PlayerAPI not ready");
                    return;
                }

                // Stop existing global video
                stopGlobalVideo();

                // Get or extract video
                Path videoPath = extractVideoFromResource(videoLocation);
                if (videoPath == null) {
                    System.err.println("Failed to get video path");
                    return;
                }

                VideoPlayer player;

                // Use preloaded player if available
                if (preloadedPlayers.containsKey(videoLocation)) {
                    player = preloadedPlayers.remove(videoLocation);
                    player.setRepeatMode(false);
                    player.setSpeed(speed);
                    player.play();
                } else {
                    URI videoUri = videoPath.toUri();
                    player = new VideoPlayer(PlayerAPI.getFactory(), Minecraft.getInstance());
                    player.setRepeatMode(false);
                    player.setSpeed(speed);
                    player.start(videoUri);
                }

                globalVideo = new ActiveVideo(player, null, speed);

                // Register renderer
                if (!registered) {
                    NeoForge.EVENT_BUS.register(VideoRendererUtil.class);
                    registered = true;
                }

            } catch (Exception e) {
                System.err.println("Failed to play global video: " + e.getMessage());
                LOGGER.error("Failed to play global video", e);
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
                System.out.println("Stopped video for target: " + targetUUID);
            } catch (Exception e) {
                System.err.println("Error stopping video: " + e.getMessage());
            }
        }
    }

    /**
     * Stop the global video
     */
    public static void stopGlobalVideo() {
        if (globalVideo != null && globalVideo.player != null) {
            try {
                globalVideo.player.release();
                System.out.println("Stopped global video");
            } catch (Exception e) {
                System.err.println("Error stopping global video: " + e.getMessage());
            }
            globalVideo = null;
        }
    }

    /**
     * Stop all videos
     */
    public static void stopAllVideos() {
        for (UUID uuid : new HashSet<>(activeVideos.keySet())) {
            stopVideo(uuid);
        }
        stopGlobalVideo();
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
            } catch (InterruptedException e) {
                SCHEDULER.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        // Render global video first (background)
        if (globalVideo != null) {
            renderVideo(event.getGuiGraphics(), globalVideo, mc);
        }

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
                System.out.println("Video ready! Duration: " + video.player.getDuration() + "ms");
            }

            if (!video.isReady) {
                return;
            }

            // Check if ended
            if (video.player.isEnded() || video.player.isStopped() || video.player.isBroken()) {
                System.out.println("Video ended, cleaning up");
                if (video == globalVideo) {
                    stopGlobalVideo();
                } else if (video.target != null) {
                    stopVideo(video.target.getUUID());
                }
                return;
            }

            // Render fullscreen
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();

            renderVideoTexture(graphics, video.player.texture(), 0, 0, screenWidth, screenHeight);

        } catch (Exception e) {
            System.err.println("Error rendering video: " + e.getMessage());
            LOGGER.error("Error rendering video", e);
        }
    }

    private static void renderVideoTexture(GuiGraphics graphics, int textureId, int x, int y, int width, int height) {
        // Note: WaterMedia's VideoPlayer.texture() returns an OpenGL texture ID
        // To render it properly, we would need to use a custom rendering context
        // For now, we'll render a black rectangle (placeholder)
        // TODO: Implement proper texture rendering using RenderSystem directly
        graphics.fill(x, y, x + width, y + height, 0xFF000000);
    }

    private static Path extractVideoFromResource(String videoLocation) {
        // Check cache first
        if (cachedVideoPaths.containsKey(videoLocation)) {
            Path cached = cachedVideoPaths.get(videoLocation);
            if (Files.exists(cached)) {
                return cached;
            }
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
                    System.out.println("Video extracted to: " + videoFile);
                }
            }

            // Cache and return
            cachedVideoPaths.put(videoLocation, videoFile);
            return videoFile;

        } catch (Exception e) {
            System.err.println("Failed to extract video: " + e.getMessage());
            LOGGER.error("Failed to extract video", e);
            return null;
        }
    }

}