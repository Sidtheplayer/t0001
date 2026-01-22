package sid.base.utils;

//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.api.distmarker.OnlyIn;
//import net.minecraftforge.client.event.RenderGuiOverlayEvent;
//import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.loading.FMLPaths;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
//import org.watermedia.api.player.PlayerAPI;
//import org.watermedia.api.player.videolan.VideoPlayer;


/**
 * SHIT DOES NOT WORK
 * Utility class for playing videos on screen with WaterMedia
 * Can play videos for specific targets or globally
 * Usage:
 * VideoRendererUtil.playVideo("t0001:video/my_video.mp4", targetEntity, 0.5f);
 * VideoRendererUtil.playVideoGlobal("t0001:video/intro.mp4", 1.0f);
 */
@OnlyIn(Dist.CLIENT)
public class VideoRendererUtil { /*
//    private static final Map<UUID, ActiveVideo> activeVideos = new ConcurrentHashMap<>();
//    private static final Map<String, Path> cachedVideoPaths = new ConcurrentHashMap<>();
//    private static final Map<String, VideoPlayer> preloadedPlayers = new ConcurrentHashMap<>();
//    private static boolean registered = false;
//
//    private static ActiveVideo globalVideo = null;
//
//    private static class ActiveVideo {
//        VideoPlayer player;
//        LivingEntity target;
//        boolean isReady;
//        float speed;
//
//        ActiveVideo(VideoPlayer player, LivingEntity target, float speed) {
//            this.player = player;
//            this.target = target;
//            this.speed = speed;
//            this.isReady = false;
//        }
//    }
//
//    /**
//     * Preload a video for instant playback later
//     * Call this during mod initialization
//     *
//     * @param videoLocation ResourceLocation format: "modid:video/filename.mp4"
//     **/
//    public static void preloadVideo(String videoLocation) {
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                if (!PlayerAPI.isReady()) {
//                    System.out.println("PlayerAPI not ready, retrying preload for " + videoLocation);
//                    preloadVideo(videoLocation);
//                    return;
//                }
//
//                try {
//                    Path videoPath = extractVideoFromResource(videoLocation);
//                    if (videoPath == null) {
//                        System.err.println("Failed to extract video for preload: " + videoLocation);
//                        return;
//                    }
//
//                    Minecraft.getInstance().tell(() -> {
//                        try {
//                            URI videoUri = videoPath.toUri();
//                            VideoPlayer player = new VideoPlayer(PlayerAPI.getFactory(), Minecraft.getInstance());
//                            player.startPaused(videoUri);
//                            preloadedPlayers.put(videoLocation, player);
//                            System.out.println("Video preloaded: " + videoLocation);
//                        } catch (Exception e) {
//                            System.err.println("Failed to preload video: " + e.getMessage());
//                            e.printStackTrace();
//                        }
//                    });
//                } catch (Exception e) {
//                    System.err.println("Error during preload: " + e.getMessage());
//                    e.printStackTrace();
//                }
//            }
//        }, 2000L);
//    }
//
//    /**
//     * Play a video fullscreen for a specific target entity
//     * Video will play when this entity is the local player
//     *
//     * @param videoLocation ResourceLocation format: "modid:video/filename.mp4"
//     * @param target The entity this video is for (usually the player who triggered it)
//     * @param speed Video playback speed (0.1 to 3.0, normal = 1.0)
//     */
//    public static void playVideo(String videoLocation, LivingEntity target, float speed) {
//        if (target == null) {
//            System.err.println("Cannot play video: target is null");
//            return;
//        }
//
//        Minecraft.getInstance().tell(() -> {
//            try {
//                if (!PlayerAPI.isReady()) {
//                    System.err.println("PlayerAPI not ready");
//                    return;
//                }
//
//                // Stop existing video for this target
//                UUID targetUUID = target.getUUID();
//                stopVideo(targetUUID);
//
//                // Get or extract video
//                Path videoPath = extractVideoFromResource(videoLocation);
//                if (videoPath == null) {
//                    System.err.println("Failed to get video path");
//                    return;
//                }
//
//                VideoPlayer player;
//
//                // Use preloaded player if available
//                if (preloadedPlayers.containsKey(videoLocation)) {
//                    player = preloadedPlayers.remove(videoLocation);
//                    player.setRepeatMode(false);
//                    player.setSpeed(speed);
//                    player.play();
//                    System.out.println("Using preloaded video: " + videoLocation);
//                } else {
//                    // Create new player
//                    URI videoUri = videoPath.toUri();
//                    player = new VideoPlayer(PlayerAPI.getFactory(), Minecraft.getInstance());
//                    player.setRepeatMode(false);
//                    player.setSpeed(speed);
//                    player.start(videoUri);
//                    System.out.println("Created new video player: " + videoLocation);
//                }
//
//                // Store active video
//                ActiveVideo activeVideo = new ActiveVideo(player, target, speed);
//                activeVideos.put(targetUUID, activeVideo);
//
//                // Register renderer if needed
//                if (!registered) {
//                    MinecraftForge.EVENT_BUS.register(VideoRendererUtil.class);
//                    registered = true;
//                }
//
//            } catch (Exception e) {
//                System.err.println("Failed to play video: " + e.getMessage());
//                e.printStackTrace();
//            }
//        });
//    }
//
//    /**
//     * Play a video fullscreen globally (shows for everyone/local player)
//     *
//     * @param videoLocation ResourceLocation format: "modid:video/filename.mp4"
//     * @param speed Video playback speed (0.1 to 3.0, normal = 1.0)
//     */
//    public static void playVideoGlobal(String videoLocation, float speed) {
//        Minecraft.getInstance().tell(() -> {
//            try {
//                if (!PlayerAPI.isReady()) {
//                    System.err.println("PlayerAPI not ready");
//                    return;
//                }
//
//                // Stop existing global video
//                stopGlobalVideo();
//
//                // Get or extract video
//                Path videoPath = extractVideoFromResource(videoLocation);
//                if (videoPath == null) {
//                    System.err.println("Failed to get video path");
//                    return;
//                }
//
//                VideoPlayer player;
//
//                // Use preloaded player if available
//                if (preloadedPlayers.containsKey(videoLocation)) {
//                    player = preloadedPlayers.remove(videoLocation);
//                    player.setRepeatMode(false);
//                    player.setSpeed(speed);
//                    player.play();
//                } else {
//                    URI videoUri = videoPath.toUri();
//                    player = new VideoPlayer(PlayerAPI.getFactory(), Minecraft.getInstance());
//                    player.setRepeatMode(false);
//                    player.setSpeed(speed);
//                    player.start(videoUri);
//                }
//
//                globalVideo = new ActiveVideo(player, null, speed);
//
//                // Register renderer
//                if (!registered) {
//                    MinecraftForge.EVENT_BUS.register(VideoRendererUtil.class);
//                    registered = true;
//                }
//
//            } catch (Exception e) {
//                System.err.println("Failed to play global video: " + e.getMessage());
//                e.printStackTrace();
//            }
//        });
//    }
//
//    /**
//     * Stop video for a specific target
//     */
//    public static void stopVideo(UUID targetUUID) {
//        ActiveVideo video = activeVideos.remove(targetUUID);
//        if (video != null && video.player != null) {
//            try {
//                video.player.release();
//                System.out.println("Stopped video for target: " + targetUUID);
//            } catch (Exception e) {
//                System.err.println("Error stopping video: " + e.getMessage());
//            }
//        }
//    }
//
//    /**
//     * Stop the global video
//     */
//    public static void stopGlobalVideo() {
//        if (globalVideo != null && globalVideo.player != null) {
//            try {
//                globalVideo.player.release();
//                System.out.println("Stopped global video");
//            } catch (Exception e) {
//                System.err.println("Error stopping global video: " + e.getMessage());
//            }
//            globalVideo = null;
//        }
//    }
//
//    /**
//     * Stop all videos
//     */
//    public static void stopAllVideos() {
//        for (UUID uuid : new HashSet<>(activeVideos.keySet())) {
//            stopVideo(uuid);
//        }
//        stopGlobalVideo();
//    }
//
//    @SubscribeEvent
//    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
//        Minecraft mc = Minecraft.getInstance();
//
//        // Render global video first (background)
//        if (globalVideo != null) {
//            renderVideo(event.getGuiGraphics(), globalVideo, mc);
//        }
//
//        // Render target-specific videos (if local player matches)
//        if (mc.player != null) {
//            UUID playerUUID = mc.player.getUUID();
//            ActiveVideo targetVideo = activeVideos.get(playerUUID);
//
//            if (targetVideo != null) {
//                renderVideo(event.getGuiGraphics(), targetVideo, mc);
//            }
//        }
//    }
//
//    private static void renderVideo(GuiGraphics graphics, ActiveVideo video, Minecraft mc) {
//        if (video.player == null || !video.player.isSafeUse()) {
//            return;
//        }
//
//        try {
//            // Check if ready
//            if (!video.isReady && video.player.isReady()) {
//                video.isReady = true;
//                System.out.println("Video ready! Duration: " + video.player.getDuration() + "ms");
//            }
//
//            if (!video.isReady) {
//                return;
//            }
//
//            // Check if ended
//            if (video.player.isEnded() || video.player.isStopped() || video.player.isBroken()) {
//                System.out.println("Video ended, cleaning up");
//                if (video == globalVideo) {
//                    stopGlobalVideo();
//                } else if (video.target != null) {
//                    stopVideo(video.target.getUUID());
//                }
//                return;
//            }
//
//            // Render fullscreen
//            int screenWidth = mc.getWindow().getGuiScaledWidth();
//            int screenHeight = mc.getWindow().getGuiScaledHeight();
//
//            renderVideoTexture(graphics, video.player.texture(), 0, 0, screenWidth, screenHeight);
//
//        } catch (Exception e) {
//            System.err.println("Error rendering video: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    private static void renderVideoTexture(GuiGraphics graphics, int textureId, int x, int y, int width, int height) {
//        RenderSystem.enableBlend();
//        RenderSystem.setShaderTexture(0, textureId);
//        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
//        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//
//        Matrix4f matrix = graphics.pose().last().pose();
//        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
//
//        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
//        buffer.vertex(matrix, x, y, 0).uv(0, 0).endVertex();
//        buffer.vertex(matrix, x, y + height, 0).uv(0, 1).endVertex();
//        buffer.vertex(matrix, x + width, y + height, 0).uv(1, 1).endVertex();
//        buffer.vertex(matrix, x + width, y, 0).uv(1, 0).endVertex();
//
//        BufferUploader.drawWithShader(buffer.end());
//        RenderSystem.disableBlend();
//    }
//
//    private static Path extractVideoFromResource(String videoLocation) {
//        // Check cache first
//        if (cachedVideoPaths.containsKey(videoLocation)) {
//            Path cached = cachedVideoPaths.get(videoLocation);
//            if (Files.exists(cached)) {
//                return cached;
//            }
//        }
//
//        try {
//            // Parse resource location: "modid:video/filename.mp4"
//            String[] parts = videoLocation.split(":", 2);
//            if (parts.length != 2) {
//                throw new IllegalArgumentException("Invalid video location format. Use: modid:video/filename.ext");
//            }
//
//            String modId = parts[0];
//            String videoPath = parts[1];
//            String filename = videoPath.substring(videoPath.lastIndexOf('/') + 1);
//
//            String resourcePath = "/assets/" + modId + "/" + videoPath;
//
//            // Extract to game directory
//            Path videoDir = FMLPaths.GAMEDIR.get().resolve(modId).resolve("video");
//            Files.createDirectories(videoDir);
//            Path videoFile = videoDir.resolve(filename);
//
//            // Extract if not exists
//            if (!Files.exists(videoFile)) {
//                try (InputStream in = VideoRendererUtil.class.getResourceAsStream(resourcePath)) {
//                    if (in == null) {
//                        throw new FileNotFoundException("Video not found: " + resourcePath);
//                    }
//                    Files.copy(in, videoFile, StandardCopyOption.REPLACE_EXISTING);
//                    System.out.println("Video extracted to: " + videoFile);
//                }
//            }
//
//            // Cache and return
//            cachedVideoPaths.put(videoLocation, videoFile);
//            return videoFile;
//
//        } catch (Exception e) {
//            System.err.println("Failed to extract video: " + e.getMessage());
//            e.printStackTrace();
//            return null;
//        }
//    }
//    */
}