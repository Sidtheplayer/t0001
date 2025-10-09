package sid.t0001.skill.weaponinnate;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLPaths;
import org.watermedia.api.player.PlayerAPI;
import org.watermedia.api.player.videolan.VideoPlayer;
import sid.t0001.gameasset.t0001Animations;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;
import yesman.epicfight.world.effect.EpicFightMobEffects;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

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
        this.fifth = t0001Animations.TFU5;
        this.fail = Animations.BIPED_IDLE;
    }

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);

        container.getExecutor().getEventListener().addEventListener(EventType.ATTACK_ANIMATION_END_EVENT, EVENT_UUID, (event) -> {

            // TFU1 Animation - First Hit
            if (t0001Animations.TFU1.equals(event.getAnimation())) {
                List<LivingEntity> hurtEntities = event.getPlayerPatch().getCurrentlyActuallyHitEntities();
                if (!hurtEntities.isEmpty() && hurtEntities.get(0).isAlive()) {
                    Objects.requireNonNull(event.getPlayerPatch().getServerAnimator().getPlayerFor(null)).reset();
                    event.getPlayerPatch().reserveAnimation(this.second);

                    ServerPlayer player = event.getPlayerPatch().getOriginal();
                    PlayerChatMessage chatMessage = PlayerChatMessage.unsigned(player.getUUID(), "Pathetic");
                    event.getPlayerPatch().getOriginal().sendChatMessage(
                            new OutgoingChatMessage.Player(chatMessage),
                            false,
                            ChatType.bind(ChatType.CHAT, player)
                    );
                    event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
                } else {
                    Objects.requireNonNull(event.getPlayerPatch().getServerAnimator().getPlayerFor(null)).reset();
                    event.getPlayerPatch().reserveAnimation(this.fail);
                    event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
                }
            }

            // TFU2 Animation - Second Hit
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

            // TFU4_COPY Animation - Third Hit
            if (t0001Animations.TFU4_COPY.equals(event.getAnimation())) {
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

            // TFU4 Animation - Fourth Hit
            if (t0001Animations.TFU4.equals(event.getAnimation())) {
                List<LivingEntity> hurtEntities = event.getPlayerPatch().getCurrentlyActuallyHitEntities();
                if (!hurtEntities.isEmpty() && hurtEntities.get(0).isAlive()) {
                    Objects.requireNonNull(event.getPlayerPatch().getServerAnimator().getPlayerFor(null)).reset();
                    event.getPlayerPatch().reserveAnimation(this.fifth);
                    event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
                } else {
                    Objects.requireNonNull(event.getPlayerPatch().getServerAnimator().getPlayerFor(null)).reset();
                    event.getPlayerPatch().reserveAnimation(this.fail);
                    event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
                }
            }

            // TFU5 Animation - Final Hit with Video Playback
            if (t0001Animations.TFU5.equals(event.getAnimation())) {
                List<LivingEntity> hurtEntities = event.getPlayerPatch().getCurrentlyActuallyHitEntities();

                if (!hurtEntities.isEmpty() && hurtEntities.get(0).isAlive()) {
                    // On logical client (singleplayer or client side of server)
                    if (event.getPlayerPatch().getOriginal().level().isClientSide) {
                        ClientVideoHandler.playVideo();
                    }
                    event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
                }
            }
        });
    }

    @Override
    public void onRemoved(SkillContainer container) {
        container.getExecutor().getEventListener().removeListener(EventType.ATTACK_ANIMATION_END_EVENT, EVENT_UUID);
    }

    @Override
    public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
        container.getExecutor().playAnimationSynchronized(this.first, 0);
        container.getExecutor().getOriginal().addEffect(
                new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY.get(), 38, 10, true, false, false)
        );
    }

    @OnlyIn(Dist.CLIENT)
    public static class ClientVideoHandler {
        private static VideoPlayer activePlayer = null;
        private static Path cachedVideoPath = null;

        public static void playVideo() {
            // Clean up any existing player first
            if (activePlayer != null) {
                try {
                    activePlayer.release();
                } catch (Exception e) {
                    System.err.println("Error releasing previous video player: " + e.getMessage());
                }
                activePlayer = null;
            }

            if (!PlayerAPI.isReady()) {
                System.err.println("PlayerAPI is not ready. Video playback unavailable.");
                return;
            }

            try {
                // Ensure video is extracted
                if (cachedVideoPath == null || !Files.exists(cachedVideoPath)) {
                    extractVideoFromJar();
                }

                if (cachedVideoPath == null || !Files.exists(cachedVideoPath)) {
                    System.err.println("Video file not found after extraction attempt");
                    return;
                }

                // Create URI from file
                URI videoUri = URI.create("File:///home/sidtheplayer/.minecraft/instances/TRAS/t0001/video/hit_skullbreak_cg2.mp4");
                System.out.println("Playing video from: " + videoUri);

                // Create and start video player
                activePlayer = new VideoPlayer(
                        PlayerAPI.getFactory(),
                        Minecraft.getInstance()
                );

                activePlayer.start(videoUri);
                System.out.println("Video player started successfully");

                // Schedule cleanup after 5 seconds
                scheduleCleanup(100);

            } catch (Exception e) {
                System.err.println("Failed to play video: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private static void scheduleCleanup(int ticks) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Minecraft.getInstance().execute(() -> {
                        if (activePlayer != null) {
                            try {
                                activePlayer.release();
                                System.out.println("Video player released");
                            } catch (Exception e) {
                                System.err.println("Error releasing video player: " + e.getMessage());
                            }
                            activePlayer = null;
                        }
                    });
                }
            }, ticks * 50L);
        }

        private static synchronized void extractVideoFromJar() {
            try {
                String modId = "t0001";
                String videoFilename = "hit_skullbreak_cg2.mp4";
                String resourcePath = "/assets/" + modId + "/video/" + videoFilename;

                Path videoDir = FMLPaths.GAMEDIR.get().resolve(modId).resolve("video");
                Files.createDirectories(videoDir);

                Path videoFile = videoDir.resolve(videoFilename);

                // Always extract to ensure we have the file
                try (InputStream in = ClientVideoHandler.class.getResourceAsStream(resourcePath)) {
                    if (in == null) {
                        throw new FileNotFoundException("Video resource not found in JAR: " + resourcePath);
                    }

                    Files.copy(in, videoFile, StandardCopyOption.REPLACE_EXISTING);
                    cachedVideoPath = videoFile;
                    System.out.println("Video extracted to: " + videoFile.toAbsolutePath());
                }
            } catch (Exception e) {
                System.err.println("Failed to extract video: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}