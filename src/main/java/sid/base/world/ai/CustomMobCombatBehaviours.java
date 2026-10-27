package sid.base.world.ai;

import net.shelmarow.combat_evolution.ai.CECombatBehaviors;
import net.shelmarow.combat_evolution.ai.params.AnimationParams;
import sid.base.gameasset.animations.t0001Animations;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

public class CustomMobCombatBehaviours {

    public static final CombatBehaviors.Builder<HumanoidMobPatch<?>> AMOGUS_AXE_INNATE_SPAM = CombatBehaviors.<HumanoidMobPatch<?>>builder()
            .newBehaviorSeries(
                    CombatBehaviors.BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(true).looping(false).cooldown(2)
                            .nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.THE_GUILLOTINE).withinEyeHeight().withinDistance(0.0D, 1.0D))
                            .nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.AXE_AIRSLASH).withinAngle(30,40).withinDistance(0.1D,2.0D))
            ).newBehaviorSeries(
                    CombatBehaviors.BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(20.0F).canBeInterrupted(false).looping(false).cooldown(100)
                            .nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(t0001Animations.DARKNESS_IDLE))

            );


    public static CECombatBehaviors.Builder<MobPatch<?>> createFistCombo() {
        return CECombatBehaviors.builder()

                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(1)
                        .maxCooldown(20)


                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0, 1.2)
                                .animationBehavior(Animations.FIST_AUTO1, new AnimationParams().playSpeed(1.0f))


                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .withinDistance(0, 1.5)
                                        .animationBehavior(Animations.FIST_AUTO2, new AnimationParams().playSpeed(1.2f))

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .withinDistance(0, 2)
                                                .animationBehavior(t0001Animations.UP_KICK_L, new AnimationParams().transitionTime(0.1f).playSpeed(1.5f))

                                                .guard(4).addStamina(4)

                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .withinDistance(0, 1.5)
                                                        .animationBehavior(Animations.FIST_AUTO3, new AnimationParams().playSpeed(1.3f))

                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                .withinDistance(0, 2)
                                                                .animationBehavior(t0001Animations.UP_KICK_R, new AnimationParams().playSpeed(1.420f))

                                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                        .withinDistance(0, 1.4)
                                                                        .animationBehavior(t0001Animations.SWEEP, new AnimationParams().transitionTime(0.05f).playSpeed(1.8f).stunType(StunType.KNOCKDOWN))

                                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                                .withinDistance(0, 1.4)
                                                                                .animationBehavior(t0001Animations.FW_KICK, new AnimationParams()
                                                                                        .playSpeed(1.2f)
                                                                                        .damageMultiplier(1.3F)
                                                                                        .impactMultiplier(1.5F)
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



}
