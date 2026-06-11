package sid.base.utils;

import sid.base.skill.t0001SkillDataKeys;
import sid.base.skill.t0001SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class HelperUtils {

    /// To check if a player is awakened, returns false if the entitypatch isn't a player
 public static boolean is_Awakened(EntityPatch<?> entityPatch){
     if(entityPatch instanceof PlayerPatch<?> playerPatch && !playerPatch.getSkill(t0001SkillSlots.AWAKENING).isEmpty()){
         if (playerPatch.getSkill(t0001SkillSlots.AWAKENING).getDataManager().hasData(t0001SkillDataKeys.IS_AWAKENED)){
             return playerPatch.getSkill(t0001SkillSlots.AWAKENING).getDataManager().getDataValue(t0001SkillDataKeys.IS_AWAKENED);
         }
     }
     return false;
 }


}
