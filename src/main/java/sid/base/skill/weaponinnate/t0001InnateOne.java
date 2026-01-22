package sid.base.skill.weaponinnate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import sid.base.gameasset.t0001Animations;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.registry.entries.EpicFightMobEffects;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.StunType;


import java.util.List;
import java.util.function.Function;


public class t0001InnateOne extends WeaponInnateSkill {


    public static final class Builder extends WeaponInnateSkill.Builder<Builder> {
        public Builder(Function<t0001InnateOne.Builder, ? extends Skill> constructor) {
            super(constructor);
        }
    }


    public static Builder createT0001InnateBuilder() {
        return new Builder(t0001InnateOne::new)
                .setCategory(SkillCategories.WEAPON_INNATE)
                .setResource(Resource.WEAPON_CHARGE);
    }


    private final AssetAccessor<? extends StaticAnimation> first;
    private final AssetAccessor<? extends StaticAnimation> second;
    private final AssetAccessor<? extends StaticAnimation> third;
    private final AssetAccessor<? extends StaticAnimation> fourth;
    private final AssetAccessor<? extends StaticAnimation> fifth;

    public t0001InnateOne(Builder builder) {
        super(builder);

        this.first = t0001Animations.TFU1;
        this.second = t0001Animations.TFU2;
        this.third = t0001Animations.TFU4_COPY;
        this.fourth = t0001Animations.TFU4;
        this.fifth = t0001Animations.TFU5_REMADE;
    }

    public AssetAccessor<? extends StaticAnimation> dynamic_fail_animation = null;


    //HUGE thanks to Yonichi(refm) and arcane(Ascended arts)!
    // note to self - check if statements' indentations, if something doesn't work after you add another anim.

    private LivingEntity opponentEntity = null;

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);


        eventListener.registerEvent(
                EpicFightEventHooks.Animation.END,
                event -> {
                    this.dynamic_fail_animation = event.getEntityPatch().getAnimator().getLivingAnimation(LivingMotions.IDLE, Animations.BIPED_IDLE);

                    if (event.getAnimation().equals(this.first)) {
                        List<LivingEntity> hurtEntities = event.getEntityPatch().getCurrentlyActuallyHitEntities();

                        if (!hurtEntities.isEmpty() && hurtEntities.getFirst().isAlive()) {
                            event.getEntityPatch().reserveAnimation(this.second);
                            //the "Haaaah!" sounds
                            event.getEntityPatch().playSound(SoundEvents.VILLAGER_HURT, 75, 0, 155);
                            ServerPlayer player = (ServerPlayer) event.getEntityPatch().getOriginal();
                            PlayerChatMessage chatMessage = PlayerChatMessage.unsigned(player.getUUID(), "Pathetic");
                            player.sendChatMessage(
                                    new OutgoingChatMessage.Player(chatMessage),
                                    false,//If ykyk
                                    ChatType.bind(ChatType.TEAM_MSG_COMMAND_INCOMING, player)
                            );
                            event.getEntityPatch().getCurrentlyActuallyHitEntities().clear();

                        } else {
                            event.getEntityPatch().reserveAnimation(this.dynamic_fail_animation);
                            event.getEntityPatch().getCurrentlyActuallyHitEntities().clear();
                        }
                    }

                    if (this.second.equals(event.getAnimation())) {
                        List<LivingEntity> hurtEntities = event.getEntityPatch().getCurrentlyActuallyHitEntities();
                        if (!hurtEntities.isEmpty() && hurtEntities.getFirst().isAlive()) {
                            event.getEntityPatch().reserveAnimation(this.third);
                            event.getEntityPatch().getCurrentlyActuallyHitEntities().clear();
                        } else {
                            event.getEntityPatch().playAnimationSynchronized(this.dynamic_fail_animation, 0.2F);
                            event.getEntityPatch().getCurrentlyActuallyHitEntities().clear();
                        }
                    }

                    if (this.third.equals(event.getAnimation())) {
                        // was supposed to use TFU3 but I "accidentally" broke the anim in blender
                        List<LivingEntity> hurtEntities = event.getEntityPatch().getCurrentlyActuallyHitEntities();
                        if (!hurtEntities.isEmpty() && hurtEntities.getFirst().isAlive()) {
                            event.getEntityPatch().reserveAnimation(this.fourth);
                            event.getEntityPatch().getAngleTo(hurtEntities.getFirst());
                            event.getEntityPatch().getCurrentlyActuallyHitEntities().clear();
                        } else {
                            event.getEntityPatch().reserveAnimation(this.dynamic_fail_animation);
                            event.getEntityPatch().getCurrentlyActuallyHitEntities().clear();
                        }
                    }

                    if (this.fourth.equals(event.getAnimation())) {
                        List<LivingEntity> hurtEntities = event.getEntityPatch().getCurrentlyActuallyHitEntities();
                        if (!hurtEntities.isEmpty() && hurtEntities.getFirst().isAlive()) {
                            event.getEntityPatch().reserveAnimation(this.fifth);
                            opponentEntity = hurtEntities.getFirst();
                        } else {
                            event.getEntityPatch().reserveAnimation(this.dynamic_fail_animation);
                            event.getEntityPatch().getCurrentlyActuallyHitEntities().clear();
                        }
                    }
                    if (this.fifth.equals(event.getAnimation())) {
                        List<LivingEntity> hurtEntities = event.getEntityPatch().getCurrentlyActuallyHitEntities();
                        opponentEntity = hurtEntities.getFirst();
                        if (opponentEntity != null && opponentEntity.isAlive()) {
                            opponentEntity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 55, 6, false, false, false));
                            opponentEntity.addTag("SetToFallBoom");
                            LivingEntityPatch<?> oppatch = EpicFightCapabilities.getEntityPatch(opponentEntity,LivingEntityPatch.class);
                            oppatch.applyStun(StunType.HOLD,10.0F);
                        }

                    }

                }, this,-1);


    }



