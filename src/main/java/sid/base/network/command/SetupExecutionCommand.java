package sid.base.network.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;

import org.jetbrains.annotations.Nullable;

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
                        .then(Commands.literal("execution").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                                .then(Commands.literal("setup")
                                        .then(Commands.argument("victim", EntityArgument.entity())
                                                .then(Commands.argument("attacker", EntityArgument.entity())
                                                        .then(Commands.argument("attackerAnimation", AnimationArgument.animation())
                                                                .then(Commands.argument("victimAnimation", AnimationArgument.animation())
                                                                        .then(Commands.argument("forwardOffset", DoubleArgumentType.doubleArg())
                                                                                .then(Commands.argument("rotationOffsetForVictim", FloatArgumentType.floatArg())
                                                                                        .executes((commandContext) -> {
                                                                                            return setupExecution( commandContext.getSource()
                                                                                                    , (LivingEntity) EntityArgument.getEntity(commandContext, "victim")
                                                                                                    , (LivingEntity) EntityArgument.getEntity(commandContext, "attacker")
                                                                                                    , AnimationArgument.getAnimation(commandContext, "attackerAnimation")
                                                                                                    , AnimationArgument.getAnimation(commandContext, "victimAnimation")
                                                                                                    , DoubleArgumentType.getDouble(commandContext, "forwardOffset")
                                                                                                    , FloatArgumentType.getFloat(commandContext, "rotationOffsetForVictim")
                                                                                                    , EpicFightSounds.NO_SOUND.get()
                                                                                            );
                                                                                        })
                                                                                        .then(Commands.argument("startSound", ResourceLocationArgument.id())
                                                                                                .executes((commandContext) -> {
                                                                                                    ResourceLocation soundId = ResourceLocationArgument.getId(commandContext, "startSound");
                                                                                                    SoundEvent startSound = BuiltInRegistries.SOUND_EVENT.get(soundId);

                                                                                                    return setupExecution( commandContext.getSource()
                                                                                                            , (LivingEntity) EntityArgument.getEntity(commandContext, "victim")
                                                                                                            , (LivingEntity) EntityArgument.getEntity(commandContext, "attacker")
                                                                                                            , AnimationArgument.getAnimation(commandContext, "attackerAnimation")
                                                                                                            , AnimationArgument.getAnimation(commandContext, "victimAnimation")
                                                                                                            , DoubleArgumentType.getDouble(commandContext, "forwardOffset")
                                                                                                            , FloatArgumentType.getFloat(commandContext, "rotationOffsetForVictim")
                                                                                                            , startSound
                                                                                                    );
                                                                                                })
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

    public static int setupExecution(CommandSourceStack source, LivingEntity victim, LivingEntity attacker, AnimationAccessor<? extends StaticAnimation> attackerAnimation, AnimationAccessor<? extends StaticAnimation> victimAnimation, double forwardOffset, float rotationOffsetForVictim, @Nullable SoundEvent startSound) {
        LivingEntityPatch<?> attackerPatch = EpicFightCapabilities.getEntityPatch(attacker, LivingEntityPatch.class);
        LivingEntityPatch<?> VictimPatch = EpicFightCapabilities.getEntityPatch(attacker, LivingEntityPatch.class);

        if (attackerPatch == null) {
            source.sendFailure(net.minecraft.network.chat.Component.literal("Attacker entity has no entity patch."));
            return 0;
        }

        if (VictimPatch == null) {
            source.sendFailure(net.minecraft.network.chat.Component.literal("Victim entity has no entity patch."));
            return 0;
        }

        ExecutionHandle.setup_simple_forward_execution(forwardOffset, victim, attackerPatch, attackerAnimation, victimAnimation, startSound, rotationOffsetForVictim);

        return 1;
    }
}