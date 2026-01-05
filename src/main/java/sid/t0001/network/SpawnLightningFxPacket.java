package sid.t0001.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import sid.t0001.main.t0001;
//import sid.t0001.skill.transition_skills.AnomalousLightningTransitionSkill;

import java.util.function.Supplier;

/**
 * Packet sent from server to client to spawn lightning FX on a target entity
 */
public record SpawnLightningFxPacket(int entityId)
        implements CustomPacketPayload {

    public static final Type<SpawnLightningFxPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(t0001.MODID, "spawn_lightning_fx"));

    public static final StreamCodec<ByteBuf, SpawnLightningFxPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    SpawnLightningFxPacket::entityId,
                    SpawnLightningFxPacket::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
