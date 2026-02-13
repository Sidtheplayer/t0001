package sid.base.skill.weapon_passives;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import org.jetbrains.annotations.NotNull;
import sid.base.skill.t0001SkillDataKeys;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.damagesource.EpicFightDamageTypes;

import java.util.Set;


public class DgsPassiveSkill extends Skill {


    public DgsPassiveSkill(SkillBuilder<?> builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);

        var data_manager = container.getDataManager();

        eventListener.registerContextAwareEvent(
                EpicFightEventHooks.Entity.TAKE_DAMAGE_INCOME,
                (event,ihatemylife) -> {

                    if (event.getResult() != AttackResult.ResultType.BLOCKED) return;

                    Set<@NotNull ResourceKey<DamageType>> Predicate = Set.of(
                            DamageTypes.ARROW,
                            DamageTypes.FIREBALL,
                            DamageTypes.MOB_PROJECTILE,
                            EpicFightDamageTypes.WITHER_BEAM
                    );


                    try {
                        if(event.isParried() && Predicate.contains(event.getDamageSource().typeHolder().unwrapKey().orElse(null))){
                            data_manager.setDataSync(t0001SkillDataKeys.PARRIED_A_PROJECTILE,true);
                        }else data_manager.setDataSync(t0001SkillDataKeys.PARRIED_A_PROJECTILE,false);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                },this
        );

    }
}
