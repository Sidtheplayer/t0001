package sid.base.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import sid.base.main.t0001;
import sid.base.network.command.EntityModelEffectCommand;
import sid.base.network.command.JointEntityEffectCommand;
import sid.base.network.command.PlayCamAnimCommand;

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
                PlayCamAnimCommand.TYPE,
                PlayCamAnimCommand.CODEC,
                PlayCamAnimCommand::execute
        );

        registrar.playToClient(JointEntityEffectCommand.TYPE,JointEntityEffectCommand.CODEC,JointEntityEffectCommand::execute);

        registrar.playToClient(EntityModelEffectCommand.TYPE, EntityModelEffectCommand.CODEC, EntityModelEffectCommand::execute);

    }

}