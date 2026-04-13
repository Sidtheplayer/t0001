package sid.base.skill.dodge;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import sid.base.events.global_events.GlobalEventHandlers;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.player.ModifyComboCounter;
import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.dodge.DodgeSkill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;


public class AccelerateSkill extends DodgeSkill {


    public AccelerateSkill(Builder<?> builder) {
        super(builder);
    }

    protected int DebuffDuration;

    @Override
    public void loadDatapackParameters(CompoundTag parameters) {
        super.loadDatapackParameters(parameters);
        if (parameters != null) {
            DebuffDuration = parameters.getInt("debuff_ticks");
        }
    }

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);

        eventListener.registerEvent(
                EpicFightEventHooks.Player.MODIFY_COMBO_COUNTER,
                event -> {
                    if (event.getCausal() == ModifyComboCounter.Causal.ANOTHER_ACTION_ANIMATION
                            && event.getAnimation().get().in(this.animations)
                    ) {
                        event.setNextValue(event.getPrevValue());
                    }

                }, this
        );

        eventListener.registerEvent(EpicFightEventHooks.Entity.ON_DODGE, event -> {
            MinecraftServer server = container.getServerExecutor().getOriginal().server;
            LivingEntity target = container.getExecutor().getTarget();
            if (target != null && event.getDamageSource().getDirectEntity() != target) return;
            LivingEntityPatch<?> targetPatch = EpicFightCapabilities.getEntityPatch(target, LivingEntityPatch.class);
            if (targetPatch != null) {
                if (!(targetPatch instanceof ServerPlayerPatch)) {
                    targetPatch.getAnimator().setHardPause(true);
                    targetPatch.getOriginal().setNoActionTime(DebuffDuration);
                    GlobalEventHandlers.DelayedTaskScheduler.schedule(server, DebuffDuration,
                            () ->
                                    targetPatch.getAnimator().setHardPause(false)
                    );
                    targetPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, DebuffDuration, 69));
                } else {
                    targetPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, DebuffDuration, 4));
                    targetPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, DebuffDuration, 69));
                }
            } else if (target instanceof Mob mob) {
                mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, DebuffDuration, 69));
                mob.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, DebuffDuration, 69));
                mob.setNoActionTime(DebuffDuration);
            }

        }, this);

    }

    @Override
    public Skill getPriorSkill() {
        return EpicFightSkills.FORBIDDEN_STRENGTH.get();
    }

}
