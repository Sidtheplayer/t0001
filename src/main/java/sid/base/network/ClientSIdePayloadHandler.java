package sid.base.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;


public final class ClientSIdePayloadHandler {

    public static void handleParryEffect(
            final ParryEffectPacket packet,
            final IPayloadContext context
    ) {
        ParryEffectPacketHandler.handleParryEffect(
                packet.entityUUID(),
                packet.isParried(),
                packet.posX(),
                packet.posY(),
                packet.posZ()
        );
    }


}
