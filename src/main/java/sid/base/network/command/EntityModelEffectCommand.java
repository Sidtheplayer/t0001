package sid.base.network.command;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.photon.Photon;
import com.lowdragmc.photon.client.fx.EntityEffectExecutor;
import com.lowdragmc.photon.client.fx.FXHelper;
import com.lowdragmc.photon.command.EffectCommand;
import com.lowdragmc.photon.command.EntityEffectCommand;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import sid.base.client.photon.executor.LivingEntityPatchEffect;
import sid.base.main.t0001;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

import static sid.base.network.command.JointEntityEffectCommand.findJoint;


public class EntityModelEffectCommand extends EffectCommand {

    public static final ResourceLocation ID = Photon.id("entity_model_effect_command");
    public static final Type<EntityModelEffectCommand> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityModelEffectCommand> CODEC = StreamCodec.ofMember(EntityModelEffectCommand::encode, EntityModelEffectCommand::decodePacket);


    protected List<Entity> entities;
    // client
    private int[] ids = new int[0];


    private EntityEffectExecutor.AutoRotate autoRotate;
    private String jointName;
    private float translationX;
    private float translationY;
    private float translationZ;
    private boolean updateRotation;


    @Override
    @Nonnull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public EntityModelEffectCommand() {
        this.autoRotate = EntityEffectExecutor.AutoRotate.NONE;
        this.jointName      = "";
        this.translationX   = 0f;
        this.translationY   = 0f;
        this.translationZ   = 0f;
        this.updateRotation = false;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    public void setAutoRotate(EntityEffectExecutor.AutoRotate autoRotate) {
        this.autoRotate = autoRotate;
    }
    public void setJointName(String jointName)            { this.jointName      = jointName;      }
    public void setTranslationX(float translationX)      { this.translationX   = translationX;   }
    public void setTranslationY(float translationY)      { this.translationY   = translationY;   }
    public void setTranslationZ(float translationZ)      { this.translationZ   = translationZ;   }
    public void setUpdateRotation(boolean updateRotation) { this.updateRotation = updateRotation; }



    public static LiteralArgumentBuilder<CommandSourceStack> createServerCommand() {
        return Commands.literal("entity_model")
                .then(Commands.argument("entities", EntityArgument.entities())
                        .executes((c) -> execute(c, false, false, false, false, false, false, false, false))
                        .then(Commands.argument("joint", StringArgumentType.string())
                                .suggests(((commandContext, suggestionsBuilder) -> {
                                    String remaining = suggestionsBuilder.getRemaining().toLowerCase();


                                    Collection<? extends Entity> entities = EntityArgument.getEntities(commandContext, "entities");

                                    //continue if size is one or all entities are of same type
                                    if (
                                            entities.size() == 1 ||
                                                    entities.stream()
                                                            .map(Entity::getType)
                                                            .distinct()
                                                            .count() == 1
                                    ) {

                                        Entity entity = entities.iterator().next();

                                        LivingEntityPatch<?> entityPatch = EpicFightCapabilities.getEntityPatch(
                                                entity,
                                                LivingEntityPatch.class
                                        );

                                        if (entityPatch != null) {

                                            // Resolve all joints from a root joint
                                            for (Joint joint : entityPatch.getArmature().rootJoint.getAllJoints()) {
                                                if (joint.getName().toLowerCase().startsWith(remaining.toLowerCase())) {
                                                    suggestionsBuilder.suggest(joint.getName());
                                                }
                                            }
                                        }
                                    }


                                    return suggestionsBuilder.buildFuture();
                                }))
                                .executes((c) -> execute(c, false, false, false, false, false, false, false, false))
                                .then(Commands.argument("rotation", Vec3Argument.vec3(false))
                                        .executes((c) -> execute(c, true, false, false, false, false, false, false, false))
                                        .then(Commands.argument("offset", Vec3Argument.vec3(false))
                                                .executes(c -> execute(c, true, true, false, false, false, false, false, false))
                                                .then(Commands.argument("translation", Vec3Argument.vec3(false))
                                                        .executes((c) -> execute(c, true, true, true, false, false, false, false, false))
                                                        .then(Commands.argument("scale", Vec3Argument.vec3(false))
                                                                .executes((c) -> execute(c, true, true, true, true, false, false, false, false))
                                                                .then(Commands.argument("delay", IntegerArgumentType.integer(0))
                                                                        .executes((c) -> execute(c, true, true, true, true, true, false, false, false))
                                                                        .then(Commands.argument("allow_multi", BoolArgumentType.bool())
                                                                                .executes((c) -> execute(c, true, true, true, true, true, true, false, false))
                                                                                .then(Commands.argument("update_rotation", BoolArgumentType.bool())
                                                                                        .executes((c) -> execute(c, true, true, true, true, true, true, true, false))
                                                                                        .then(Commands.argument("auto_rotate", new EntityEffectCommand.AutoRotateType())
                                                                                                .executes((c) -> execute(c, true, true, true, true, true, true, true, true))))))))))));
    }

    private static int execute(CommandContext<CommandSourceStack> context,
                               boolean updateRotation,
                               boolean offset,
                               boolean translation,
                               boolean rotation,
                               boolean scale,
                               boolean delay,
                               boolean allowMulti,
                               boolean autoRotate
    ) throws CommandSyntaxException {
        var command = new EntityModelEffectCommand();
        command.setLocation(ResourceLocationArgument.getId(context, "location"));
        command.setEntities(EntityArgument.getEntities(context, "entities")
                .stream().map((e) -> (Entity) e).toList());
        command.setJointName(StringArgumentType.getString(context, "joint"));

        if (offset) {
            command.setOffset(Vec3Argument.getVec3(context, "offset"));
        }

        if (rotation) {
            command.setRotation(Vec3Argument.getVec3(context, "rotation"));
        }
        if (translation) {
            Vec3 t = Vec3Argument.getVec3(context, "translation");
            command.setTranslationX((float) t.x);
            command.setTranslationY((float) t.y);
            command.setTranslationZ((float) t.z);
        }
        if (scale) {
            command.setScale(Vec3Argument.getVec3(context, "scale"));
        }
        if (delay) {
            command.setDelay(IntegerArgumentType.getInteger(context, "delay"));
        }
        if (allowMulti) {
            command.setAllowMulti(BoolArgumentType.getBool(context, "allow_multi"));
        }
        if (updateRotation) {
            command.setUpdateRotation(BoolArgumentType.getBool(context, "update_rotation"));
        }
        if (autoRotate) {
            command.setAutoRotate(EntityEffectCommand.AutoRotateType.getValue(context, "auto_rotate"));
        }

        PacketDistributor.sendToAllPlayers(command);
        return Command.SINGLE_SUCCESS;
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeEnum(this.autoRotate);
        buf.writeUtf(this.jointName);
        buf.writeFloat(this.translationX);
        buf.writeFloat(this.translationY);
        buf.writeFloat(this.translationZ);
        buf.writeBoolean(this.updateRotation);
        buf.writeVarInt(this.entities.size());
        for (Entity entity : this.entities) {
            buf.writeVarInt(entity.getId());
        }
    }

    public void decode(RegistryFriendlyByteBuf buf) {
        super.decode(buf);
        this.autoRotate     = buf.readEnum(EntityEffectExecutor.AutoRotate.class);
        this.jointName      = buf.readUtf();
        this.translationX   = buf.readFloat();
        this.translationY   = buf.readFloat();
        this.translationZ   = buf.readFloat();
        this.updateRotation = buf.readBoolean();
        this.ids = new int[buf.readVarInt()];
        for (int i = 0; i < this.ids.length; ++i) {
            this.ids[i] = buf.readVarInt();
        }
    }

    public static EntityModelEffectCommand decodePacket(RegistryFriendlyByteBuf buf) {
        var packet = new EntityModelEffectCommand();
        packet.decode(buf);
        return packet;
    }

    public static void execute(EntityModelEffectCommand packet, IPayloadContext context) {
        if (LDLib2.isClient()) {
            EntityModelEffectCommand.Client.execute(packet, context);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static class Client {
        public static void execute(EntityModelEffectCommand packet, IPayloadContext context) {
            var level = Minecraft.getInstance().level;
            if (level != null) {
                var fx = FXHelper.getFX(packet.location);
                if (fx != null) {
                    for (var id : packet.ids) {
                        var entity = level.getEntity(id);
                        if (entity != null) {

                            if (!(entity instanceof LivingEntity living)) {
                                t0001.LOGGER.warn("[JointEntityEffect] Entity {} is not LivingEntity, skipping.", id);
                                continue;
                            }

                            LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(living, LivingEntityPatch.class);
                            if (patch == null) {
                                t0001.LOGGER.warn("[JointEntityEffect] No EpicFight patch on entity {}, skipping.", id);
                                continue;
                            }

                            Joint joint = findJoint(patch, packet.jointName);

                            if (joint == null) {
                                t0001.LOGGER.warn("[JointEntityEffect] Joint '{}' not found on entity {}, skipping.",
                                        packet.jointName, id);
                                continue;
                            }

                            Vec3f translation = new Vec3f(packet.translationX, packet.translationY, packet.translationZ);



                            var effect = new LivingEntityPatchEffect(         fx,
                                    level,
                                    entity,
                                    joint,
                                    translation,
                                    packet.autoRotate,
                                    packet.updateRotation
                            );
                            var offset = packet.offset;
                            var rotation = packet.rotation;
                            var scale = packet.scale;
                            effect.setOffset(offset.x, offset.y, offset.z);
                            effect.setRotation(rotation.x, rotation.y, rotation.z);
                            effect.setScale(scale.x, scale.y, scale.z);
                            effect.setDelay(packet.delay);
                            effect.setForcedDeath(packet.forcedDeath);
                            effect.setAllowMulti(packet.allowMulti);
                            effect.start();

                        }
                    }
                }
            }
        }
    }

}
