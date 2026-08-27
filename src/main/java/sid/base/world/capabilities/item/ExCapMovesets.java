package sid.base.world.capabilities.item;

import sid.base.gameasset.animations.DragonGodSwordAnimations;
import sid.base.gameasset.animations.t0001Animations;
import sid.base.skill.t0001Skills;
import sid.base.main.t0001;
import yesman.epicfight.api.animation.LivingMotions;

import yesman.epicfight.api.ex_cap.data.Moveset;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.registry.deferred.MovesetRegister;
import yesman.epicfight.registry.deferred.holders.DeferredMoveset;
import yesman.epicfight.registry.entries.EpicFightMovesets;
import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.skill.guard.GuardSkill;

public class ExCapMovesets {

    private ExCapMovesets(){}

    public static final MovesetRegister REGISTRY = MovesetRegister.create(t0001.MODID);

    public static void modifyMoveset(){



        EpicFightMovesets.GLOVE.get()
                .addLivingMotionModifier(LivingMotions.BLOCK, t0001Animations.UNARMEDBLOCKFULL)
                .addGuardAnimations(GuardSkill.BlockType.GUARD, t0001Animations.UNARMEDBLOCKFULL_HIT)
                .addGuardAnimations(GuardSkill.BlockType.ADVANCED_GUARD,Animations.SWORD_GUARD_ACTIVE_HIT1,Animations.SWORD_GUARD_ACTIVE_HIT2)
                .addGuardAnimations(GuardSkill.BlockType.GUARD_BREAK, Animations.BIPED_COMMON_NEUTRALIZED);
    }



    public static final DeferredMoveset DRAGON_GOD_SWORD_NORMAL = REGISTRY.registerMoveset(
            "dgs_n",() ->
            Moveset.builder()
                    .addLivingMotionsRecursive(DragonGodSwordAnimations.DGS_IDLE, LivingMotions.IDLE,LivingMotions.FLOAT)
                    .addLivingMotionModifier(LivingMotions.RUN, DragonGodSwordAnimations.DGS_RUN)
                    .addLivingMotionModifier(LivingMotions.BLOCK, DragonGodSwordAnimations.GUARD)
                    .addLivingMotionModifier(LivingMotions.WALK, Animations.BIPED_WALK_LONGSWORD)
                    .setPassiveSkill(t0001Skills.DGSPASSIVE_SKILL)
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
                    .shouldRenderSheath(living -> true)

    );

    public static final DeferredMoveset TACHI_2H = REGISTRY.registerMoveset("tachi_2h_sheath",
            () -> Moveset.builder()
                    .addLivingMotionsRecursive(Animations.BIPED_HOLD_TACHI,
                            LivingMotions.IDLE, LivingMotions.KNEEL, LivingMotions.WALK, LivingMotions.CHASE, LivingMotions.RUN,
                            LivingMotions.SNEAK, LivingMotions.SWIM, LivingMotions.FLOAT, LivingMotions.FALL)
                    .addLivingMotionModifier(LivingMotions.BLOCK, Animations.LONGSWORD_GUARD)
                    .addComboAttacks(
                            Animations.TACHI_AUTO1, Animations.TACHI_AUTO2, Animations.TACHI_AUTO3,
                            Animations.TACHI_DASH, Animations.LONGSWORD_AIR_SLASH
                    )
                    .addMountAttacks(Animations.SWORD_MOUNT_ATTACK)
                    .addInnateSkill((itemStack, playerPatch) -> EpicFightSkills.RUSHING_TEMPO.get())
                    .shouldRenderSheath(living -> true)

    );

    public static final DeferredMoveset DRAGON_GOD_SWORD_AWAKENED = REGISTRY.registerMoveset(
            "dgs_s", () ->
            Moveset.builder()
                    .parent(DRAGON_GOD_SWORD_NORMAL)
                    .addInnateSkill((i,p)-> t0001Skills.PHANTOM_SEVERANCE.get())
                    .shouldRenderSheath(living -> true)

    );

    public static final DeferredMoveset amatuerKicker = REGISTRY.registerMoveset(
            "amatuer_kicker_d",()->
            Moveset.builder()
                    .parent(EpicFightMovesets.GLOVE)
                    .addComboAttacks(
                            Animations.FIST_AUTO1,
                            Animations.FIST_AUTO2,
                            t0001Animations.UP_KICK_L,
                            Animations.FIST_AUTO3,
                            t0001Animations.UP_KICK_R,
                            t0001Animations.SWEEP,
                            t0001Animations.FW_KICK,
                            Animations.FIST_AIR_SLASH
                    )
                    .addGuardAnimations(GuardSkill.BlockType.GUARD,t0001Animations.UNARMEDBLOCKFULL,t0001Animations.UNARMEDBLOCKFULL_HIT)
    );

}
