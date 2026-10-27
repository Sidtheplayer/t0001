package sid.base.world.ai;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.shelmarow.combat_evolution.ai.CECombatBehaviors;
import net.shelmarow.combat_evolution.ai.StaminaStatus;
import net.shelmarow.combat_evolution.ai.event.BeforeCounterEvent;
import net.shelmarow.combat_evolution.ai.params.AnimationParams;
import net.shelmarow.combat_evolution.ai.util.BehaviorUtils;
import net.shelmarow.combat_evolution.ai.util.CEPatchUtils;
import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.collider.MultiOBBCollider;
import yesman.epicfight.api.ex_cap.data.Moveset;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.guard.GuardSkill;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

import java.util.*;

/// Derives AI movesets from Player Movesets
public class MobAIGenerator {

    public record ComboStep(
            AnimationManager.AnimationAccessor<? extends AttackAnimation> anim,
            double maxDist) {}

    /** Pre-computed guard behavior data and Null if the moveset can't guard */
    public record GuardData(
            AnimationManager.AnimationAccessor<? extends StaticAnimation> counterAnim,
            float counterTransitionTime) {}

    /**
     * Everything that can be derived from a {@link Moveset} once and reused across mobs
     * Cached per-moveset via a {@link WeakHashMap} keyed by identity
     */
    public record Derived(
            List<ComboStep> combo,
            Set<Pair<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>>> living,
            List<AnimationManager.AnimationAccessor<? extends StaticAnimation>> guardHits,
            @Nullable GuardData guard) {}

    //Cache
    private static final Map<Moveset, Derived> CACHE =
            Collections.synchronizedMap(new WeakHashMap<>());

    public static @Nullable Derived derive(Moveset moveset) {
        if (moveset == null) return null;
        return CACHE.computeIfAbsent(moveset, MobAIGenerator::computeDerived);
    }

    public static void clearCache() {
        CACHE.clear();
    }



    private static Derived computeDerived(Moveset moveset) {
        //step with pre-computed reach.
        List<ComboStep> steps = new ArrayList<>();
        var all = moveset.getComboAttackAnimations();
        if (all != null && all.size() >= 3) {
            for (var anim : all.subList(0, all.size() - 2)) {   // skipping dash + airslash (last 2)
                steps.add(new ComboStep(anim, reach(anim)));
            }
        }

        // Living motions and guard hit list.
        Set<Pair<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>>> living =
                Set.copyOf(deriveLivingMotions(moveset));
        List<AnimationManager.AnimationAccessor<? extends StaticAnimation>> guardHits =
                List.copyOf(deriveGuardHitMotions(moveset));

        // Guard behavior prerequisites: counter anim + guard pose + guard hits.
        GuardData guard = deriveGuardData(moveset, all, living, guardHits);

        return new Derived(List.copyOf(steps), living, guardHits, guard);
    }

    @Nullable
    private static GuardData deriveGuardData(
            Moveset moveset,
            List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> all,
            Set<Pair<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>>> living,
            List<AnimationManager.AnimationAccessor<? extends StaticAnimation>> guardHits) {

        // Needs guard-hit reactions, a BLOCK pose (or per-skill guard hold), and a counter anim.
        if (guardHits.isEmpty()) return null;

        boolean hasBlockPose = living.stream().anyMatch(p -> p.getFirst() == LivingMotions.BLOCK);
        if (!hasBlockPose) {
            var poses = moveset.getGuardPoses();
            if (poses == null || poses.isEmpty()) return null;
        }

        AnimationManager.AnimationAccessor<? extends StaticAnimation> counter = moveset.getRevelation();
        if (counter == null && all != null && !all.isEmpty()) counter = all.getFirst();
        if (counter == null) return null;

        return new GuardData(counter, 0.15F);
    }

    // collider math
    private static double reach(AnimationManager.AnimationAccessor<? extends AttackAnimation> anim) {
        double maxDist = 4.0;
        try {
            var phases = anim.get().phases;
            if (phases != null && phases.length > 1 && phases[1] != null) {
                var colliders = phases[1].getColliders();
                if (colliders != null && colliders.length > 1) {
                    var pair = colliders[1];
                    if (pair != null && pair.getSecond() instanceof MultiOBBCollider collider) {
                        maxDist = getColliderRange(collider)[1];
                    }
                }
            }
        } catch (Exception ignored) {}
        return Math.max(maxDist, 0.5);
    }

