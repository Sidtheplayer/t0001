package sid.base.gameasset;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import sid.base.main.t0001;
import sid.base.world.entity.Amogus;
import sid.base.world.entity.DarknessEntity;
import sid.base.world.entity.DemonCoredDrone;
import sid.base.world.entity.JunKunaiEntity;

import java.util.function.Supplier;

public class t0001Entities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, t0001.MODID);


    public static final Supplier<EntityType<Amogus>> AMOGUS = ENTITIES.register("amogus", () ->
            EntityType.Builder.of(Amogus::new, MobCategory.CREATURE)
                    .fireImmune().immuneTo(Blocks.WITHER_ROSE).sized(0.69F, 1.15F).clientTrackingRange(8).build("amogus")
    );

    public static final Supplier<EntityType<DarknessEntity>> DARKNESS_ENTITY = ENTITIES.register("darkness_entity", () ->
            EntityType.Builder.of(DarknessEntity::new, MobCategory.MONSTER)
                    .fireImmune().sized(2.5F, 4.5F).clientTrackingRange(8).build("darkness_entity")
    );

    public static final Supplier<EntityType<JunKunaiEntity>> JUN_KUNAI_PROJECTILE =
            ENTITIES.register("jun_kunai", ()->
                    EntityType.Builder.<JunKunaiEntity>of(JunKunaiEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.15f).build("jun_kunai")

            );

    public static final Supplier<EntityType<DemonCoredDrone>> DEMONCOREDRONE = ENTITIES.register("demon_core_drone", () ->
            EntityType.Builder.of(DemonCoredDrone::new, MobCategory.MONSTER)
                    .fireImmune().immuneTo(Blocks.WITHER_ROSE).sized(1.0F, 1.15F).clientTrackingRange(8).build("demon_core_drone")
    );

    @SubscribeEvent
    public static void onSpawnPlacementRegister(final RegisterSpawnPlacementsEvent event) {
        event.register(AMOGUS.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(DARKNESS_ENTITY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);

    }
}
