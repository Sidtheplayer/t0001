package sid.t0001.skill;


import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import sid.t0001.main.t0001;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.skill.passive.AdaptiveSkinSkill;
import yesman.epicfight.skill.passive.BonebreakerSkill;

import sid.t0001.skill.identity.FangCounterSkill;
import yesman.epicfight.skill.SkillDataKey;

public class t0001SkillDataKeys {
    private t0001SkillDataKeys(){}

    public static final DeferredRegister<SkillDataKey<?>> DATA_KEYS = DeferredRegister.create(EpicFightRegistries.SKILL_DATA_KEY, t0001.MODID);

    public static final DeferredHolder<SkillDataKey<?>, SkillDataKey<Integer>> SUPER_STACKS = DATA_KEYS.register(
            "superstacks",
            () -> SkillDataKey.createSkillDataKey(ByteBufCodecs.INT, 0,false, AdaptiveSkinSkill.class, BonebreakerSkill.class, FangCounterSkill.class)
    );

    //TODO: CHECK IF NOT SYNCING WITH TRACKING PLAYERS CAUSE ERRORS ON SERVER
    public static final DeferredHolder<SkillDataKey<?>, SkillDataKey<Boolean>> IS_AWAKENED = DATA_KEYS.register("awakened",
            ()-> SkillDataKey.createSkillDataKey(ByteBufCodecs.BOOL, false, false, FangCounterSkill.class));


    public static final DeferredHolder<SkillDataKey<?>, SkillDataKey<Integer>> ULTIMATE_MOVE_MODE_SET = DATA_KEYS.register("ultimate_move_mode_set",
            ()-> SkillDataKey.createSkillDataKey(ByteBufCodecs.INT,0,true,FangCounterSkill.class)
            );

    public static final DeferredHolder<SkillDataKey<?>, SkillDataKey<Integer>> PARRY_COUNTER = DATA_KEYS.register("parry_counter",
            ()-> SkillDataKey.createSkillDataKey(ByteBufCodecs.INT, 0 ,true, FangCounterSkill.class));

}
