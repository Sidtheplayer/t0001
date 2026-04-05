package sid.base.network.command;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.photon.command.EffectCommand;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXHelper;
import com.lowdragmc.photon.command.EntityEffectCommand;
import com.lowdragmc.photon.client.fx.EntityEffectExecutor.AutoRotate;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
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
import sid.base.main.t0001;
import sid.base.utils.JointTrackedEntityEffect;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import javax.annotation.Nonnull;
import java.util.List;


public class JointEntityEffectCommand extends EffectCommand {

    public static final ResourceLocation ID = t0001.identifier("joint_entity_effect_command");
    public static final CustomPacketPayload.Type<JointEntityEffectCommand> TYPE;
    public static final StreamCodec<RegistryFriendlyByteBuf, JointEntityEffectCommand> CODEC;

    static {
        TYPE = new CustomPacketPayload.Type<>(ID);
        CODEC = StreamCodec.ofMember(JointEntityEffectCommand::encode, JointEntityEffectCommand::decodePacket);
    }

    // Server-side only
    protected List<Entity> entities;

    // Client-side decoded IDs
    private int[] ids = new int[0];

    private AutoRotate autoRotate;
    private String jointName;
    private float translationX;
    private float translationY;
    private float translationZ;
    private boolean updateRotation;

    public JointEntityEffectCommand() {
        this.autoRotate     = AutoRotate.NONE;
        this.jointName      = "";
        this.translationX   = 0f;
        this.translationY   = 0f;
        this.translationZ   = 0f;
        this.updateRotation = false;
    }

    @Override
    @Nonnull
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Setters
    public void setEntities(List<Entity> entities)        { this.entities       = entities;       }
    public void setAutoRotate(AutoRotate autoRotate)      { this.autoRotate     = autoRotate;     }
    public void setJointName(String jointName)            { this.jointName      = jointName;      }
    public void setTranslationX(float translationX)      { this.translationX   = translationX;   }
    public void setTranslationY(float translationY)      { this.translationY   = translationY;   }
    public void setTranslationZ(float translationZ)      { this.translationZ   = translationZ;   }
    public void setUpdateRotation(boolean updateRotation) { this.updateRotation = updateRotation; }

    public static LiteralArgumentBuilder<CommandSourceStack> createServerCommand() {
        return Commands.literal("joint_entity")
                .then(Commands.argument("entities", EntityArgument.entities())
                        .executes((c) -> execute(c, false, false, false, false, false, false, false))
                        .then(Commands.argument("joint", StringArgumentType.string())
                                .executes((c) -> execute(c, false, false, false, false, false, false, false))
                                .then(Commands.argument("rotation", Vec3Argument.vec3(false))
                                        .executes((c) -> execute(c, true, false, false, false, false, false, false))
                                        .then(Commands.argument("translation", Vec3Argument.vec3(false))
                                                .executes((c) -> execute(c, true, true, false, false, false, false, false))
                                                .then(Commands.argument("scale", Vec3Argument.vec3(false))
                                                        .executes((c) -> execute(c, true, true, true, false, false, false, false))
                                                        .then(Commands.argument("delay", IntegerArgumentType.integer(0))
                                                                .executes((c) -> execute(c, true, true, true, true, false, false, false))
                                                                .then(Commands.argument("allow_multi", BoolArgumentType.bool())
                                                                        .executes((c) -> execute(c, true, true, true, true, true, false, false))
                                                                        .then(Commands.argument("update_rotation", BoolArgumentType.bool())
                                                                                .executes((c) -> execute(c, true, true, true, true, true, true, false))
                                                                                .then(Commands.argument("auto_rotate", new EntityEffectCommand.AutoRotateType())
                                                                                        .executes((c) -> execute(c, true, true, true, true, true, true, true)))))))))));
    }

    private static int execute(CommandContext<CommandSourceStack> context,
                               boolean rotation,
                               boolean translation,
                               boolean scale,
                               boolean delay,
                               boolean allowMulti,
                               boolean updateRotation,
                               boolean autoRotate) throws CommandSyntaxException {

        JointEntityEffectCommand command = new JointEntityEffectCommand();

        command.setLocation(ResourceLocationArgument.getId(context, "location"));
        command.setEntities(EntityArgument.getEntities(context, "entities")
                .stream().map((e) -> (Entity) e).toList());
        command.setJointName(StringArgumentType.getString(context, "joint"));

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

        PacketDistributor.sendToAllPlayers(command, new CustomPacketPayload[0]);
        return 1;
    }

    // Encode order MUST match decode order
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
        this.autoRotate     = buf.readEnum(AutoRotate.class);
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

    public static JointEntityEffectCommand decodePacket(RegistryFriendlyByteBuf buf) {
        JointEntityEffectCommand packet = new JointEntityEffectCommand();
        packet.decode(buf);
        return packet;
    }

    public static void execute(JointEntityEffectCommand packet, IPayloadContext context) {
        if (LDLib2.isClient()) {
            Client.execute(packet, context);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static class Client {
        public static void execute(JointEntityEffectCommand packet, IPayloadContext context) {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) return;

            FX fx = FXHelper.getFX(packet.location);
            if (fx == null) return;

            for (int id : packet.ids) {
                Entity entity = level.getEntity(id);
                if (entity == null) continue;

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

                JointTrackedEntityEffect effect = new JointTrackedEntityEffect(
                        fx,
                        level,
                        entity,
                        joint,
                        translation,
                        packet.autoRotate,
                        packet.updateRotation
                );

                Vec3 rotation = packet.rotation;
                Vec3 scale    = packet.scale;
                effect.setRotation(rotation.x, rotation.y, rotation.z);
                effect.setScale(scale.x,       scale.y,    scale.z);
                effect.setDelay(packet.delay);
                effect.setForcedDeath(packet.forcedDeath);
                effect.setAllowMulti(packet.allowMulti);
                effect.start();
            }
        }

        // Resolves a joint by name from the entity's EpicFight armature.
        private static Joint findJoint(LivingEntityPatch<?> patch, String name) {
            try {
                return patch.getArmature().searchJointByName(name);
            } catch (Exception e) {
                t0001.LOGGER.error("[JointEntityEffect] Exception resolving joint '{}': {}", name, e.getMessage());
                return null;
            }
        }
    }
}