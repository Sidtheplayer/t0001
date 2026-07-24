package sid.base.skill.weaponinnate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import sid.base.gameasset.animations.DragonGodSwordAnimations;
import sid.base.skill.t0001SkillDataKeys;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.player.SkillCastEvent;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class PhantomSeverance extends WeaponInnateSkill {

    public PhantomSeverance(Builder<?> builder) {
        super(builder);
        builder.setResource(Resource.COOLDOWN);
    }

    protected float stamina_cost = 12;

    @Override
    public void loadDatapackParameters(CompoundTag parameters) {
        super.loadDatapackParameters(parameters);
        if(parameters.contains("stamina_cost")){
            stamina_cost = parameters.getFloat("stamina_cost");
        }
    }

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);

        var data_manager = container.getDataManager();

        eventListener.registerEvent(EpicFightEventHooks.Animation.END, (event) -> {
            if (event.getAnimation().equals(DragonGodSwordAnimations.TOO_EASY_RUN)) {
                data_manager.setDataSync(t0001SkillDataKeys.PHANTOM_KEY, false);
            }
            if (event.getAnimation().equals(DragonGodSwordAnimations.TOO_EASY_STRIKE)) {
                data_manager.setDataSync(t0001SkillDataKeys.PHANTOM_KEY, false);
            }
        }, this);
    }


    @Override
    public boolean resourcePredicate(PlayerPatch<?> executor, SkillCastEvent skillcastevent) {
        boolean activation_key = executor.getSkill(this).getDataManager()
                .getDataValue(t0001SkillDataKeys.PHANTOM_KEY);
        // If already in run phase, skip stamina cost
        if (activation_key) {
            return executor.consumeForSkill(this, Resource.STAMINA, 8.0F);

        }
        return super.resourcePredicate(executor, skillcastevent);
    }


    @Override
    public boolean canExecute(SkillContainer container) {
        if (container.getExecutor().isLogicalClient()) {
            return super.canExecute(container);
        }
        boolean activation_key = container.getDataManager()
                .getDataValue(t0001SkillDataKeys.PHANTOM_KEY);
        ItemStack itemstack = (container.getExecutor().getOriginal()).getMainHandItem();
        boolean correctWeapon = EpicFightCapabilities.getItemStackCapability(itemstack)
                .getInnateSkill(container.getExecutor(), itemstack) == this;
        boolean notRiding = (container.getExecutor().getOriginal()).getVehicle() == null;

        if (activation_key) {
            return correctWeapon && notRiding;
        } else {
            return correctWeapon && notRiding
                    && ( container.getExecutor().getOriginal()).fallDistance == 0.0F;
        }
    }

    @Override
    public void executeOnServer(SkillContainer container, CompoundTag args) {
        super.executeOnServer(container, args);

        var data_manager = container.getDataManager();
        boolean activation_key = data_manager.getDataValue(t0001SkillDataKeys.PHANTOM_KEY);


        if (!activation_key) {
            container.getExecutor().playAnimationSynchronized(DragonGodSwordAnimations.TOO_EASY_RUN, 0.2f);
            data_manager.setDataSync(t0001SkillDataKeys.PHANTOM_KEY, true);
        } else {
            container.activate();
            container.getExecutor().consumeForSkill(this,Resource.STAMINA, stamina_cost);
            container.getExecutor().playAnimationSynchronized(DragonGodSwordAnimations.TOO_EASY_STRIKE, 0.0f);
        }

    }


}
