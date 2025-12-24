package sid.t0001.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import sid.t0001.skill.transition_skills.AnomalousLightningTransitionSkill;

import java.util.function.Supplier;

/**
 * Packet sent from server to client to spawn lightning FX on a target entity
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

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Execute on client to avoid problemo
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                sid.t0001.client.LightningBallClientHandler.spawnLightningFX(entityId);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}