package sid.base.main;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = t0001.MODID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue PARRY_PARTICLE = BUILDER.comment("Whether to enable custom photon particle on parry and block")
            .define("parryParticle", true);

    private static final ModConfigSpec.BooleanValue CAM_HEAD_CLIPPING_TOGGLE = BUILDER.comment("Whether to hide the head if the camera goes inside the player head during camera animation")
            .define("camHeadClipToggle", true);

    private static final ModConfigSpec.IntValue JUN_KUNAI_LIFETIME = BUILDER.comment("Kunai lifetime (in ticks) to allow teleport for players")
            .defineInRange("junKunaiLifetime", 420, 0, Integer.MAX_VALUE);


    private static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER.comment("A list of items to log on common setup.")
            .defineListAllowEmpty(
                    "items",
                    List.of("minecraft:iron_ingot"),
                    () -> "minecraft:iron_ingot",
                    Config::validateItemName);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean parryParticle;
    public static boolean camHeadClipToggle;
    public static int junKunaiLifetime;
    public static Set<Item> items = Set.of();

    private static boolean validateItemName(final Object obj) {
        if (!(obj instanceof String itemName)) {
            return false;
        }
        ResourceLocation rl = ResourceLocation.tryParse(itemName);
        return rl != null && BuiltInRegistries.ITEM.containsKey(rl);
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }

        parryParticle = PARRY_PARTICLE.get();
        camHeadClipToggle = CAM_HEAD_CLIPPING_TOGGLE.get();
        junKunaiLifetime = JUN_KUNAI_LIFETIME.get();


        items = ITEM_STRINGS.get().stream()
                .map(ResourceLocation::tryParse)
                .filter(Objects::nonNull)
                .map(BuiltInRegistries.ITEM::get)
                .collect(Collectors.toUnmodifiableSet());
    }
}