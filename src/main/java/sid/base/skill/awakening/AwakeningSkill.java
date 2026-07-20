package sid.base.skill.awakening;

import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import sid.base.events.event_hook.AwakenBeginEvent;
import sid.base.events.event_hook.AwakenEndEvent;
import sid.base.events.event_hook.AwakenTickEvent;
import sid.base.events.event_hook.MyEventHooks;
import sid.base.main.t0001;
import sid.base.skill.t0001SkillCategories;
import sid.base.skill.t0001SkillDataKeys;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;

import java.util.Map;
import java.util.function.Function;

public abstract class AwakeningSkill extends Skill {


    public AwakeningSkill(SkillBuilder<?> builder) {
        super(builder);
    }

    public static float default_reduction_coefficient() {
            return 0.50f;
    }

    public static float default_damage_increase_coefficient(){
        return 0.25f;
    }

    public static float default_dmg_increase_upper_limit(){
        return 20.0f;
    }

    public static float Default_Meter_Capacity(){
      return 100.0f;
    }

    public static float default_kill_increment(){
        return 5.0f;
    }

    protected static float meter_capacity = Default_Meter_Capacity();
    protected static float dmg_upper_limit = default_dmg_increase_upper_limit();
    protected static float dmg_increase_coefficient = default_damage_increase_coefficient();
    protected static float reduction_coefficient = default_reduction_coefficient();
    protected static float default_kill_increment = default_kill_increment();

    protected final Map<EntityType<?>, Float> killIncrement = Maps.newHashMap();

    @Override
    public void loadDatapackParameters(CompoundTag parameters) {
        super.loadDatapackParameters(parameters);

        dmg_upper_limit = parameters.contains("dmg_upper_limit")
                ? parameters.getFloat("dmg_upper_limit")
                : default_dmg_increase_upper_limit();

        dmg_increase_coefficient = parameters.contains("dmg_increase_coefficient")
                ? parameters.getFloat("dmg_increase_coefficient")
                : default_damage_increase_coefficient();

        meter_capacity = parameters.contains("meter_capacity")
                ? parameters.getFloat("meter_capacity")
                : Default_Meter_Capacity();

        reduction_coefficient = parameters.contains("reduction_coefficient")
                ? parameters.getFloat("reduction_coefficient")
                : default_reduction_coefficient();

        default_kill_increment = parameters.contains("default_kill_increment")
                ? parameters.getFloat("default_kill_increment")
                : default_kill_increment();

        killIncrement.clear();
        CompoundTag increments = parameters.getCompound("kill_increment");
        for (String key : increments.getAllKeys()) {
            EntityType.byString(key).ifPresentOrElse(
                    type -> killIncrement.put(type, increments.getFloat(key)),
                    () -> t0001.LOGGER.warn("{}: unknown entity type {}", this.getDisplayName().getString(), key)
            );
        }
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


        eventListener.registerContextAwareEvent(EpicFightEventHooks.Entity.DELIVER_DAMAGE_INCOME, (event, context) -> {
            var dm = container.getDataManager();

            boolean is_awakened = dm.hasData(t0001SkillDataKeys.IS_AWAKENED) && dm.getDataValue(t0001SkillDataKeys.IS_AWAKENED);

            if (is_awakened || event.getOriginalDamage() < 2.5) return;

            float current = dm.getDataValue(t0001SkillDataKeys.ULTIMATE_METER);

            float increase = Math.clamp(event.getOriginalDamage() * dmg_increase_coefficient, 0.0f, dmg_upper_limit);

            dm.setDataSync(t0001SkillDataKeys.ULTIMATE_METER,
                    Math.clamp(current + increase, 0f, meter_capacity)
            );

        }, this, 3);

        eventListener.registerEvent(EpicFightEventHooks.Entity.KILL_ENTITY, (event) -> {
            var dm = container.getDataManager();

            boolean is_awakened = dm.hasData(t0001SkillDataKeys.IS_AWAKENED) && dm.getDataValue(t0001SkillDataKeys.IS_AWAKENED);

            float inc = killIncrement.getOrDefault(event.getKilledEntity().getType(), default_kill_increment);

            if (is_awakened || inc == 0f) return;

            float current = dm.getDataValueOptional(t0001SkillDataKeys.ULTIMATE_METER).orElse(0f);

            dm.setDataSync(t0001SkillDataKeys.ULTIMATE_METER,
                    Math.clamp(current + inc, 0f, meter_capacity)
            );
        }, this, 2);


        //Slowly Deplete bar
        eventListener.registerContextAwareEvent(EpicFightEventHooks.Player.TICK_EPICFIGHT_MODE, (event, context) -> {

            boolean has_data = data_manager.hasData(t0001SkillDataKeys.IS_AWAKENED) && data_manager.hasData(t0001SkillDataKeys.ULTIMATE_METER);

            //Awaken tick eventHook post is inside #SkillEvents

            if(has_data && data_manager.getDataValue(t0001SkillDataKeys.IS_AWAKENED) && !container.getExecutor().getOriginal().isCreative() ){
                if(event.getPlayerPatch().getOriginal().tickCount % (Mth.floor(10 * reduction_coefficient)) == 0){

                    float meter_value = data_manager.getDataValue(t0001SkillDataKeys.ULTIMATE_METER);
                    float reduction = Math.max(meter_value - 0.1f, 0.0f);
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
