package sid.base.network;


import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import sid.base.main.t0001;

/**
 * Packet sent from server to client to spawn parry/block FX at player position.
 * Made after noticing that making Parry_effect spawn code on the EFM skill itself caused dedicated server crashes.
 */

public record ParryEffectPacket(String entityUUID, boolean isParried, double posX, double posY, double posZ)
        implements CustomPacketPayload {

    public static final Type<ParryEffectPacket> TYPE =  new Type<>(ResourceLocation.fromNamespaceAndPath(t0001.MODID,"parry_effect_packet"));

    public static final StreamCodec<ByteBuf, ParryEffectPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ParryEffectPacket::entityUUID,
            ByteBufCodecs.BOOL,
            ParryEffectPacket::isParried,
            ByteBufCodecs.DOUBLE,
            ParryEffectPacket::posX,
            ByteBufCodecs.DOUBLE,
            ParryEffectPacket::posY,
            ByteBufCodecs.DOUBLE,
            ParryEffectPacket::posZ,
            ParryEffectPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


}

