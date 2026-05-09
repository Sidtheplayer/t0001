package sid.base.network.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import sid.base.client.events.CameraAnimator;
import sid.base.main.t0001;

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
    private boolean loop = false;
    private boolean lockMousePanning = false;

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
                        .executes(c -> execute(c, false, false))


                        .then(Commands.argument("loop", BoolArgumentType.bool())
                                .executes(c -> execute(c,

                                        BoolArgumentType.getBool(c, "loop"),
                                        false))

                                .then(Commands.argument("lockMousePanning", BoolArgumentType.bool())
                                        .executes(c -> execute(c,
                                                BoolArgumentType.getBool(c, "loop"),
                                                BoolArgumentType.getBool(c, "lockMousePanning")
                                        ))


                                )
                        )

                );
    }

    private static int execute(CommandContext<CommandSourceStack> context, boolean loop, boolean lockMousePanning) throws CommandSyntaxException {
        PlayCamAnimCommand packet = new PlayCamAnimCommand();
        packet.animName = StringArgumentType.getString(context, "animName");
        packet.loop = loop;
        packet.lockMousePanning = lockMousePanning;


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
        buf.writeBoolean(this.loop);
        buf.writeBoolean(this.lockMousePanning);
    }

    public void decode(RegistryFriendlyByteBuf buf) {
        this.animName = buf.readUtf();
        this.loop = buf.readBoolean();
        this.lockMousePanning = buf.readBoolean();
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

            animator.playWithOption(
                    packet.animName,
                    packet.loop,
                    packet.lockMousePanning
            );

        }
    }
}