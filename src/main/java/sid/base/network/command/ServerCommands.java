package sid.base.network.command;


import com.lowdragmc.photon.command.FxLocationArgument;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import sid.base.main.t0001;

@EventBusSubscriber(modid = t0001.MODID)
public class ServerCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("photon")
                        .then(Commands.literal("fx")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("location", new FxLocationArgument())
                                        .then(JointEntityEffectCommand.createServerCommand())
                                )
                        )
        );

        event.getDispatcher().register(
                Commands.literal(t0001.MODID)
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                        .then(PlayCamAnimCommand.createServerCommand())

        );

    }

}
