package sid.base.network.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
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

    private ResourceLocation animId = ResourceLocation.fromNamespaceAndPath("minecraft", "empty");
    private boolean loop = false;
    private boolean lockMousePanning = false;
    private boolean useWorldSpace = false;

    @Override
    @Nonnull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Command Registration, I fucking hate this boilerplate
    public static LiteralArgumentBuilder<CommandSourceStack> createServerCommand() {
        return Commands.literal("play_cam_anim")
                .then(Commands.argument("anim", ResourceLocationArgument.id())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(
                                CameraAnimator.getInstance().getAnimationNames(), builder))
                        .executes(c -> execute(c, false, false, false))
                        .then(Commands.argument("loop", BoolArgumentType.bool())
                                .executes(c -> execute(c,
                                        BoolArgumentType.getBool(c, "loop"), false, false))
                                .then(Commands.argument("lockMousePanning", BoolArgumentType.bool())
                                        .executes(c -> execute(c,
                                                BoolArgumentType.getBool(c, "loop"),
                                                BoolArgumentType.getBool(c, "lockMousePanning"),
                                                false))
                                        .then(Commands.argument("useWorldSpace", BoolArgumentType.bool())
                                                .executes(c -> execute(c,
                                                        BoolArgumentType.getBool(c, "loop"),
                                                        BoolArgumentType.getBool(c, "lockMousePanning"),
                                                        BoolArgumentType.getBool(c, "useWorldSpace"))))
                                )
                        )
                );
    }


    private static int execute(CommandContext<CommandSourceStack> context, boolean loop,
                               boolean lockMousePanning, boolean useWorldSpace) throws CommandSyntaxException {
        PlayCamAnimCommand packet = new PlayCamAnimCommand();
        packet.animId = ResourceLocationArgument.getId(context, "anim");
        packet.loop = loop;
        packet.lockMousePanning = lockMousePanning;
        packet.useWorldSpace = useWorldSpace;

        PacketDistributor.sendToPlayer(context.getSource().getPlayerOrException(), packet);
        context.getSource().sendSuccess(
                () -> Component.literal("Playing cam anim: " + packet.animId), true);
        return 1;
    }

    // Networking
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeResourceLocation(this.animId);
        buf.writeBoolean(this.loop);
        buf.writeBoolean(this.lockMousePanning);
        buf.writeBoolean(this.useWorldSpace);
    }

    public void decode(RegistryFriendlyByteBuf buf) {
        this.animId = buf.readResourceLocation();
        this.loop = buf.readBoolean();
        this.lockMousePanning = buf.readBoolean();
        this.useWorldSpace = buf.readBoolean();
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

            animator.play(
                    new CameraAnimator.CamAnim(packet.animId),
                    packet.loop,
                    packet.lockMousePanning,
                    packet.useWorldSpace,
                    null
            );

        }
    }
}