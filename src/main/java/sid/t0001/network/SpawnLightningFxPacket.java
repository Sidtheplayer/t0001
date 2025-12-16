package sid.t0001.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import sid.t0001.skill.transition_skills.AnomalousLightningTransitionSkill;

import java.util.function.Supplier;

/**
 * Packet sent from server to client to spawn lightning FX on a target entity, photon
 */
public class SpawnLightningFxPacket {
    private final int entityId;

    public SpawnLightningFxPacket(int entityId) {
        this.entityId = entityId;
    }

    public SpawnLightningFxPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            AnomalousLightningTransitionSkill.createClientSideFX(entityId);
        });
        ctx.setPacketHandled(true);
    }

}