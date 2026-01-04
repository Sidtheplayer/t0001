package sid.t0001.skill;


import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import sid.t0001.main.t0001;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.passive.AdaptiveSkinSkill;
import yesman.epicfight.skill.passive.BonebreakerSkill;

import sid.t0001.skill.identity.FangCounterSkill;
import yesman.epicfight.skill.SkillDataKey;

public class t0001SkillDataKeys {

    public static final DeferredRegister<SkillDataKey<?>> DATA_KEYS = DeferredRegister.create(ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "skill_data_keys"), t0001.MODID);

    public static final DeferredHolder<SkillDataKey<?>, SkillDataKey<Integer>> SUPER_STACKS = DATA_KEYS.register(
            "superstacks",
            () -> SkillDataKey.createSkillDataKey(ByteBufCodecs.INT, 0,false, AdaptiveSkinSkill.class, BonebreakerSkill.class, FangCounterSkill.class)
    );

    public static final DeferredHolder<SkillDataKey<?>, SkillDataKey<Boolean>> IS_AWAKENED = null;

}
