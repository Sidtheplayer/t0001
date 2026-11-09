package sid.base.main;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = t0001.MODID, value = Dist.CLIENT)
public class ClientConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.DoubleValue GLOBAL_HDR_INTENSITY = BUILDER.comment("Global HDR intensity multiplier")
            .defineInRange("globalHDRIntensity", 1.0, 0.0, Float.MAX_VALUE);

    private static final ModConfigSpec.BooleanValue CameraAnimationToggle = BUILDER.comment("Whether to enable Camera Animations").define("camAniToggle", true);

    private static final ModConfigSpec.IntValue GLOBAL_FALLBACK_TRAIL_HDR_MODE = BUILDER.comment("Default Global HDR mode to use with fallback photon weapon trails " + "(0 = ADDITIVE HDR MODE, 1 = MULTIPLICATIVE). Default is '1'")
            .defineInRange("globalFallBackTrailHDRMode", 1, 0, 1);


    private static final ModConfigSpec.ConfigValue<List<? extends String>> GLOW_TRAIL_ITEMS = BUILDER.comment("Items that should use globalHDRIntensity in fallback trails. Use resource locations, e.g. \"minecraft:diamond_sword\".")
            .defineListAllowEmpty(
                    "glowTrailItems",
                    List.of("minecraft:iron_ingot", "wom:solar"),
                    () -> "minecraft:iron_ingot",
                    ClientConfig::validateItemName);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean camAniToggle;
    public static float globalHDRIntensity;
    public static int globalFallBackHDRMode;
    public static Set<Item> glowTrailItems = Set.of();

    private static boolean validateItemName(final Object obj) {
        if (!(obj instanceof String itemName)) {
            return false;
        }
        ResourceLocation rl = ResourceLocation.tryParse(itemName);
        return rl != null && BuiltInRegistries.ITEM.containsKey(rl);
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (!event.getConfig().getSpec().equals(SPEC)) {
            return;
        }

        camAniToggle = CameraAnimationToggle.get();

        globalHDRIntensity = GLOBAL_HDR_INTENSITY.get().floatValue();
        globalFallBackHDRMode = GLOBAL_FALLBACK_TRAIL_HDR_MODE.get();

        glowTrailItems = GLOW_TRAIL_ITEMS.get().stream()
                .map(ResourceLocation::tryParse)
                .filter(Objects::nonNull)
                .map(BuiltInRegistries.ITEM::get)
                .collect(Collectors.toSet());
    }

}