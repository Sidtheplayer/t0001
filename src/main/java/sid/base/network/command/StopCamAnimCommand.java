package sid.base.network.command;

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

public class StopCamAnimCommand implements CustomPacketPayload {

    public static final ResourceLocation ID =
            t0001.identifier("stop_cam_anim_command");

    public static final CustomPacketPayload.Type<StopCamAnimCommand> TYPE;
    public static final StreamCodec<RegistryFriendlyByteBuf, StopCamAnimCommand> CODEC;

    static {
        TYPE = new CustomPacketPayload.Type<>(ID);
        CODEC = StreamCodec.ofMember(
                StopCamAnimCommand::encode,
                StopCamAnimCommand::decodePacket
        );
    }

    @Override
    @Nonnull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> createServerCommand() {
        return Commands.literal("stop_cam_anim")
                .executes(StopCamAnimCommand::executeCommand);
    }

    private static int executeCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        StopCamAnimCommand packet = new StopCamAnimCommand();

        PacketDistributor.sendToPlayer(
                context.getSource().getPlayerOrException(),
                packet
        );

        context.getSource().sendSuccess(
                () -> net.minecraft.network.chat.Component.literal(
                        "Stopped cam animation"
                ),
                true
        );

        return 1;
    }

    // Networking
    public void encode(RegistryFriendlyByteBuf buf) {
    }

    public static StopCamAnimCommand decodePacket(RegistryFriendlyByteBuf buf) {
        return new StopCamAnimCommand();
    }

    public static void execute(
            StopCamAnimCommand packet,
            IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                Client.handle(packet);
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    private static class Client {

        public static void handle(StopCamAnimCommand packet) {
            CameraAnimator.getInstance().stop();
        }
    }
}
