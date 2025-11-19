package sid.t0001.gameasset;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.RegisterEvent;
import org.xame.t0001;
import sid.t0001.skill.dodge.AccelerateSkill;
import sid.t0001.skill.identity.FangCounterSkill;
import sid.t0001.skill.transition_skills.AnomalousLightningTransitionSkill;
import sid.t0001.skill.weapon_passives.FreeKatanaPassive;
import yesman.epicfight.api.forgeevent.SkillBuildEvent;
import yesman.epicfight.api.forgeevent.SkillBuildEvent.ModRegistryWorker;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.dodge.DodgeSkill;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;
import sid.t0001.skill.weaponinnate.t0001InnateOne;


import yesman.epicfight.api.animation.property.AnimationProperty.AttackPhaseProperty;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;
import java.util.Set;

@Mod.EventBusSubscriber(modid = t0001.MODID, bus = EventBusSubscriber.Bus.MOD)
public class t0001Skills {

    public static Skill ACCELERATE;
    public static Skill FANG_COUNTER;
    public static Skill T0001INNATEONE;
    public static Skill ANOMALOUSLIGHTNINGTRANSITION;

    public static Skill FREEKATANAPASSIVE;

    @SubscribeEvent
    public static void buildSkillEvent(SkillBuildEvent build) {
        ModRegistryWorker modRegistry = build.createRegistryWorker("t0001");

        FREEKATANAPASSIVE = modRegistry.build("free_katana_passive", FreeKatanaPassive::new, Skill.createBuilder().setCategory(SkillCategories.WEAPON_PASSIVE));

        ACCELERATE = modRegistry.build("accelerate", AccelerateSkill::new, DodgeSkill.createDodgeBuilder().setAnimations(t0001Animations.ACCELERATE, t0001Animations.ACCELERATE_BACK));

        FANG_COUNTER = modRegistry.build("fangcounter", FangCounterSkill::new, FangCounterSkill.createFangCounterSkillBuilder());
        ANOMALOUSLIGHTNINGTRANSITION = modRegistry.build("anomalous_lightning_transition", AnomalousLightningTransitionSkill::new, AnomalousLightningTransitionSkill.createAnomalousLightningTransitionBuilder());


        WeaponInnateSkill t0001Inn = modRegistry.build("t0001_innate_one", t0001InnateOne::new, WeaponInnateSkill.createWeaponInnateBuilder());


        t0001Inn
                .newProperty()
                .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(2))
                .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(1.0F))
                .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.adder(6))
                .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.SHORT)
                .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.GUARD_PUNCTURE));

        t0001Inn
                .newProperty()
                .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(1))
                .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.adder(1.5F))
                .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.adder(10))
                .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.NEUTRALIZE)
                .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.COUNTER));


        t0001Inn
                .newProperty()
                .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(2))
                .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(6.0F))
                .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.setter(20.0F))
                .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
                .addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.setter(100))
                .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE, EpicFightDamageTypeTags.UNBLOCKALBE));

        T0001INNATEONE = t0001Inn;

    }

    public t0001Skills() {
    }

    public static void registert0001Skills(RegisterEvent bus) {
    }
}