package sid.base.skill.awakening;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import sid.base.world.entity.t0001Entities;
import sid.base.skill.t0001Skills;
import sid.base.main.t0001;
import sid.base.skill.t0001SkillCategories;
import sid.base.skill.t0001SkillSlots;
import sid.base.world.entity.ShadowCloneEntity;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPRemoveSkillAndLearn;
import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;

import static sid.base.world.entity.ShadowCloneEntity.getShadowCloneList;

public class ShadowCloneSkill extends Skill {

    public ShadowCloneSkill(SkillBuilder<?> builder) {
        super(builder);
        builder.setActivateType(ActivateType.ONE_SHOT);
        builder.setResource(Resource.STAMINA);
        builder.setCategory(t0001SkillCategories.AWAKENING_EXTRA_SKILL);
    }

    @Override
    public void setConsumption(SkillContainer container, float value) {

       int current_clones =   getShadowCloneList(container.getExecutor().getOriginal()).size();

       super.setConsumption(container, (float) Math.max(current_clones * 20, 10) );


    }

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);

        eventListener.registerEvent(EpicFightEventHooks.Player.TICK_EPICFIGHT_MODE, (event) -> {

            boolean valid_awakening_skill = container.getExecutor().getSkill(t0001SkillSlots.AWAKENING).hasSkill(t0001Skills.Jun_AWAKEN.value());

            if(!valid_awakening_skill && !container.getExecutor().isLogicalClient()) {
                try {

                    Holder<Skill> removedSkill = container.getSkill().holder();

                    container.setSkill(null);

                    EpicFightNetworkManager.sendToPlayer(new SPRemoveSkillAndLearn(removedSkill, t0001SkillSlots.AWAKENING_EXTRA_SKILL), container.getServerExecutor().getOriginal());
                    EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(container.createSyncPacketToRemotePlayer(), container.getServerExecutor().getOriginal());

                    container.getExecutor().getSkill(t0001SkillSlots.AWAKENING_EXTRA_SKILL).setSkill(EpicFightSkills.EMPTY.value());

                } catch (Exception e) {

                    t0001.LOGGER.error("unable to remove extra skill: {}", e.getMessage());

                }
            }

        },this);

    }

    @Override
    public void executeOnServer(SkillContainer container, CompoundTag args) {
        super.executeOnServer(container, args);

        ServerLevel level = container.getServerExecutor().getOriginal().serverLevel();

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
            level.addFreshEntity(shadowCloneEntity);
        }


    }


    @Override
    public Skill getPriorSkill() {
        return t0001Skills.Jun_AWAKEN.get();
    }


}
