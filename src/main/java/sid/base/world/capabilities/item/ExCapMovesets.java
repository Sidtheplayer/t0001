package sid.base.world.capabilities.item;

import sid.base.gameasset.animations.DragonGodSwordAnimations;
import sid.base.gameasset.t0001Skills;
import sid.base.main.t0001;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.ex_cap.modules.core.data.MoveSet;
import yesman.epicfight.api.ex_cap.modules.core.data.MoveSetEntry;
import yesman.epicfight.gameasset.Animations;

public class ExCapMovesets
{
    public static final MoveSetEntry DRAGON_GOD_SWORD_NORMAL = new MoveSetEntry(
            t0001.identifier("dgs_n"),
            MoveSet.builder()
                    .addLivingMotionsRecursive(DragonGodSwordAnimations.DGS_IDLE, LivingMotions.IDLE,LivingMotions.FLOAT)
                    .addLivingMotionModifier(LivingMotions.RUN, DragonGodSwordAnimations.DGS_RUN)
                    .addLivingMotionModifier(LivingMotions.BLOCK, DragonGodSwordAnimations.GUARD)
                    .addLivingMotionModifier(LivingMotions.WALK, Animations.BIPED_WALK_LONGSWORD)
                    .setPassiveSkill(t0001Skills.DGSPASSIVE_SKILL.get())
                    .shouldRenderSheath(LivingEntityPatch -> true)
                    .addComboAttacks(
                            Animations.UCHIGATANA_AUTO1,
                            Animations.LONGSWORD_AUTO2,
                            Animations.UCHIGATANA_AUTO3,
                            Animations.LONGSWORD_DASH,
                            Animations.UCHIGATANA_AIR_SLASH
                    )
                    .addInnateSkill(((itemStack, playerPatch) ->
                            t0001Skills.EDGINGSWORDINTENT.get()
                    ))
    );

    public static final MoveSetEntry DRAGON_GOD_SWORD_AWAKENED = new MoveSetEntry(
            t0001.identifier("dgs_s"),
            MoveSet.builder()
                    .parent(t0001.identifier("dgs_n"))
                    .addInnateSkill((i,p)-> t0001Skills.PHANTOM_SEVERANCE.get())
    );
}
