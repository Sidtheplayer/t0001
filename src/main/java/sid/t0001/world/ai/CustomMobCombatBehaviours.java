package sid.t0001.world.ai;

import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

public class CustomMobCombatBehaviours {

    public static final CombatBehaviors.Builder<HumanoidMobPatch<?>> AMOGUS_AXE_INNATE_SPAM = CombatBehaviors.<HumanoidMobPatch<?>>builder()
            .newBehaviorSeries(
                    CombatBehaviors.BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(true).looping(false).cooldown(2)
                            .nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.THE_GUILLOTINE).withinEyeHeight().withinDistance(0.0D, 1.0D))
                            .nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.AXE_AIRSLASH).withinAngle(30,40).withinDistance(0.1D,2.0D))
            ).newBehaviorSeries(
                    CombatBehaviors.BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(20.0F).canBeInterrupted(false).looping(false).cooldown(100)
                            .nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.ENDERMAN_CONVERT_RAGE))

            );
}