// FIXME : cutscenes will be re added at later time
//    @OnlyIn(Dist.CLIENT)
//    @Override
//    public void onRemoveClient(SkillContainer container) {
//        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> VideoOverlayRenderer::stop);
//    }

//    @OnlyIn(Dist.CLIENT)
//    @Override
//    public void onInitiateClient(SkillContainer container) {
//
//        EpicFightCameraAPI instance = EpicFightCameraAPI.getInstance();
//        if(container.getClientExecutor().getTarget() != null) {
//            instance.toggleLockOn();
//        }
//
//
//    }

    @Override
    public void executeOnServer(SkillContainer container, CompoundTag arguments) {
        super.executeOnServer(container, arguments);
        container.getExecutor().playAnimationSynchronized(this.first, 0);
        container.getExecutor().getOriginal().addEffect(
                new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY, 38, 2, true, false, false)
        );


    }
//
//    //TODO: Clean up this mess later and separate into client and server classes and use it as a universal video overlay renderer
//    @SuppressWarnings("CallToPrintStackTrace")
//    @OnlyIn(Dist.CLIENT)
//    public static class VideoOverlayRenderer {
//        private static VideoPlayer videoPlayer = null;
//        private static VideoPlayer preloadedPlayer = null;
//        private static Path cachedVideoPath = null;
//        private static boolean registered = false;
//        private static boolean isReady = false;
//
//        // I am an imposter, why does this work? if this work lets not touch it - can see my dumbahh breaking this rule nex day
//        public static void preloadVideo() {
//            // I lost more hair while trying to get this thing to work than deriving an organic chem equation from scratch
//            // i got all my hair back so its all good now.
//            new Timer().schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    if (!PlayerAPI.isReady()) {
//                        System.out.println("PlayerAPI not ready, retrying preload in 1 second...");
//                        preloadVideo(); // Retry
//                        return;
//                    }
//
//                    if (cachedVideoPath == null || !Files.exists(cachedVideoPath)) {
//                        extractVideoFromJar();
//                    }
//
//                    if (cachedVideoPath == null) {
//                        System.err.println("Failed to extract video for preload");
//                        return;
//                    }
//
//                    Minecraft.getInstance().tell(() -> {
//                        try {
//                            if (preloadedPlayer == null) {
//                                URI videoUri = cachedVideoPath.toUri();
//                                preloadedPlayer = new VideoPlayer(PlayerAPI.getFactory(), Minecraft.getInstance());
//                                preloadedPlayer.startPaused(videoUri);
//                                System.out.println("Video preloaded successfully!");
//                            }
//                        } catch (Exception e) {
//                            System.err.println("Failed to preload video: " + e.getMessage());
//                            e.printStackTrace();
//                        }
//                    });
//                }
//            }, 2000L); // pre-delay to preload :? next week im gonna forget what all this code even does
//        }
//
//        public static void startVideo() {
//            // Extracts video first
//            if (cachedVideoPath == null || !Files.exists(cachedVideoPath)) {
//                extractVideoFromJar();
//            }
//
//            if (cachedVideoPath == null) {
//                System.err.println("Failed to extract video");
//                return;
//            }
//
//            Minecraft.getInstance().tell(() -> {
//                try {
//                    if (!PlayerAPI.isReady()) {
//                        System.err.println("PlayerAPI not ready");
//                        return;
//                    }
//
//                    if (videoPlayer != null) {
//                        videoPlayer.release();
//                    }
//
//
//                    if (preloadedPlayer != null) {
//                        videoPlayer = preloadedPlayer;
//                        preloadedPlayer = null;
//                        videoPlayer.setRepeatMode(false);
//                        videoPlayer.setSpeed(0.35F);
//                        videoPlayer.play();
//                        isReady = true;
//                        System.out.println("Using preloaded video player");
//                    } else {
//
//                        URI videoUri = cachedVideoPath.toUri();
//                        videoPlayer = new VideoPlayer(PlayerAPI.getFactory(), Minecraft.getInstance());
//                        videoPlayer.setRepeatMode(false);
//                        videoPlayer.setSpeed(0.36F);
//                        videoPlayer.start(videoUri);
//                        isReady = false;
//                        System.out.println("Video player created (not preloaded)");
//                    }
//
//
//                    if (!registered) {
//                        MinecraftForge.EVENT_BUS.register(VideoOverlayRenderer.class);
//                        registered = true;
//                    }
//
//                } catch (Exception e) {
//                    System.err.println("Failed to start video: " + e.getMessage());
//                    e.printStackTrace();
//                }
//            });
//        }
//
//        @SubscribeEvent
//        public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
//            if (videoPlayer == null || !videoPlayer.isSafeUse()) {
//                return;
//            }
//
//            try {
//
//                if (!isReady && videoPlayer.isReady()) {
//                    isReady = true;
//                    long duration = videoPlayer.getDuration();
//                    System.out.println("Video is ready! Duration: " + duration + "ms");
//                }
//
//                if (!isReady) {
//                    return;
//                }
//
//
//                if (videoPlayer.isEnded()) {
//                    System.out.println("Video ended naturally, stopping playback");
//                    stop();
//                    return;
//                }
//
//
//                if (videoPlayer.isStopped() || videoPlayer.isBroken()) {
//                    System.out.println("Video stopped or broken, cleaning up");
//                    stop();
//                    return;
//                }
//
//                Minecraft mc = Minecraft.getInstance();
//                int screenWidth = mc.getWindow().getGuiScaledWidth();
//                int screenHeight = mc.getWindow().getGuiScaledHeight();
//
//                renderVideoTexture(event.getGuiGraphics(), videoPlayer.texture(),
//                        screenWidth, screenHeight);
//
//            } catch (Exception e) {
//                System.err.println("Error rendering video: " + e.getMessage());
//                e.printStackTrace();
//            }
//        }
//
//        private static void renderVideoTexture(GuiGraphics graphics, int textureId, int width, int height) {
//            RenderSystem.enableBlend();
//            RenderSystem.setShaderTexture(0, textureId);
//            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
//            RenderSystem.setShader(GameRenderer::getPositionTexShader);
//
//            Matrix4f matrix = graphics.pose().last().pose();
//            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
//
//            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
//            buffer.vertex(matrix, 0, 0, 0).uv(0, 0).endVertex();
//            buffer.vertex(matrix, 0, height, 0).uv(0, 1).endVertex();
//            buffer.vertex(matrix, width, height, 0).uv(1, 1).endVertex();
//            buffer.vertex(matrix, width, 0, 0).uv(1, 0).endVertex();
//
//            BufferUploader.drawWithShader(buffer.end());
//            RenderSystem.disableBlend();
//        }
//
//        public static void stop() {
//            if (videoPlayer != null) {
//                try {
//                    videoPlayer.release();
//                    System.out.println("Video player stopped and released");
//                } catch (Exception e) {
//                    System.err.println("Error stopping video: " + e.getMessage());
//                }
//                videoPlayer = null;
//                isReady = false;
//            }
//        }
//
//        @OnlyIn(Dist.CLIENT)
//        private static synchronized void extractVideoFromJar() {
//            try {
//                if (cachedVideoPath != null && Files.exists(cachedVideoPath)) {
//                    return;
//                }
//
//                String modId = "t0001";
//                String videoFilename = "hit_skullbreak_cg2.mov";
//                String resourcePath = "/assets/" + modId + "/video/" + videoFilename;
//
//                Path videoDir = FMLPaths.GAMEDIR.get().resolve(modId).resolve("video");
//                Files.createDirectories(videoDir);
//                Path videoFile = videoDir.resolve(videoFilename);
//
//                try (InputStream in = VideoOverlayRenderer.class.getResourceAsStream(resourcePath)) {
//                    if (in == null) {
//                        throw new FileNotFoundException("Video not found: " + resourcePath);
//                    }
//                    Files.copy(in, videoFile, StandardCopyOption.REPLACE_EXISTING);
//                    cachedVideoPath = videoFile;
//                    System.out.println("Video extracted to: " + videoFile);
//                }
//            } catch (Exception e) {
//                System.err.println("Failed to extract video: " + e.getMessage());
//                e.printStackTrace();
//            }
//        }
//    }
}