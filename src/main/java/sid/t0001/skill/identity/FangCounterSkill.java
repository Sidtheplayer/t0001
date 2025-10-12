package sid.t0001.skill.identity;

import com.google.common.collect.Maps;
import com.lowdragmc.photon.client.fx.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3d;
import sid.t0001.gameasset.t0001Animations;
import sid.t0001.skill.t0001SkillDataKeys;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;
import yesman.epicfight.world.entity.eventlistener.TakeDamageEvent;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;


public class FangCounterSkill extends Skill {
    private static final UUID EVENT_UUID = UUID.fromString("7eaf7af7-2622-40a8-acbc-dee925e3aec3");

    public static Builder createFangCounterSkillBuilder() {
        return (new Builder())
                .addMotion(WeaponCategories.FIST, (item, player) -> t0001Animations.FANG_COUNTER)
                .addMotion(WeaponCategories.DAGGER,(item, player) -> t0001Animations.FANG_COUNTER)
                .addMotion(WeaponCategories.UCHIGATANA,(item, player) -> t0001Animations.FANG_COUNTER)
                .setCategory(SkillCategories.IDENTITY)
                .setActivateType(ActivateType.ONE_SHOT)
                .setResource(Resource.NONE);
    }

    @SuppressWarnings("removal")
    private static void accept(TakeDamageEvent.Attack event) {
        if (event.getResult() != AttackResult.ResultType.BLOCKED) return;

        ServerPlayer serverPlayer = event.getPlayerPatch().getOriginal();
        if (serverPlayer == null) return;
//Graheh

        String command = "";
        if (!serverPlayer.level().isClientSide() && event.isParried()) {

            BiFunction<Entity, Entity, Vector3d> FRONT_OF_EYES = (target, attacker) -> {
                Vec3 eyePosition = target.getEyePosition();
                Vec3 viewVec = target.getLookAngle().scale(1.0D);

                return new Vector3d(eyePosition.x + viewVec.x, eyePosition.y + viewVec.y - 0.27D, eyePosition.z + viewVec.z);
            };


            Vector3d frontOfEyes = FRONT_OF_EYES.apply(serverPlayer, null);


            BlockPos effectPos = new BlockPos((int) frontOfEyes.x, (int) frontOfEyes.y, (int) frontOfEyes.z);

            FX breakclashfx = FXHelper.getFX(new ResourceLocation("photon:breakclash4"));
            BlockEffect parry_effect = new BlockEffect(breakclashfx, serverPlayer.level(), effectPos);

            double offsetX = frontOfEyes.x - effectPos.getX() - 0.5; // we gonna do -0.5 because BlockEffect adds 0.5
            double offsetY = frontOfEyes.y - effectPos.getY() - 0.5;
            double offsetZ = frontOfEyes.z - effectPos.getZ() - 0.5;

            parry_effect.setOffset(offsetX, offsetY, offsetZ);
            parry_effect.setRotation(0, 0, 0);
            parry_effect.setScale(0.95, 0.95, 0.95);
            parry_effect.setDelay(0);
            parry_effect.setForcedDeath(false);
            parry_effect.setAllowMulti(true);

            parry_effect.start();

        } else {
            command = "/photon fx photon:block entity @s 0 0.35 0 0 0 0 1 1 1 0 true true xrot";
        }

        Objects.requireNonNull(serverPlayer.getServer()).getCommands().performPrefixedCommand(
                serverPlayer.getServer().createCommandSourceStack()
                        .withEntity(serverPlayer)
                        .withSuppressedOutput(),
                command
        );
    }

    public static class Builder extends SkillBuilder<FangCounterSkill> {
        protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, AnimationAccessor<? extends StaticAnimation>>> motions = Maps.newHashMap();
        public Builder addMotion(WeaponCategory weaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, AnimationAccessor<? extends StaticAnimation>> function) {
            this.motions.put(weaponCategory, function);
            return this;
        }
    }

    protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, AnimationAccessor<? extends StaticAnimation>>> motions;

    protected final Map<EntityType<?>, Integer> KillIncrement = Maps.newHashMap();

    protected int COST;
    protected int MAX_SUPER_STACKS;


