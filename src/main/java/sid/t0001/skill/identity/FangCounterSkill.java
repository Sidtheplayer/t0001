package sid.t0001.skill.identity;

import com.google.common.collect.Maps;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import sid.t0001.gameasset.t0001Animations;
import sid.t0001.main.t0001;
import sid.t0001.network.ParryEffectPacket;
import sid.t0001.skill.t0001SkillDataKeys;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.neoevent.playerpatch.SkillCastEvent;
import yesman.epicfight.api.neoevent.playerpatch.TakeDamageEvent;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.*;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;


@SuppressWarnings("SpellCheckingInspection")
public class FangCounterSkill extends Skill {
    private static final UUID EVENT_UUID =
            UUID.fromString("7eaf7af7-2622-40a8-acbc-dee925e3aec3");


    public static Builder createFangCounterSkillBuilder(
            Function<Builder, ? extends Skill> constructor
    ) {
        return new Builder(FangCounterSkill::new)
                .addMotion(WeaponCategories.FIST, (item, player) -> t0001Animations.FANG_COUNTER)
                .addMotion(WeaponCategories.DAGGER, (item, player) -> t0001Animations.FANG_COUNTER)
                .addMotion(WeaponCategories.UCHIGATANA, (item, player) -> t0001Animations.FANG_COUNTER)
                .setCategory(SkillCategories.IDENTITY)
                .setActivateType(ActivateType.ONE_SHOT)
                .setResource(Resource.COOLDOWN);
    }

    public static class Builder extends SkillBuilder<FangCounterSkill.Builder> {
        protected final Map<WeaponCategory,
                BiFunction<CapabilityItem, PlayerPatch<?>,
                        AnimationAccessor<? extends StaticAnimation>>> motions = Maps.newHashMap();

        public Builder(Function<Builder, ? extends Skill> constructor) {
            super(constructor);
        }

        public Builder addMotion(
                WeaponCategory category,
                BiFunction<CapabilityItem, PlayerPatch<?>,
                        AnimationAccessor<? extends StaticAnimation>> motion
        ) {
            this.motions.put(category, motion);
            return this;
        }
    }

    protected final Map<WeaponCategory,
            BiFunction<CapabilityItem, PlayerPatch<?>,
                    AnimationAccessor<? extends StaticAnimation>>> motions;

    protected final Map<EntityType<?>, Integer> killIncrement = Maps.newHashMap();
    protected int COST;
    protected int MAX_SUPER_STACKS;

    public FangCounterSkill(Builder builder) {
        super(builder);
        this.motions = builder.motions;
    }


    @Override
    public void loadDatapackParameters(CompoundTag parameters) {
        super.loadDatapackParameters(parameters);

        killIncrement.clear();
        MAX_SUPER_STACKS = parameters.getInt("max_super_stacks");
        COST = parameters.getInt("cost");

        CompoundTag increments = parameters.getCompound("KillIncrement");
        for (String key : increments.getAllKeys()) {
            EntityType.byString(key).ifPresentOrElse(
                    type -> killIncrement.put(type, increments.getInt(key)),
                    () -> EpicFightMod.LOGGER.warn(
                            "FangCounterSkill: unknown entity type {}", key
                    )
            );
        }
    }

    /* ---------------- EVENTS ---------------- */

    @SkillEvent(
            caller = t0001.MODID,
            side = SkillEvent.Side.SERVER,
            priority = -1
    )
    public void onTakeDamage(TakeDamageEvent.Income event, SkillContainer container) {
        if (event.getResult() != AttackResult.ResultType.BLOCKED) return;

        ServerPlayerPatch patch = event.getPlayerPatch();
        ServerPlayer player = patch.getOriginal();
        if (player == null) return;

        Vec3 eye = player.getEyePosition();
        Vec3 view = player.getLookAngle().scale(1.95D);

        ParryEffectPacket packet = new ParryEffectPacket(
                player.getId(),
                event.isParried(),
                eye.x + view.x,
                eye.y + view.y - 0.27D,
                eye.z + view.z
        );

        PacketDistributor.sendToPlayersTrackingEntity(player, packet);
    }

    @SkillEvent(
            caller = t0001.MODID,
            side = SkillEvent.Side.SERVER
    )
    public void onKill(
            yesman.epicfight.api.neoevent.playerpatch.PlayerKilledEvent event,
            SkillContainer container
    ) {
        int current = container.getDataManager()
                .getDataValue(t0001SkillDataKeys.SUPER_STACKS);

        EntityType<?> type = event.getKilledEntity() != null
                ? event.getKilledEntity().getType()
                : null;

        int inc = type != null
                ? killIncrement.getOrDefault(type, 1)
                : 1;

        int next = Math.min(current + inc, MAX_SUPER_STACKS);

        container.getDataManager()
                .setDataSync(t0001SkillDataKeys.SUPER_STACKS, next);
    }

    @SkillEvent(
            caller = t0001.MODID,
            side = SkillEvent.Side.CLIENT
    )
    public void onSkillCast(SkillCastEvent event, SkillContainer container) {
        if (!container.getExecutor().isLogicalClient()) return;
        if (event.getSkillContainer() != container) return;

        // must be activated
        if (!container.isActivated()) return;

        int stacks = container.getDataManager()
                .getDataValue(t0001SkillDataKeys.SUPER_STACKS);

        if (stacks < COST) return;
        if (!EpicFightKeyMappings.GUARD.isDown()) return;
        if (container.getExecutor().getTarget() == null) return;

        if (container.sendCastRequest(
                container.getClientExecutor(),
                ControlEngine.getInstance()
        ).isExecutable()) {

            container.setDuration(0);
            event.setCanceled(true);
            EpicFightKeyMappings.GUARD.consumeClick();
        }
    }



    @Override
    public void executeOnServer(SkillContainer container, CompoundTag arguements) {
        super.executeOnServer(container,arguements);

        CapabilityItem item =
                container.getExecutor().getHoldingItemCapability(InteractionHand.MAIN_HAND);

        if (item == null) return;

        int stacks = container.getDataManager()
                .getDataValue(t0001SkillDataKeys.SUPER_STACKS);

        WeaponCategory category = item.getWeaponCategory();
        if (category == null || stacks < COST || !motions.containsKey(category)) return;

        container.getDataManager()
                .setDataSync(t0001SkillDataKeys.SUPER_STACKS, stacks - COST);

        container.getExecutor().playAnimationSynchronized(
                motions.get(category).apply(item, container.getExecutor()),
                0.0F
        );
    }

    @Override
    public Set<WeaponCategory> getAvailableWeaponCategories() {
        return motions.keySet();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
        guiGraphics.blit(this.getSkillTexture(), (int) x, (int) y, 24, 24, 0, 0, 1, 1, 1, 1);

        if (container.getRemainDuration() > 0) {
            return;
        }

        int stacks = container.getDataManager().getDataValue(t0001SkillDataKeys.SUPER_STACKS);

        boolean active = container.isActivated();
        boolean enoughStacks = stacks >= COST;

        int color = (active && enoughStacks) ? 0xFCFECF : 0x777777;

        guiGraphics.drawString(gui.getFont(), String.valueOf(stacks), x + 18, y + 14, color, true);
    }

}
