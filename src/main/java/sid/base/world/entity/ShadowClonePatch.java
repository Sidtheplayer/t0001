package sid.base.world.entity;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.shelmarow.combat_evolution.ai.CECombatBehaviors;
import net.shelmarow.combat_evolution.ai.CEHumanoidPatch;
import net.shelmarow.combat_evolution.ai.iml.CustomExecuteEntity;
import net.shelmarow.combat_evolution.ai.util.BehaviorUtils;
import net.shelmarow.combat_evolution.example.entity.shelmarow.ai.ShelMarowCombatBehaviors;
import net.shelmarow.combat_evolution.execution.ExecutionTypeManager;
import sid.base.gameasset.animations.MiscAnimations;
import sid.base.gameasset.animations.t0001Animations;
import sid.base.world.ai.CustomMobCombatBehaviours;
import sid.base.world.ai.MobAIGenerator;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.ex_cap.data.Moveset;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.registry.entries.EpicFightAttributes;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCapability;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.damagesource.StunType;

import java.util.*;


public class ShadowClonePatch extends CEHumanoidPatch<ShadowCloneEntity> implements CustomExecuteEntity {

    private final Set<WeaponCategory> manualCategories;

    public ShadowClonePatch(ShadowCloneEntity original, Factions factions) {
        super(original, factions);
        // make setWeaponMotions() have higher priority than derived motions
        Set<WeaponCategory> manual = new HashSet<>(this.weaponLivingMotions.keySet());
        manual.addAll(this.guardHitMotions.keySet());
        manual.addAll(this.weaponAttackMotions.keySet());
        this.manualCategories = manual;
    }

    private boolean isManual(WeaponCategory category) {
        return manualCategories != null && manualCategories.contains(category);
    }

    private CapabilityItem lastCap;
    private Style lastStyle;

    private void deriveMotionsForHeldItem() {
        try {
            CapabilityItem cap = this.getHoldingItemCapability(InteractionHand.MAIN_HAND);
            WeaponCategory category = cap.getWeaponCategory();
            if (!(cap instanceof WeaponCapability wc) || isManual(category)) return;

            Style style = cap.getStyle(this);
            if (cap == lastCap && style == lastStyle) return;   // already installed

            Moveset moveset = wc.getMovesetForStyle(style);
            if (moveset == null) return;

            var d = MobAIGenerator.derive(moveset);
            if (d != null && !d.living().isEmpty())
                this.weaponLivingMotions.computeIfAbsent(category, k -> new HashMap<>()).putIfAbsent(style, d.living());
            if (d != null && !d.guardHits().isEmpty())
                this.guardHitMotions.computeIfAbsent(category, k -> new HashMap<>()).putIfAbsent(style, d.guardHits());

            lastCap = cap;
            lastStyle = style;
        } catch (Exception e) {
            System.err.println("[ShadowClone] motion derivation failed: " + e);
        }
    }

    @Override
    public void modifyLivingMotionByCurrentItem(boolean forceChange) {
        deriveMotionsForHeldItem();
        super.modifyLivingMotionByCurrentItem(forceChange);
    }

    @Override
    public AnimationManager.AnimationAccessor<? extends StaticAnimation> getGuardHitAnimation(DamageSource damageSource) {
        deriveMotionsForHeldItem();
        return super.getGuardHitAnimation(damageSource);
    }

    public static InteractionHand getSafeHand(CEHumanoidPatch<?> patch) {
        return patch.getAttackingHand() == null ? InteractionHand.MAIN_HAND : patch.getAttackingHand();
    }

    @Override //THis stupid attack-speed was reason i thought my autogeneration wasn't working, without override the attack anim speed was 0!!! and i had  override to make this max
    public float getAttackSpeed(InteractionHand hand) {
        var stack = this.getValidItemInHand(getSafeHand(this));
        float weaponSpeed = 1.0F;
        //get attack speed reliably
        ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (modifiers != null) {
            for (var entry : modifiers.modifiers()) {
                if (entry.attribute().is(Attributes.ATTACK_SPEED)) {
                    var mod = entry.modifier();
                    if (mod.operation() == AttributeModifier.Operation.ADD_VALUE) {
                        weaponSpeed = 4.0F + (float) mod.amount();
                        break;
                    }
                }
            }
        }

        return Math.max(1.0F, weaponSpeed);
    }

