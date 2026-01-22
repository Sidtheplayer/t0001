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

import java.util.function.Supplier;

public class t0001Entities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, t0001.MODID);



    public static final Supplier<EntityType<Amogus>> AMOGUS = ENTITIES.register("amogus", () ->
            EntityType.Builder.of(Amogus::new, MobCategory.CREATURE)
                    .fireImmune().immuneTo(Blocks.WITHER_ROSE).sized(0.5F, 0.5F).clientTrackingRange(8).build("amogus")
    );

    public static final Supplier<EntityType<DarknessEntity>> DARKNESS_ENTITY = ENTITIES.register("darkness_entity", () ->
            EntityType.Builder.of(DarknessEntity::new, MobCategory.CREATURE)
                    .fireImmune().sized(2.5F, 1.5F).clientTrackingRange(8).build("darkness_entity")
    );

    @SubscribeEvent
    public static void onSpawnPlacementRegister(final RegisterSpawnPlacementsEvent event) {
        event.register(AMOGUS.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(DARKNESS_ENTITY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);

    }
}
