package sid.base.network.command;


import com.lowdragmc.photon.command.FxLocationArgument;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import sid.base.main.t0001;

@EventBusSubscriber(modid = t0001.MODID)
public class ServerCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {

        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("photon")
                        .then(Commands.literal("fx")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("location", new FxLocationArgument())
                                        .then(JointEntityEffectCommand.createServerCommand())
                                )
                        )
        );

        dispatcher.register(
                Commands.literal("photon")
                        .then(Commands.literal("fx")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("location", new FxLocationArgument())
                                        .then(EntityModelEffectCommand.createServerCommand())
                                )
                        )
        );

        dispatcher.register(
                PlayCamAnimCommand.createServerCommand()
        );

        dispatcher.register(
                EntityEmitterShapeEffectCommand.createServerCommand()
        );




        dispatcher.register(StopCamAnimCommand.createServerCommand());

        SetupExecutionCommand.register(event.getDispatcher());

    }

}