    @Override
    public AnimationManager.AnimationAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
        if (this.getHoldingItemCapability(getSafeHand(this)).isWeaponCategory(CapabilityItem.WeaponCategories.FIST)
                && stunType == StunType.NEUTRALIZE) {
            return MiscAnimations.SPECIAL_GUARD_BREAK;
        } else {
            return super.getHitAnimation(stunType);
        }
    }

    public static void initAttributes(EntityAttributeModificationEvent event) {
        event.add(t0001Entities.SHADOW_CLONE.get(), EpicFightAttributes.MAX_STRIKES, 4.0D);
        event.add(t0001Entities.SHADOW_CLONE.get(), EpicFightAttributes.ARMOR_NEGATION, 12.0D);
        event.add(t0001Entities.SHADOW_CLONE.get(), EpicFightAttributes.IMPACT, 2.0D);
        event.add(t0001Entities.SHADOW_CLONE.get(), EpicFightAttributes.WEIGHT, 10.0D);
        event.add(t0001Entities.SHADOW_CLONE.get(), EpicFightAttributes.MAX_STAMINA, 40.0D);
        event.add(t0001Entities.SHADOW_CLONE.get(), EpicFightAttributes.STAMINA_REGEN, 1.0D);
    }


    @Override
    public void initAnimator(Animator animator) {
        super.initAnimator(animator);
        animator.addLivingAnimation(LivingMotions.IDLE, Animations.BIPED_IDLE);
        animator.addLivingAnimation(LivingMotions.WALK, Animations.BIPED_WALK);
        animator.addLivingAnimation(LivingMotions.RUN, Animations.BIPED_RUN);
        animator.addLivingAnimation(LivingMotions.SNEAK, Animations.BIPED_SNEAK);
        animator.addLivingAnimation(LivingMotions.SWIM, Animations.BIPED_SWIM);
        animator.addLivingAnimation(LivingMotions.FLOAT, Animations.BIPED_FLOAT);
        animator.addLivingAnimation(LivingMotions.KNEEL, Animations.BIPED_KNEEL);
        animator.addLivingAnimation(LivingMotions.FALL, Animations.BIPED_FALL);
        animator.addLivingAnimation(LivingMotions.MOUNT, Animations.BIPED_MOUNT);
        animator.addLivingAnimation(LivingMotions.SIT, Animations.BIPED_SIT);
        animator.addLivingAnimation(LivingMotions.FLY, Animations.BIPED_FLYING);
        animator.addLivingAnimation(LivingMotions.DEATH, Animations.BIPED_DEATH);
        animator.addLivingAnimation(LivingMotions.JUMP, Animations.BIPED_JUMP);
        animator.addLivingAnimation(LivingMotions.CLIMB, Animations.BIPED_CLIMBING);
        animator.addLivingAnimation(LivingMotions.SLEEP, Animations.BIPED_SLEEPING);
        animator.addLivingAnimation(LivingMotions.CREATIVE_FLY, Animations.BIPED_CREATIVE_FLYING);
        animator.addLivingAnimation(LivingMotions.CREATIVE_IDLE, Animations.BIPED_CREATIVE_IDLE);

        animator.addLivingAnimation(LivingMotions.DIGGING, Animations.BIPED_DIG);
        animator.addLivingAnimation(LivingMotions.AIM, Animations.BIPED_BOW_AIM);
        animator.addLivingAnimation(LivingMotions.SHOT, Animations.BIPED_BOW_SHOT);
        animator.addLivingAnimation(LivingMotions.DRINK, Animations.BIPED_DRINK);
        animator.addLivingAnimation(LivingMotions.EAT, Animations.BIPED_EAT);
        animator.addLivingAnimation(LivingMotions.SPECTATE, Animations.BIPED_SPYGLASS_USE);
    }


    @Override
    protected void setWeaponMotions() {
        // ---- LONGSWORD ----
        this.weaponLivingMotions.put(CapabilityItem.WeaponCategories.LONGSWORD,
                ImmutableMap.of(CapabilityItem.Styles.COMMON, Set.of(
                        Pair.of(LivingMotions.BLOCK, Animations.LONGSWORD_GUARD),
                        Pair.of(LivingMotions.IDLE, Animations.BIPED_HOLD_SPEAR),
                        Pair.of(LivingMotions.WALK, Animations.BIPED_WALK_SPEAR),
                        Pair.of(LivingMotions.RUN, Animations.BIPED_RUN_SPEAR),
                        Pair.of(LivingMotions.CHASE, Animations.BIPED_RUN_SPEAR),
                        Pair.of(LivingMotions.DEATH, Animations.BIPED_DEATH)
                ))
        );

        this.guardHitMotions.put(CapabilityItem.WeaponCategories.LONGSWORD,
                ImmutableMap.of(CapabilityItem.Styles.COMMON, List.of(
                        Animations.LONGSWORD_GUARD_ACTIVE_HIT1,
                        Animations.LONGSWORD_GUARD_ACTIVE_HIT2,
                        Animations.SWORD_GUARD_ACTIVE_HIT1,
                        Animations.SWORD_GUARD_ACTIVE_HIT2,
                        Animations.SWORD_GUARD_ACTIVE_HIT3
                ))
        );

        this.weaponAttackMotions.put(
                CapabilityItem.WeaponCategories.LONGSWORD,
                ImmutableMap.of(CapabilityItem.Styles.COMMON, ShelMarowCombatBehaviors.creatCommon())
        );

        // ---- FIST ----
        this.weaponAttackMotions.put(
                CapabilityItem.WeaponCategories.FIST,
                Map.of(CapabilityItem.Styles.COMMON, CustomMobCombatBehaviours.createFistCombo())
        );

        this.weaponLivingMotions.put(CapabilityItem.WeaponCategories.FIST,
                ImmutableMap.of(CapabilityItem.Styles.COMMON, Set.of(
                        Pair.of(LivingMotions.BLOCK, t0001Animations.UNARMEDBLOCKFULL),
                        Pair.of(LivingMotions.IDLE, Animations.BIPED_IDLE),
                        Pair.of(LivingMotions.WALK, Animations.BIPED_WALK),
                        Pair.of(LivingMotions.RUN, Animations.BIPED_RUN),
                        Pair.of(LivingMotions.CHASE, Animations.BIPED_RUN_SPEAR),
                        Pair.of(LivingMotions.DEATH, Animations.BIPED_COMMON_NEUTRALIZED)
                ))
        );

        this.guardHitMotions.put(CapabilityItem.WeaponCategories.FIST, ImmutableMap.of(
                CapabilityItem.Styles.COMMON, List.of(
                        t0001Animations.UNARMEDBLOCKFULL_HIT
                )
        ));
    }




    @SuppressWarnings("CallToPrintStackTrace")
    @Override
    protected CECombatBehaviors.Builder<MobPatch<?>> getCustomWeaponMotionBuilder() {
        try {
            CapabilityItem cap = this.getHoldingItemCapability(InteractionHand.MAIN_HAND);
            WeaponCategory category = cap.getWeaponCategory();

            if (cap instanceof WeaponCapability wc
                    && category != CapabilityItem.WeaponCategories.FIST
                    && category != CapabilityItem.WeaponCategories.LONGSWORD) {
                Style style = cap.getStyle(this);
                Moveset moveset = wc.getMovesetForStyle(style);
                if (moveset != null && !moveset.getComboAttackAnimations().isEmpty()) {
                    return MobAIGenerator.buildFor(moveset);
                }
            }
        } catch (Exception e) {
            System.err.println("[ShadowClone] getCustomWeaponMotionBuilder failed: " + e);
            e.printStackTrace();
        }
        return super.getCustomWeaponMotionBuilder();
    }

    @Override
    public void postTick() {
        super.postTick();
        if (!this.isLogicalClient() && this.original.tickCount % 20 == 0 && EpicFightSharedConstants.IS_DEV_ENV) {
            LivingEntity target = this.original.getTarget();
            CECombatBehaviors<?> behaviors = BehaviorUtils.getCECombatBehaviors(this);
            CECombatBehaviors.Behavior<?> current = behaviors == null ? null : behaviors.getCurrentBehavior();
            var player = this.getAnimator().getPlayerFor(null);
            String anim = "none";
            if (player != null && player.getAnimation() != null) {
                anim = player.getAnimation().get() + " t=" + String.format("%.2f", player.getElapsedTime())
                        + " end=" + player.isEnd();
            }
            System.out.println("[ShadowClone#" + this.original.getId() + "] item="
                    + this.original.getMainHandItem().getItem()
                    + " inaction=" + this.state.inaction()
                    + " realInaction=" + this.getAnimator().getEntityState().inaction()
                    + " anim=" + anim
                    + " atkAttr=" + this.original.getAttributeValue(Attributes.ATTACK_SPEED)
                    + " efAtkSpeed=" + this.getAttackSpeed(InteractionHand.MAIN_HAND)
                    + " behavior=[" + (current == null ? "null" : current.getBehaviorName()) + "]"
                    + " target=" + (target == null ? "null" : target.getName().getString()));
        }


    }


    @Override
    public boolean canBeExecuted(LivingEntityPatch<?> executorPatch) {
        return true;
    }

    @Override
    public boolean canUseCustomType(LivingEntityPatch<?> executorPatch, ExecutionTypeManager.Type originalType) {
        return true;
    }

    @Override
    public ExecutionTypeManager.Type getExecutionType(LivingEntityPatch<?> executorPatch, ExecutionTypeManager.Type originalType) {
        return originalType;
    }
}