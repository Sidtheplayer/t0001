package sid.t0001.skill.identity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;
import sid.t0001.client.input.t0001KeyMappings;
import sid.t0001.gameasset.animations.UltimateAnimations;
import sid.t0001.gameasset.t0001Animations;
import sid.t0001.gameasset.t0001Skills;
import sid.t0001.network.ParryEffectPacket;
import sid.t0001.network.t0001NetworkManager;
import sid.t0001.skill.t0001SkillDataKeys;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.*;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;
import yesman.epicfight.world.entity.eventlistener.TakeDamageEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;


public class FangCounterSkill extends Skill {
    private static final UUID EVENT_UUID = UUID.fromString("7eaf7af7-2622-40a8-acbc-dee925e3aec3");
    private static int parrycounter = 0;

    public static Builder createFangCounterSkillBuilder() {
        return (new Builder())
                .addMotion(WeaponCategories.FIST, (item, player) -> t0001Animations.FANG_COUNTER)
                .addMotion(WeaponCategories.DAGGER,(item, player) -> t0001Animations.FANG_COUNTER)
                .addMotion(WeaponCategories.UCHIGATANA,(item, player) -> t0001Animations.FANG_COUNTER)
                .setCategory(SkillCategories.IDENTITY)
                .setActivateType(ActivateType.ONE_SHOT) //use oneshot for this type of skill to prvnt issues.
                .setResource(Resource.COOLDOWN);


    }

    @SuppressWarnings("removal")
    private static void accept(TakeDamageEvent.Attack event) {
        if (event.getResult() != AttackResult.ResultType.BLOCKED) return;

        ServerPlayer serverPlayer = event.getPlayerPatch().getOriginal();
        if (serverPlayer == null) return;

        // Calculate position for parry effect
        Vec3 eyePosition = serverPlayer.getEyePosition();
        Vec3 viewVec = serverPlayer.getLookAngle().scale(1.95D);
        double posX = eyePosition.x + viewVec.x;
        double posY = eyePosition.y + viewVec.y - 0.27D;
        double posZ = eyePosition.z + viewVec.z;

        //send the parry effect packet to all clients to spawn the effect.
        ParryEffectPacket packet = new ParryEffectPacket(serverPlayer.getId(), event.isParried(), posX, posY, posZ);
        t0001NetworkManager.INSTANCE.send(PacketDistributor.ALL.noArg(), packet);


        if(event.isParried()){
            parrycounter++;
            if(parrycounter==5){ //WILL add awakening condition or something later on;
                int current_stack = event.getPlayerPatch().getSkill(t0001Skills.FANG_COUNTER).getDataManager().getDataValue(t0001SkillDataKeys.SUPER_STACKS.get());
                int incr = current_stack +2;
                event.getPlayerPatch().getSkill(t0001Skills.FANG_COUNTER).getDataManager().setDataSync(t0001SkillDataKeys.SUPER_STACKS.get(), incr);
                parrycounter=0;
            }
        }



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
    protected int ONEINCHCOUNTERCOST;


    @Override
    public void setParams(CompoundTag parameters) {
        super.setParams(parameters);

        this.KillIncrement.clear();
        this.MAX_SUPER_STACKS = parameters.getInt("max_super_stacks");
        this.COST = parameters.getInt("cost");
        this.ONEINCHCOUNTERCOST = parameters.getInt("one_inch_counter_cost");

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

            boolean awakened = container.getDataManager().getDataValue(t0001SkillDataKeys.IS_AWAKENED.get());


            listener.addEventListener(EventType.TAKE_DAMAGE_EVENT_ATTACK, EVENT_UUID, FangCounterSkill::accept);

            listener.addEventListener(EventType.PLAYER_KILLED_EVENT, EVENT_UUID, (event) -> {
                int cur = container.getDataManager().getDataValue(t0001SkillDataKeys.SUPER_STACKS.get());

                EntityType<?> type = event.getKilledEntity() != null ? event.getKilledEntity().getType() : null;
                int increment = (type != null) ? KillIncrement.getOrDefault(type, 1) : 1;

                int next = cur + increment;
                if (next > MAX_SUPER_STACKS && !awakened) {
                    next = MAX_SUPER_STACKS;
                }
                else if (next > MAX_SUPER_STACKS){
                    next = MAX_SUPER_STACKS + 5;
                } //awakening buff

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
                boolean UltimateKeyPressed = t0001KeyMappings.SUPER_SKILL.isDown();


                if (container.getExecutor().getTarget() != null && stacks >= COST && GuardKeyPressed) {
                    if (container.sendCastRequest((LocalPlayerPatch) container.getExecutor(), // check if player tries to activate skill
                            ClientEngine.getInstance().controlEngine).isExecutable()) {
                        event.setCanceled(true);
                        EpicFightKeyMappings.GUARD.consumeClick(); // to fix the stuck guard after using or trying to use the skill
                    }
                }
                if (container.getExecutor().getTarget() != null && stacks >= ONEINCHCOUNTERCOST && UltimateKeyPressed) {
                    if (container.sendCastRequest((LocalPlayerPatch) container.getExecutor(), // check if player tries to activate skill
                            ClientEngine.getInstance().controlEngine).isExecutable()) {
                        container.getExecutor().consumeForSkill(this, Resource.STAMINA, ONEINCHCOUNTERCOST);
                        container.getExecutor().playAnimationSynchronized(UltimateAnimations.ONE_INCH_COUNTER,0.0125F);
                        LivingEntityPatch<?> targetpatch = EpicFightCapabilities.getEntityPatch(event.getPlayerPatch().getTarget(), LivingEntityPatch.class);
                        PlayerPatch<?> pp = event.getPlayerPatch();
                        pp.getTarget().teleportTo(pp.getOriginal().getX(),pp.getOriginal().getY(),pp.getOriginal().getZ());
                        pp.getTarget().lookAt(EntityAnchorArgument.Anchor.EYES, pp.getOriginal().getEyePosition());
                        assert targetpatch != null;
                        targetpatch.playAnimationSynchronized(UltimateAnimations.ONE_INCH_COUNTER_HIT,0.01F);
                        t0001KeyMappings.SUPER_SKILL.consumeClick(); // to fix the stuck condition after using or trying to use the skill
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


        CapabilityItem holdingItem = container.getExecutor().getHoldingItemCapability(InteractionHand.MAIN_HAND);
        int stacks = container.getDataManager().getDataValue(t0001SkillDataKeys.SUPER_STACKS.get());

        // validate held item and weapon category; check stacks and motion availability else return
        if (holdingItem == null) return;
        var weaponCat = holdingItem.getWeaponCategory();
        if (stacks < COST || weaponCat == null || !this.motions.containsKey(weaponCat)) {
            return;
        }

        container.getDataManager().setDataSync(t0001SkillDataKeys.SUPER_STACKS.get(), stacks - COST);

        AnimationAccessor<? extends StaticAnimation> animation =
                this.motions.get(weaponCat)
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
        guiGraphics.drawString(gui.getFont(), String.valueOf(stacks), x + 18, y + 14, 0xFCFECF, true);

        poseStack.popPose();
    }
}
