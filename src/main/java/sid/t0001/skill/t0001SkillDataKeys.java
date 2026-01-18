package sid.t0001.skill;


import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.xame.t0001;
import yesman.epicfight.api.utils.PacketBufferCodec;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.passive.AdaptiveSkinSkill;
import yesman.epicfight.skill.passive.BonebreakerSkill;

import sid.t0001.skill.identity.FangCounterSkill;
import yesman.epicfight.skill.SkillDataKey;

public class t0001SkillDataKeys {

    public static final DeferredRegister<SkillDataKey<?>> DATA_KEYS = DeferredRegister.create(ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "skill_data_keys"), t0001.MODID);

    public static final RegistryObject<SkillDataKey<Integer>> SUPER_STACKS = DATA_KEYS.register(
            "superstacks",
            () -> SkillDataKey.createSkillDataKey(PacketBufferCodec.INTEGER, 0, AdaptiveSkinSkill.class, BonebreakerSkill.class, FangCounterSkill.class)
    );

    public static final RegistryObject<SkillDataKey<Boolean>> IS_AWAKENED = DATA_KEYS.register("is_awakened",()-> SkillDataKey.createSkillDataKey(PacketBufferCodec.BOOLEAN, false, FangCounterSkill.class));

}
