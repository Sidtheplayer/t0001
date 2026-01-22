package sid.base.utils;

import net.neoforged.neoforge.registries.DeferredRegister;
import sid.base.gameasset.t0001Entities;
import sid.base.gameasset.t0001Skills;
import sid.base.gameasset.t0001Sounds;
import sid.base.particle.t0001Particles;
import sid.base.skill.t0001SkillDataKeys;
import sid.base.world.item.t0001Items;
import sid.base.world.item.t0001Tab;

import java.util.List;

public abstract class ModRegistries {


    public static final List<DeferredRegister<?>> DEFERRED_REGISTER_LIST =
            List.of(
                    t0001Items.ITEMS,
                    t0001Skills.REGISTRY,
                    t0001Sounds.REGISTRY,
                    t0001Entities.ENTITIES,
                    t0001Particles.PARTICLES,
                    t0001Tab.REGISTRY,
                    t0001SkillDataKeys.DATA_KEYS
            );
}
