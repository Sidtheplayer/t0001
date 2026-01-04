package sid.t0001.network;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import sid.t0001.main.t0001;

/**
 * read neoforge docs for more info
 */
@EventBusSubscriber(modid = t0001.MODID)
public class t0001NetworkManager {
    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToClient(
                ParryEffectPacket.TYPE,
                ParryEffectPacket.STREAM_CODEC,
                ClientSIdePayloadHandler::handleParryEffect
        );

        registrar.playToClient(
                SpawnLightningFxPacket.TYPE,
                SpawnLightningFxPacket.STREAM_CODEC,
                ClientSIdePayloadHandler::handleSpawnLightningFx
        );




    }

}