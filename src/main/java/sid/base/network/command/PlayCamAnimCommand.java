package sid.base.network.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import sid.base.client.events.CameraAnimator;
import sid.base.main.t0001;
import yesman.epicfight.api.utils.math.Vec3f;

import javax.annotation.Nonnull;

public class PlayCamAnimCommand implements CustomPacketPayload {

    public static final ResourceLocation ID = t0001.identifier("play_cam_anim_command");
    public static final CustomPacketPayload.Type<PlayCamAnimCommand> TYPE;
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayCamAnimCommand> CODEC;

    static {
        TYPE = new CustomPacketPayload.Type<>(ID);
        CODEC = StreamCodec.ofMember(PlayCamAnimCommand::encode, PlayCamAnimCommand::decodePacket);
    }

    private String animName = "";
    private float transitionTime = 0.0f;
    private boolean loop = false;
    private Vec3f offsets = new Vec3f(0, 0, 0);

    @Override
    @Nonnull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Command Registration, I fucking hate this boilerplate
    public static LiteralArgumentBuilder<CommandSourceStack> createServerCommand() {
        return Commands.literal("play_cam_anim")
                .requires(stack -> stack.hasPermission(2))
                .then(Commands.argument("animName", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            String remaining = builder.getRemaining().toLowerCase();

                            for (String name : CameraAnimator.getInstance().getAnimationNames()) {
                                if (name.toLowerCase().startsWith(remaining)) {
                                    builder.suggest(name);
                                }
                            }

                            return builder.buildFuture();
                        })
                        .executes(c -> execute(c, 0f, false, null))

                        .then(Commands.argument("transition", FloatArgumentType.floatArg(0f))
                                .executes(c -> execute(c,
                                        FloatArgumentType.getFloat(c, "transition"),
                                        false,
                                        null))

                                .then(Commands.argument("loop", BoolArgumentType.bool())
                                        .executes(c -> execute(c,
                                                FloatArgumentType.getFloat(c, "transition"),
                                                BoolArgumentType.getBool(c, "loop"),
                                                null))

                                        .then(Commands.argument("offsets", Vec3Argument.vec3())
                                                .executes(c -> execute(c,
                                                        FloatArgumentType.getFloat(c, "transition"),
                                                        BoolArgumentType.getBool(c, "loop"),
                                                        Vec3Argument.getVec3(c, "offsets")
                                                ))
                                        )
                                )
                        )
                );
    }

    private static int execute(CommandContext<CommandSourceStack> context, float transition, boolean loop, Vec3 offsets) throws CommandSyntaxException {
        PlayCamAnimCommand packet = new PlayCamAnimCommand();
        packet.animName = StringArgumentType.getString(context, "animName");
        packet.transitionTime = transition;
        packet.loop = loop;

        if (offsets != null) {
            packet.offsets = new Vec3f((float) offsets.x, (float) offsets.y, (float) offsets.z);
        }

        PacketDistributor.sendToPlayer(context.getSource().getPlayerOrException(), packet);

        context.getSource().sendSuccess(
                () -> net.minecraft.network.chat.Component.literal("Playing cam anim: " + packet.animName),
                true
        );

        return 1;
    }

    // Networking
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(this.animName == null ? "" : this.animName);
        buf.writeFloat(this.transitionTime);
        buf.writeBoolean(this.loop);
        buf.writeFloat(this.offsets.x);
        buf.writeFloat(this.offsets.y);
        buf.writeFloat(this.offsets.z);
    }

    public void decode(RegistryFriendlyByteBuf buf) {
        this.animName = buf.readUtf();
        this.transitionTime = buf.readFloat();
        this.loop = buf.readBoolean();
        this.offsets = new Vec3f(buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public static PlayCamAnimCommand decodePacket(RegistryFriendlyByteBuf buf) {
        PlayCamAnimCommand packet = new PlayCamAnimCommand();
        packet.decode(buf);
        return packet;
    }


    public static void execute(PlayCamAnimCommand packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                Client.handle(packet);
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    private static class Client {
        public static void handle(PlayCamAnimCommand packet) {
            CameraAnimator animator = CameraAnimator.getInstance();
            if (packet.transitionTime > 0f) {
                animator.playWithTransition(
                        packet.animName,
                        packet.transitionTime
                );
            } else {
                animator.play(packet.animName, packet.loop);
            }
        }
    }
}