// ...new file...
package sid.t0001.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import sid.t0001.client.LightningBallClientHandler;

import java.util.function.Supplier;

/**
 * Packet sent from server to client to stop lightning FX on a target entity
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

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            // call client-side cleanup
            LightningBallClientHandler.stopClientFX(entityId);
        });
        ctx.setPacketHandled(true);
    }
}

