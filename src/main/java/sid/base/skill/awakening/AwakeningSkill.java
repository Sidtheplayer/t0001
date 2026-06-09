package sid.base.skill.awakening;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import sid.base.events.event_hook.AwakenBeginEvent;
import sid.base.events.event_hook.AwakenEndEvent;
import sid.base.events.event_hook.MyEventHooks;
import sid.base.skill.t0001SkillCategories;
import sid.base.skill.t0001SkillDataKeys;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

import java.util.function.Function;

public abstract class AwakeningSkill extends Skill {


    public AwakeningSkill(SkillBuilder<?> builder) {
        super(builder);
    }

    public float reduction_coefficient() {
            return 0.1f;
    }

    public static SkillBuilder<?> createAwakeningSkillBuilder(Function<SkillBuilder<?>, ? extends AwakeningSkill> constructor) {
        return new SkillBuilder<>(constructor)
                .setCategory(t0001SkillCategories.AWAKENING)
                .setActivateType(ActivateType.TOGGLE)
                .setResource(Resource.NONE);
    }

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);

        var data_manager = container.getDataManager();

        //Slowly Deplete bar
        eventListener.registerContextAwareEvent(EpicFightEventHooks.Player.TICK_EPICFIGHT_MODE, (event, context) -> {
            boolean has_data = data_manager.hasData(t0001SkillDataKeys.IS_AWAKENED) && data_manager.hasData(t0001SkillDataKeys.ULTIMATE_METER);
            if(has_data && data_manager.getDataValue(t0001SkillDataKeys.IS_AWAKENED) && !container.getExecutor().getOriginal().isCreative() ){
                if(event.getPlayerPatch().getOriginal().tickCount % 20 == 0){

                    float meter_value = data_manager.getDataValue(t0001SkillDataKeys.ULTIMATE_METER);
                    float reduction = Math.max(meter_value - reduction_coefficient(), 0.0f);
                    data_manager.setDataSync(t0001SkillDataKeys.ULTIMATE_METER, reduction);

                    if(data_manager.getDataValue(t0001SkillDataKeys.ULTIMATE_METER) <= 0.0){
                        data_manager.setDataSync(t0001SkillDataKeys.IS_AWAKENED,false);

                        AwakenEndEvent evt = new AwakenEndEvent(event.getPlayerPatch());
                        MyEventHooks.Awakening.END.postWithListener(evt, eventListener);

                        event.getPlayerPatch().playLocalSound(EpicFightSounds.ADRENALINE);
                    }

                }
            }

        },this, 100);
    }


    @Override
    public boolean canExecute(SkillContainer container) {
        ItemStack weapon = container.getExecutor().getOriginal().getMainHandItem();
        WeaponCategory weaponCategory = EpicFightCapabilities.getItemStackCapability(weapon).getWeaponCategory();
        if (container.getExecutor().isLogicalClient())
        {
            return super.canExecute(container);

        } else {
            return super.canExecute(container)
                    && container.getExecutor().getOriginal().getVehicle() == null && (!container.getExecutor().getSkill(this).isActivated() || this.activateType == ActivateType.TOGGLE);
        }
    }

    @Override
    public void executeOnServer(SkillContainer container, CompoundTag args) {
        super.executeOnServer(container, args);
        AwakenBeginEvent event = new AwakenBeginEvent(container.getExecutor().getEventListener().getEntityPatch());
        MyEventHooks.Awakening.BEGIN.post(event);
    }





}
