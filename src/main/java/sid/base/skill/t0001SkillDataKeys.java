package sid.base.skill;


import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import sid.base.main.t0001;
import sid.base.skill.transition_skills.AnomalousLightningTransitionSkill;
import sid.base.skill.weapon_passives.DgsPassiveSkill;
import sid.base.skill.weaponinnate.PhantomSeverance;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.skill.passive.AdaptiveSkinSkill;
import yesman.epicfight.skill.passive.BonebreakerSkill;

import sid.base.skill.identity.FangCounterSkill;
import yesman.epicfight.skill.SkillDataKey;
import yesman.epicfight.skill.weapon_passive.BattojutsuPassive;
import yesman.epicfight.skill.weaponinnate.BattojutsuSkill;

public class t0001SkillDataKeys {
    private t0001SkillDataKeys(){}

    public static final DeferredRegister<SkillDataKey<?>> DATA_KEYS = DeferredRegister.create(EpicFightRegistries.SKILL_DATA_KEY, t0001.MODID);

    public static final DeferredHolder<SkillDataKey<?>, SkillDataKey<Integer>> SUPER_STACKS = DATA_KEYS.register(
            "superstacks",
            () -> SkillDataKey.createSkillDataKey(ByteBufCodecs.INT, 0,false, AdaptiveSkinSkill.class, BonebreakerSkill.class, FangCounterSkill.class)
    );

    //TODO: CHECK IF NOT SYNCING WITH TRACKING PLAYERS CAUSE ERRORS ON SERVER
    public static final DeferredHolder<SkillDataKey<?>, SkillDataKey<Boolean>> IS_AWAKENED = DATA_KEYS.register("awakened",
            ()-> SkillDataKey.createSkillDataKey(ByteBufCodecs.BOOL, false, false, FangCounterSkill.class, PhantomSeverance.class, DgsPassiveSkill.class));



    public static final DeferredHolder<SkillDataKey<?>, SkillDataKey<Integer>> ULTIMATE_MOVE_MODE_SET = DATA_KEYS.register("ultimate_move_mode_set",
            ()-> SkillDataKey.createSkillDataKey(ByteBufCodecs.INT,0,true,FangCounterSkill.class));

    public static final DeferredHolder<SkillDataKey<?>, SkillDataKey<Integer>> PARRY_COUNTER = DATA_KEYS.register("parry_counter",
            ()-> SkillDataKey.createSkillDataKey(ByteBufCodecs.INT, 0 ,true, FangCounterSkill.class));

    public static final DeferredHolder<SkillDataKey<?>, SkillDataKey<Integer>> ULTIMATE_METER = DATA_KEYS.register("ultimate_meter",
            ()-> SkillDataKey.createSkillDataKey(ByteBufCodecs.INT,69,true, AnomalousLightningTransitionSkill.class));

    public static final DeferredHolder<SkillDataKey<?>, SkillDataKey<Boolean>> ACTIVATION_KEY = DATA_KEYS.register("activation_key",
            ()-> SkillDataKey.createSkillDataKey(ByteBufCodecs.BOOL, false, true, AnomalousLightningTransitionSkill.class, PhantomSeverance.class));

    public static final DeferredHolder<SkillDataKey<?>, SkillDataKey<Boolean>> PARRIED_A_PROJECTILE = DATA_KEYS.register("parried_a_projectile",
            ()-> SkillDataKey.createSkillDataKey(ByteBufCodecs.BOOL, false, true,  DgsPassiveSkill.class));



}
