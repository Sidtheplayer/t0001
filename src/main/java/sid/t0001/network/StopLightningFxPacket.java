package sid.t0001.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import sid.t0001.client.LightningBallClientHandler;

import java.util.function.Supplier;

/**
 * Packet sent from server to client to stop lightning FX on a target entity
 * redundant as fuck class
 */

public class StopLightningFxPacket {
    private final int entityId;

    public StopLightningFxPacket(int entityId) {
        this.entityId = entityId;
    }

    public StopLightningFxPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> {
                LightningBallClientHandler.stopLightningFX(entityId);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}