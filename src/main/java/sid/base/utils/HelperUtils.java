package sid.base.utils;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import sid.base.skill.t0001SkillDataKeys;
import sid.base.skill.t0001SkillSlots;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class HelperUtils {

    /// To check if a player is awakened, returns false if the entitypatch isn't a player
    public static boolean is_Awakened(EntityPatch<?> entityPatch) {
        if (entityPatch instanceof PlayerPatch<?> playerPatch && !playerPatch.getSkill(t0001SkillSlots.AWAKENING).isEmpty()) {
            if (playerPatch.getSkill(t0001SkillSlots.AWAKENING).getDataManager().hasData(t0001SkillDataKeys.IS_AWAKENED)) {
                return playerPatch.getSkill(t0001SkillSlots.AWAKENING).getDataManager().getDataValue(t0001SkillDataKeys.IS_AWAKENED);
            }
        }

        return false;
    }


    public static boolean has_skill_orNull(EntityPatch<?> entityPatch, Skill skill, SkillSlot skillSlot){
        if (entityPatch instanceof PlayerPatch<?> playerPatch && !playerPatch.getSkill(skillSlot).isEmpty()) {
          return playerPatch.getSkill(skillSlot).hasSkill(skill);
        }
        return false;
    }


    @ClientOnly
    @OnlyIn(Dist.CLIENT)
    public static boolean is_fullscreen() {
        return Minecraft.getInstance().options.fullscreen().get();
    }





}
