package sid.base.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import sid.base.client.LightningBallClientHandler;

@OnlyIn(Dist.CLIENT)
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

    public static void handleSpawnLightningFx(
            SpawnLightningFxPacket packet,
            IPayloadContext context
    ) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Entity entity = mc.level.getEntity(packet.entityId());
        if (!(entity instanceof LivingEntity target)) return;

        LightningBallClientHandler.spawnLightningFX(target);
    }

}
