package sid.base.skill.character_util_skill;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import sid.base.network.RPC.ServerSidePacketDelegations;
import sid.base.skill.t0001SkillCategories;
import sid.base.skill.t0001Skills;
import sid.base.world.entity.JunKunaiEntity;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;

public class MinatoTPSkill extends Skill {

    public MinatoTPSkill(SkillBuilder<?> builder) {
        super(builder);
        builder.setCategory(t0001SkillCategories.CHARACTER_UTIL_SKILL);
        builder.setResource(Resource.COOLDOWN);
    }

    @Override
    public Skill getPriorSkill() {
        return t0001Skills.FANG_COUNTER.get();
    }

    @Override
    public void executeOnServer(SkillContainer container, CompoundTag args) {
        super.executeOnServer(container, args);

        try {
            ServerPlayer player = container.getServerExecutor().getOriginal();

            JunKunaiEntity kunai = JunKunaiEntity.KunaiMap.get(player);

            if (kunai != null && kunai.isAlive()) {
                JunKunaiEntity.tryTeleportShooterToKunai(player);
            } else {
                JunKunaiEntity.KunaiMap.remove(player);
                ServerSidePacketDelegations.throwKunai(player);
            }
        } catch (Throwable ignored) {}


    }


}
