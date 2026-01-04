package sid.t0001.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet sent from server to client to spawn parry/block FX at player position.
 * Made after noticing that making Parry_effect spawn code on the EFM skill itself caused dedicated server crashes.
 */
public class ParryEffectPacket {
    private final int entityId;
    private final boolean isParried;
    private final double posX;
    private final double posY;
    private final double posZ;

    public ParryEffectPacket(int entityId, boolean isParried, double posX, double posY, double posZ) {
        this.entityId = entityId;
        this.isParried = isParried;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

    public ParryEffectPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.isParried = buf.readBoolean();
        this.posX = buf.readDouble();
        this.posY = buf.readDouble();
        this.posZ = buf.readDouble();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeBoolean(isParried);
        buf.writeDouble(posX);
        buf.writeDouble(posY);
        buf.writeDouble(posZ);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> ParryEffectPacketHandler.handleParryEffect(entityId, isParried, posX, posY, posZ));
        ctx.setPacketHandled(true);
    }
}

