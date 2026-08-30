package sid.base.skill.awakening;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import sid.base.skill.t0001SkillSlots;
import sid.base.world.entity.t0001Entities;
import sid.base.skill.t0001Skills;
import sid.base.skill.t0001SkillCategories;
import sid.base.world.entity.ShadowCloneEntity;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;

import java.util.List;

public class ShadowCloneSkill extends Skill {

    public ShadowCloneSkill(SkillBuilder<?> builder) {
        super(builder);
        builder.setActivateType(ActivateType.ONE_SHOT);
        builder.setResource(Resource.STAMINA);
        builder.setCategory(t0001SkillCategories.AWAKENING_EXTRA_SKILL);
    }


    @Override
    public boolean canExecute(SkillContainer container) {
        return super.canExecute(container) && !container.getExecutor().getSkill(t0001SkillSlots.AWAKENING).isEmpty() &&
                container.getExecutor().getSkill(t0001SkillSlots.AWAKENING).hasSkill(t0001Skills.Jun_AWAKEN.value());
    }

    @Override
    public void executeOnServer(SkillContainer container, CompoundTag args) {
        super.executeOnServer(container, args);

        ServerLevel level = (ServerLevel) container.getServerExecutor().getOriginal().level();

        ShadowCloneEntity shadowCloneEntity = new ShadowCloneEntity(t0001Entities.SHADOW_CLONE.get(),
                level
        );

        shadowCloneEntity.setOwnerUUID(container.getServerExecutor().getOriginal().getUUID());

        Vec3 playerPos = container.getServerExecutor().getOriginal().position();

        double rand_angle = level.random.nextDouble() * Math.PI * 2.0;
        double rand_distance = 1.5 + level.random.nextDouble() * 1.5;

        double x = playerPos.x + Math.cos(rand_angle) * rand_distance;
        double z = playerPos.z + Math.sin(rand_angle) * rand_distance;

        Vec3 spawnPos = new Vec3(x, playerPos.y, z);

        if (level.noCollision(shadowCloneEntity, shadowCloneEntity.getBoundingBox())) {
            shadowCloneEntity.setPos(spawnPos);
            shadowCloneEntity.setTame(true,true);
            level.addFreshEntity(shadowCloneEntity);
        } else {
            container.getServerExecutor().getOriginal().displayClientMessage(
                    Component.literal("No valid space nearby"), true
            );
        }


    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
        if (container.getExecutor().isLogicalClient()) return;

        MinecraftServer server = container.getExecutor().getOriginal().getServer();

        if (server != null) {
            server.executeIfPossible(
                    () -> {
                    List<ShadowCloneEntity> e = ShadowCloneEntity.getShadowCloneList(container.getExecutor().getOriginal());
                    e.forEach(LivingEntity::kill);
                    }
            );
        }
    }

    @Override
    public Skill getPriorSkill() {
        return t0001Skills.Jun_AWAKEN.get();
    }


}