    @Override
    public void setParams(CompoundTag parameters) {
        super.setParams(parameters);

        this.KillIncrement.clear();

        this.MAX_SUPER_STACKS = parameters.getInt("max_super_stacks");
        this.COST = parameters.getInt("cost");


        CompoundTag increments = parameters.getCompound("KillIncrement");

        for (String registryName : increments.getAllKeys()) {
            EntityType<?> entityType = EntityType.byString(registryName).orElse(null);

            if (entityType != null) {
                this.KillIncrement.put(entityType, increments.getInt(registryName));
            } else {
                EpicFightMod.LOGGER.warn("FangCounterSkill registry error: no entity type named : {}", registryName);
            }
        }
    }




    public FangCounterSkill(Builder builder) {
        super(builder);
        this.motions = builder.motions;
    }


        @Override
        public void onInitiate(SkillContainer container) {
            PlayerEventListener listener = container.getExecutor().getEventListener();

            listener.addEventListener(EventType.TAKE_DAMAGE_EVENT_ATTACK, EVENT_UUID, FangCounterSkill::accept);

            listener.addEventListener(EventType.PLAYER_KILLED_EVENT, EVENT_UUID, (event) -> {
                int cur = container.getDataManager().getDataValue(t0001SkillDataKeys.SUPER_STACKS.get());

                EntityType<?> type = event.getKilledEntity() != null ? event.getKilledEntity().getType() : null;
                int increment = (type != null) ? KillIncrement.getOrDefault(type, 1) : 1;

                int next = cur + increment;
                if (next > MAX_SUPER_STACKS) {
                    next = MAX_SUPER_STACKS;
                }

                container.getDataManager().setDataSync(t0001SkillDataKeys.SUPER_STACKS.get(), next);
            });

            listener.addEventListener(EventType.SKILL_CAST_EVENT, EVENT_UUID, (event) -> {
                if (!container.getExecutor().isLogicalClient()) return;

                int stacks = container.getDataManager().getDataValue(t0001SkillDataKeys.SUPER_STACKS.get());
                Skill skill = event.getSkillContainer().getSkill();

                if (skill.getCategory() != SkillCategories.BASIC_ATTACK){
                    return;
                }

                boolean GuardKeyPressed = EpicFightKeyMappings.GUARD.isDown();

                // fucking works, but I want to touch it so bad but its against the rules :C
                if (container.getExecutor().getTarget() != null && stacks >= COST && GuardKeyPressed) {
                    if (container.sendCastRequest((LocalPlayerPatch) container.getExecutor(),
                            ClientEngine.getInstance().controlEngine).isExecutable()) {
                        event.setCanceled(true);
                        EpicFightKeyMappings.GUARD.consumeClick();
                    }
                }
            });
    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
        var el = container.getExecutor().getEventListener();
        el.removeListener(EventType.PLAYER_KILLED_EVENT, EVENT_UUID);
        el.removeListener(EventType.SKILL_CAST_EVENT, EVENT_UUID);
    }






    @Override
    public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
        super.executeOnServer(container, args);

        int stacks = container.getDataManager().getDataValue(t0001SkillDataKeys.SUPER_STACKS.get());
        if (stacks < COST) {
            return; //riga
        }

        container.getDataManager().setDataSync(t0001SkillDataKeys.SUPER_STACKS.get(), stacks - COST);

        CapabilityItem holdingItem = container.getExecutor().getHoldingItemCapability(InteractionHand.MAIN_HAND);
        AnimationAccessor<? extends StaticAnimation> animation =
                this.motions.getOrDefault(holdingItem.getWeaponCategory(),
                                (i, p) -> Animations.RUSHING_TEMPO3)
                        .apply(holdingItem, container.getExecutor());

        container.getExecutor().playAnimationSynchronized(animation, 0.0F);
    }


    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldDraw(SkillContainer container) {
        return true;
    }

    @Override
    public Set<WeaponCategory> getAvailableWeaponCategories() {
        return this.motions.keySet();}

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(0, (float)gui.getSlidingProgression(), 0);


        guiGraphics.blit(this.getSkillTexture(), (int)x, (int)y, 24, 24, 0, 0, 1, 1, 1, 1);

        // I have stage 5 ligma, I am going to die soon ;C
        int stacks = container.getDataManager().getDataValue(t0001SkillDataKeys.SUPER_STACKS.get());
        guiGraphics.drawString(gui.getFont(), String.valueOf(stacks), x + 18, y + 14, 0xFFFFFF, true);

        poseStack.popPose();
    }
}
