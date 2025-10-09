package sid.t0001.gameasset;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import sid.t0001.world.entity.Amogus;

public class t0001Entities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "t0001");


    public static final RegistryObject<EntityType<Amogus>> AMOGUS = ENTITIES.register("amogus", () ->
            EntityType.Builder.of(Amogus::new, MobCategory.CREATURE)
                    .fireImmune().immuneTo(Blocks.WITHER_ROSE).sized(0.7F, 2.4F).clientTrackingRange(8).build("amogus")
    );

@SubscribeEvent
    public static void onSpawnPlacementRegister(final SpawnPlacementRegisterEvent event) {
        event.register(AMOGUS.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);


    }
}
