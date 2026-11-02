package sid.base.skill.character_util_skill;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import sid.base.network.RPC.ServerSidePacketDelegations;
import sid.base.skill.t0001SkillDataKeys;
import sid.base.skill.t0001Skills;
import sid.base.world.entity.JunKunaiEntity;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;

public class MinatoTPSkill extends Skill {

    private float cooldownSeconds = 0.25f;

    @Override
    public void loadDatapackParameters(CompoundTag parameters) {
        super.loadDatapackParameters(parameters);
        if(parameters.contains("cooldown_secs")){
            cooldownSeconds = parameters.getFloat("cooldown_secs");
        }
    }

    public MinatoTPSkill(SkillBuilder<?> builder) {
        super(builder);
    }

    @Override
    public Skill getPriorSkill() {
        return t0001Skills.FANG_COUNTER.get();
    }


    private boolean hasKunaiThrown(Player player){
        JunKunaiEntity kunai = JunKunaiEntity.KunaiMap.get(player);
        return kunai != null && kunai.isAlive();
    }

    @Override
    public boolean checkExecuteCondition(SkillContainer container) {
        return container.getDataManager().getDataValue(t0001SkillDataKeys.COOLDOWN_TICK) <= 0.0F;
    }

    @Override
    public void executeOnServer(SkillContainer container, CompoundTag args) {
        super.executeOnServer(container, args);

        ServerPlayer player = container.getServerExecutor().getOriginal();

        if (hasKunaiThrown(player)) {
            if (JunKunaiEntity.tryTeleportShooterToKunai(player)) {
                container.getDataManager().setDataSync(
                        t0001SkillDataKeys.COOLDOWN_TICK, this.cooldownSeconds);
            }
        } else {
            JunKunaiEntity.KunaiMap.remove(player);
            ServerSidePacketDelegations.throwKunai(player);
        }
    }

    @Override
    public void updateContainer(SkillContainer container) {
        super.updateContainer(container);

        if (container.getDataManager().getDataValue(t0001SkillDataKeys.COOLDOWN_TICK) > 0.0F) {
            container.runOnServer(patch -> {
                float next = Math.max(0.0F,
                        container.getDataManager().getDataValue(t0001SkillDataKeys.COOLDOWN_TICK)
                                - EpicFightSharedConstants.A_TICK);
                container.getDataManager().setDataSync(t0001SkillDataKeys.COOLDOWN_TICK, next);
            });
        }
    }



}