    //Living derive
    public static Set<Pair<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>>> deriveLivingMotions(Moveset moveset) {
        Set<Pair<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>>> out = new HashSet<>();
        var source = moveset.getLivingMotionModifiers();
        if (source == null || source.isEmpty()) return out;
        source.forEach((motion, anim) -> { if (anim != null) out.add(Pair.of(motion, anim)); });
        if (!source.containsKey(LivingMotions.CHASE) && source.get(LivingMotions.RUN) != null) {
            out.add(Pair.of(LivingMotions.CHASE, source.get(LivingMotions.RUN)));
        }
        return out;
    }
   //Guard derive
    public static List<AnimationManager.AnimationAccessor<? extends StaticAnimation>> deriveGuardHitMotions(Moveset moveset) {
        List<AnimationManager.AnimationAccessor<? extends StaticAnimation>> out = new ArrayList<>();
        String[] order = { "ADVANCED_GUARD", "GUARD" };
        for (String type : order) {
            collectGuardHits(out, moveset.getDefaultGuardAnimations(), type);
            if (!out.isEmpty()) return out;
        }
        var skillSpecific = moveset.getSkillSpecificGuardAnimations();
        if (skillSpecific != null) {
            for (String type : order) {
                skillSpecific.values().forEach(perSkill -> collectGuardHits(out, perSkill, type));
                if (!out.isEmpty()) return out;
            }
        }
        return out;
    }

    private static void collectGuardHits(
            List<AnimationManager.AnimationAccessor<? extends StaticAnimation>> out,
            Map<GuardSkill.BlockType, List<AnimationManager.AnimationAccessor<? extends StaticAnimation>>> byType,
            String typeName) {
        if (byType == null) return;
        byType.forEach((type, list) -> {
            if (list != null && type.name().equals(typeName)) out.addAll(list);
        });
    }

    private static double[] getColliderRange(MultiOBBCollider collider) {
        CompoundTag tag = collider.serialize(null);
        ListTag center = tag.getList("center", Tag.TAG_DOUBLE);
        ListTag size   = tag.getList("size",   Tag.TAG_DOUBLE);
        double cx = center.getDouble(0), cy = center.getDouble(1), cz = center.getDouble(2);
        double sx = size.getDouble(0),   sy = size.getDouble(1),   sz = size.getDouble(2);
        double reach = Math.sqrt(cx*cx + cy*cy + cz*cz) + Math.sqrt(sx*sx + sy*sy + sz*sz);
        return new double[] { 0.0, reach };
    }


    /** derive & build in one call and cache */
    public static CECombatBehaviors.Builder<MobPatch<?>> buildFor(Moveset moveset) {
        Derived d = derive(moveset);
        if (d == null) return CustomMobCombatBehaviours.createFistCombo();
        return buildCombo(d);
    }

    /** Create a fresh builder from cached derived data */
    public static CECombatBehaviors.Builder<MobPatch<?>> buildCombo(Derived d) {
        var steps = d.combo();
        if (steps.isEmpty()) return CustomMobCombatBehaviours.createFistCombo();

        // Building chain in reverse so addNextBehavior connects tail → head.
        CECombatBehaviors.Behavior.Builder<MobPatch<?>> chain = null;
        for (int i = steps.size() - 1; i >= 0; i--) {
            var step = steps.get(i);
            CECombatBehaviors.Behavior.Builder<MobPatch<?>> b =
                    CECombatBehaviors.Behavior.builder()
                            .withinDistance(0.0, step.maxDist())
                            .animationBehavior(step.anim(), new AnimationParams());
            if (chain != null) b.addNextBehavior(chain);
            chain = b;
        }

        CECombatBehaviors.Builder<MobPatch<?>> builder = CECombatBehaviors.builder()
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(1).maxCooldown(10)
                        .addFirstBehavior(chain.addStamina(30)));

        // Guard or parry roots only added if the moveset supports it
        GuardData guard = d.guard();
        if (guard != null) {
            builder.newBehaviorRoot(buildGuardRoot(guard));
            builder.setNoBehaviorHurt((mobPatch, damageSource, attackResult) -> {
                if (CEPatchUtils.getStaminaStatus(mobPatch) == StaminaStatus.COMMON
                        && mobPatch.getEntityState().hurtLevel() > 0) {
                    mobPatch.playAnimationSynchronized(Animations.BIPED_STEP_BACKWARD, 0F);
                    BehaviorUtils.changeCurrentBehavior(mobPatch, "Guard", "Guard", true);
                    return new AttackResult(AttackResult.ResultType.MISSED, 0);
                }
                return attackResult;
            });
        }

        return builder;
    }

    private static CECombatBehaviors.BehaviorRoot.Builder<MobPatch<?>> buildGuardRoot(GuardData guard) {
        return CECombatBehaviors.BehaviorRoot.builder()
                .rootName("Guard")
                .priority(1).weight(0.3)                       // 0.3 weight - attack preferred when idle
                .maxCooldown(100)
                .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                        .name("Guard")
                        .stopByStun(7)
                        .withinDistance(0, 5)
                        .guard(60)                             // 3 s guard window
                        .guardHitCost(1F)
                        .counterAnimation(guard.counterAnim(), guard.counterTransitionTime())
                        .counterType(CECombatBehaviors.CounterType.END)
                        .maxGuardHit(3)                        // parry after 3 blocks
                        .resetGuardTime(true)
                        .setBeforeCounterEvent(new BeforeCounterEvent(mobPatch -> {
                            mobPatch.playSound(EpicFightSounds.BUZZ.get(), 0, 0);
                            return false;
                        })));
    }
}