package sid.base.skill.weaponinnate;

import net.minecraft.nbt.CompoundTag;
import sid.base.gameasset.animations.DragonGodSwordAnimations;
import sid.base.skill.t0001SkillDataKeys;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.player.SkillCastEvent;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.Objects;

public class PhantomSeverance extends WeaponInnateSkill {

    public PhantomSeverance(Builder<?> builder) {
        super(builder);
    }



    @Override
    public boolean resourcePredicate(PlayerPatch<?> executor, SkillCastEvent skillcastevent) {
        boolean activation_key = executor.getSkill(this).getDataManager()
                .getDataValue(t0001SkillDataKeys.ACTIVATION_KEY);
        // If already in run phase, skip stamina cost
        if (activation_key) {
            return super.resourcePredicate(executor, skillcastevent);
        }
        return executor.consumeForSkill(this, Resource.STAMINA, 8.0F);
    }

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);

        eventListener.registerEvent(EpicFightEventHooks.Player.CAST_SKILL,
                (event )->{
                    Skill skill = event.getSkillContainer().getSkill();
                    AnimationPlayer animationPlayer = event.getPlayerPatch().getAnimator().getPlayerFor(null);
                    if(animationPlayer == null) return;
                    // compare the current animation instance to the accessor's animation via .get()
                    var currentAnim = animationPlayer.getAnimation().get();
                    if(skill.getCategory() == SkillCategories.WEAPON_INNATE && currentAnim == DragonGodSwordAnimations.TOO_EASY_RUN.get()){
                        // show the strike instantly on client for responsiveness
                        event.getPlayerPatch().playAnimationInstantly(DragonGodSwordAnimations.TOO_EASY_STRIKE);
                        // don't clear the activation key here (client-side). the server will clear it after the strike executes.
                    }


                },this);

        eventListener.registerEvent(EpicFightEventHooks.Animation.END,event->{

            if(event.getAnimation().equals(DragonGodSwordAnimations.TOO_EASY_STRIKE)){
                container.getDataManager().setDataSync(t0001SkillDataKeys.ACTIVATION_KEY,false);
            }

        },this);



    }

//    @Override
//    public boolean canExecute(SkillContainer container) {
//        if (container.getExecutor().isLogicalClient()) {
//            return super.canExecute(container);
//        }
//        boolean activation_key = container.getDataManager()
//                .getDataValue(t0001SkillDataKeys.ACTIVATION_KEY);
//        ItemStack itemstack = (container.getExecutor().getOriginal()).getMainHandItem();
//        boolean correctWeapon = EpicFightCapabilities.getItemStackCapability(itemstack)
//                .getInnateSkill(container.getExecutor(), itemstack) == this;
//        boolean notRiding = (container.getExecutor().getOriginal()).getVehicle() == null;
//
//        if (activation_key) {
//            return correctWeapon && notRiding;
//        } else {
//            return correctWeapon && notRiding
//                    && ( container.getExecutor().getOriginal()).fallDistance == 0.0F;
//        }
//    }

    @Override
    public void executeOnServer(SkillContainer container, CompoundTag args) {
        super.executeOnServer(container, args);

        var data_manager = container.getDataManager();

        var currentAnimation = Objects.requireNonNull(container.getExecutor().getAnimator()
                .getPlayerFor(null)).getAnimation().get();
        // compare to the actual animation instance
        boolean isRunning = currentAnimation == DragonGodSwordAnimations.TOO_EASY_RUN.get();

        if (!data_manager.getDataValue(t0001SkillDataKeys.ACTIVATION_KEY)) {
            // First press to run
            container.getExecutor().playAnimationSynchronized(DragonGodSwordAnimations.TOO_EASY_RUN, 0.2f);
            data_manager.setDataSync(t0001SkillDataKeys.ACTIVATION_KEY, true);
        } else if (isRunning) {
            container.activate();
            // Second press while running
            container.getExecutor().playAnimationSynchronized(DragonGodSwordAnimations.TOO_EASY_STRIKE, 0.0f);
            // Reset the server animation player
            Objects.requireNonNull(container.getExecutor().getServerAnimator().getPlayerFor(null)).reset();

        }

    }



}
