package sid.base.network.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

import net.minecraft.world.phys.Vec3;
import sid.base.events.ExecutionHandle;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.server.commands.arguments.AnimationArgument;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class SetupExecutionCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("epicfight")
                        .then(Commands.literal("execution")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.literal("setup")
                                        .then(Commands.argument("victim", EntityArgument.entity())
                                                .then(Commands.argument("attacker", EntityArgument.entity())
                                                        .then(Commands.argument("attackerAnimation", AnimationArgument.animation())
                                                                .then(Commands.argument("victimAnimation", AnimationArgument.animation())
                                                                        .then(Commands.argument("offsetX", DoubleArgumentType.doubleArg())
                                                                                .then(Commands.argument("offsetY", DoubleArgumentType.doubleArg())
                                                                                        .then(Commands.argument("offsetZ", DoubleArgumentType.doubleArg())
                                                                                                .then(Commands.argument("rotationOffsetForVictim", FloatArgumentType.floatArg())
                                                                                                        .then(Commands.argument("inverseEyePos", BoolArgumentType.bool())
                                                                                                                .executes(ctx -> setupExecution(
                                                                                                                        ctx.getSource(),
                                                                                                                        (LivingEntity) EntityArgument.getEntity(ctx, "victim"),
                                                                                                                        (LivingEntity) EntityArgument.getEntity(ctx, "attacker"),
                                                                                                                        AnimationArgument.getAnimation(ctx, "attackerAnimation"),
                                                                                                                        AnimationArgument.getAnimation(ctx, "victimAnimation"),
                                                                                                                        new Vec3(
                                                                                                                                DoubleArgumentType.getDouble(ctx, "offsetX"),
                                                                                                                                DoubleArgumentType.getDouble(ctx, "offsetY"),
                                                                                                                                DoubleArgumentType.getDouble(ctx, "offsetZ")
                                                                                                                        ),
                                                                                                                        FloatArgumentType.getFloat(ctx, "rotationOffsetForVictim"),
                                                                                                                        BoolArgumentType.getBool(ctx, "inverseEyePos")
                                                                                                                ))
                                                                                                        )
                                                                                                )
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
        );
    }

    public static int setupExecution(
            CommandSourceStack source,
            LivingEntity victim,
            LivingEntity attacker,
            AnimationAccessor<? extends StaticAnimation> attackerAnimation,
            AnimationAccessor<? extends StaticAnimation> victimAnimation,
            Vec3 positionOffset,
            float rotationOffsetForVictim,
            boolean inverseEyePos
    ) {

        LivingEntityPatch<?> attackerPatch =
                EpicFightCapabilities.getEntityPatch(attacker, LivingEntityPatch.class);

        if (attackerPatch == null) {
            source.sendFailure(Component.literal("Attacker entity has no entity patch."));
            return 0;
        }

        if (EpicFightCapabilities.getEntityPatch(victim, LivingEntityPatch.class) == null) {
            source.sendFailure(Component.literal("Victim entity has no entity patch."));
            return 0;
        }

        ExecutionHandle.setup_simple_forward_execution(
                victim,
                attackerPatch,
                attackerAnimation,
                victimAnimation,
                EpicFightSounds.NO_SOUND.get(),
                rotationOffsetForVictim,
                positionOffset,
                inverseEyePos
        );

        return 1;
    }
}