package sid.base.world.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import sid.base.main.t0001;

import java.util.function.Supplier;

@EventBusSubscriber(modid = t0001.MODID)
public class t0001Entities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, t0001.MODID);


    public static final Supplier<EntityType<Amogus>> AMOGUS = ENTITIES.register("amogus", () ->
            EntityType.Builder.of(Amogus::new, MobCategory.CREATURE)
                    .fireImmune().immuneTo(Blocks.WITHER_ROSE).sized(0.69F, 1.15F).clientTrackingRange(8).build("amogus")
    );

    public static final Supplier<EntityType<ShadowCloneEntity>> SHADOW_CLONE = ENTITIES.register("shadow_clone", () ->
            EntityType.Builder.of(ShadowCloneEntity::new, MobCategory.CREATURE)
                    .immuneTo(Blocks.WITHER_ROSE)
                    .sized(0.69F, 1.55F)
                    .clientTrackingRange(13)
                    .build("shadow_clone")
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
        event.register(SHADOW_CLONE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(DARKNESS_ENTITY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);

    }
}
