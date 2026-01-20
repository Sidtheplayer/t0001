package sid.t0001.utils;

import net.neoforged.neoforge.registries.DeferredRegister;
import sid.t0001.gameasset.t0001Entities;
import sid.t0001.gameasset.t0001Skills;
import sid.t0001.gameasset.t0001Sounds;
import sid.t0001.particle.t0001Particles;
import sid.t0001.skill.t0001SkillDataKeys;
import sid.t0001.world.item.t0001Items;
import sid.t0001.world.item.t0001Tab;

import java.util.List;

public abstract class t0001Registries {


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
