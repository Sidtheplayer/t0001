package sid.base.gameasset;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import sid.base.gameasset.animations.DragonGodSwordAnimations;
import sid.base.gameasset.animations.t0001Animations;
import sid.base.main.t0001;
import sid.base.skill.dodge.AccelerateSkill;
import sid.base.skill.identity.FangCounterSkill;
import sid.base.skill.transition_skills.AnomalousLightningTransitionSkill;
import sid.base.skill.weapon_passives.DgsPassiveSkill;
import sid.base.skill.weaponinnate.PhantomSeverance;
import sid.base.skill.weaponinnate.Times4ChainingInnate;
import sid.base.skill.weaponinnate.t0001InnateOne;
import sid.base.world.item.t0001Tab;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.dodge.DodgeSkill;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackPhaseProperty;
import yesman.epicfight.world.damagesource.ExtraDamageInstance;
import yesman.epicfight.world.damagesource.StunType;


import java.util.Set;


@SuppressWarnings("unused")
public final class t0001Skills {
    private t0001Skills() {}

    public static final DeferredRegister<Skill> REGISTRY =
            DeferredRegister.create(EpicFightRegistries.Keys.SKILL, t0001.MODID);


    public static final DeferredHolder<Skill, DgsPassiveSkill> DGSPASSIVE_SKILL =
            REGISTRY.register("dgs_passive",key->
                    DgsPassiveSkill.createBuilder(DgsPassiveSkill::new)
                            .setCategory(SkillCategories.WEAPON_PASSIVE)
                            .build(key)
            );

    public static final DeferredHolder<Skill, DodgeSkill> ACCELERATE =
            REGISTRY.register("accelerate", key ->
                    AccelerateSkill.createDodgeBuilder(AccelerateSkill::new)
                            .setAnimations(t0001Animations.ACCELERATE, t0001Animations.ACCELERATE_BACK)
                            .setCreativeTab(t0001Tab.T0001_TAB.get())
                            .build(key)
            );


    public static final DeferredHolder<Skill, FangCounterSkill> FANG_COUNTER =
            REGISTRY.register("fangcounter", key ->
                    FangCounterSkill.createFangCounterSkillBuilder()
                            .setCreativeTab(t0001Tab.T0001_TAB.get())
                            .build(key)
            );



    public static final DeferredHolder<Skill, AnomalousLightningTransitionSkill> ANOMALOUS_LIGHTNING_TRANSITION =
            REGISTRY.register("anomalous_lightning_transition", key ->
                    AnomalousLightningTransitionSkill
                            .createAnomalousLightningSkillBuilder()
                            .build(key)
            );


    public static final DeferredHolder<Skill, Times4ChainingInnate> EDGINGSWORDINTENT =
            REGISTRY.register("edging_swordintent", key ->
                            Times4ChainingInnate.createForeFourCutsBuilder()
                                    .newProperty()
                                    .addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(20f))
                                    .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.SHORT)
                                    .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER,ValueModifier.multiplier(1.15f))
                                    .addProperty(AttackPhaseProperty.EXTRA_DAMAGE, Set.of(ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create()))
                                    .newProperty()
                                    .addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(20f))
                                    .addProperty(AttackPhaseProperty.EXTRA_DAMAGE, Set.of(ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create()))
                                    .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.LONG)
                                    .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER,ValueModifier.multiplier(1.15f))
                                    .newProperty()
                                    .addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(20f))
                                    .addProperty(AttackPhaseProperty.EXTRA_DAMAGE, Set.of(ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create()))
                                    .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.SHORT)
                                    .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER,ValueModifier.multiplier(1.15f))
                                    .newProperty()
                                    .addProperty(AttackPhaseProperty.SOURCE_TAG,Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
                                    .addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(20f))
                                    .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER,ValueModifier.multiplier(1.6f))
                                    .addProperty(AttackPhaseProperty.EXTRA_DAMAGE, Set.of(ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create()))
                                    .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.LONG)
                                    .setAnimations(DragonGodSwordAnimations.DGS_UN_IN1,
                                            DragonGodSwordAnimations.DGS_UN_IN2,
                                            DragonGodSwordAnimations.DGS_UN_IN3,
                                            DragonGodSwordAnimations.DGS_UN_IN4, DragonGodSwordAnimations.DGS_IDLE)
                                    .build(key)

                    );

    public static final DeferredHolder<Skill, t0001InnateOne> T0001_INNATE_ONE =
            REGISTRY.register("t0001_innate_one", key ->
                    t0001InnateOne.createT0001InnateBuilder()

                            // Phase 1
                            .newProperty()
                            .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(0))
                            .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(1.0F))
                            .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.adder(6))
                            .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.SHORT)
                            .addProperty(AttackPhaseProperty.SOURCE_TAG,
                                    Set.of(EpicFightDamageTypeTags.GUARD_PUNCTURE))

                            // Phase 2
                            .newProperty()
                            .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(1))
                            .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.adder(1.5F))
                            .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.adder(10))
                            .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.NEUTRALIZE)
                            .addProperty(AttackPhaseProperty.SOURCE_TAG,
                                    Set.of(EpicFightDamageTypeTags.COUNTER))

                            // Phase 3
                            .newProperty()
                            .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(2))
                            .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(6.0F))
                            .addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.setter(20.0F))
                            .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
                            .addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.setter(100))
                            .addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG,
                                    Set.of(EpicFightDamageTypeTags.UNBLOCKALBE))


                            .build(key)
            );

    public static final DeferredHolder<Skill, PhantomSeverance> PHANTOM_SEVERANCE = REGISTRY.register(
            "phantom_severance", key ->
                    PhantomSeverance.createWeaponInnateBuilder(PhantomSeverance::new)
                            .newProperty()
                            .addProperty(AttackPhaseProperty.SOURCE_TAG,Set.of(EpicFightDamageTypeTags.UNBLOCKALBE,
                                    EpicFightDamageTypeTags.BYPASS_DODGE))
                            .build(key)
    );


}
