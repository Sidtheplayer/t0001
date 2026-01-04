package sid.t0001.world.item;

import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import sid.t0001.gameasset.t0001Entities;
import sid.t0001.main.t0001;

import java.util.function.Supplier;

public final class t0001Items {
    private t0001Items() {}

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(t0001.MODID);

    /* ------------------------------------------------------------ */
    /* Simple items                                                  */
    /* ------------------------------------------------------------ */

    public static final Supplier<Item> SHEATH =
            ITEMS.registerSimpleItem(
                    "sheath",
                    new Item.Properties().rarity(Rarity.EPIC)
            );

    public static final Supplier<Item> SANIC_SHEATH =
            ITEMS.registerSimpleItem(
                    "sanic_sheath",
                    new Item.Properties().rarity(Rarity.RARE)
            );

    public static final Supplier<Item> DRAGON_GOD_SWORD_SHEATH =
            ITEMS.registerSimpleItem(
                    "dragon_god_sword_sheath",
                    new Item.Properties().rarity(/*Rarity.valueOf("LEGENDARY", ChatFormatting.GOLD*/Rarity.EPIC));


    /* ------------------------------------------------------------ */
    /* Food                                                          */
    /* ------------------------------------------------------------ */

    public static final Supplier<Item> CHICKEN_TIKAMASALA =
            ITEMS.registerItem(
                    "chicken_tiktok_masala",
                    Item::new,
                    new Item.Properties()
                            .durability(5)
                            .food(new FoodProperties.Builder()
                                    .nutrition(5)
                                    .saturationModifier(6.0F)
                                    .effect(
                                            () -> new MobEffectInstance(MobEffects.SLOW_FALLING, 200),
                                            0.10F
                                    )
                                    .build()
                            )
            );

    /* ------------------------------------------------------------ */
    /* Weapons                                                       */
    /* ------------------------------------------------------------ */

    public static final Supplier<Item> SANIC_SWURD =
            ITEMS.registerItem(
                    "sanic_swurd",
                    props -> new SwordItem(
                            Tiers.IRON
//                            1,
//                            -1.86F
//                            props
                    ,
                    new Item.Properties()
                            .fireResistant()
                            .rarity(Rarity.RARE)
                            .durability(1851)
            ));

    public static final Supplier<Item> KATANA =
            ITEMS.registerItem(
                    "katana",
                    props -> new T001Item(props, Tiers.IRON),
                    new Item.Properties()
                            .fireResistant()
                            .rarity(Rarity.RARE)
                            .durability(2851)
            );

    public static final Supplier<Item> DRAGON_GOD_SWORD =
            ITEMS.registerItem(
                    "dragon_god_sword",
                    props -> new DragonGodSwordItem(props, Tiers.NETHERITE),
                    new Item.Properties()
                            .fireResistant()
                            .rarity(/*Rarity.create("LEGENDARY", ChatFormatting.GOLD)*/Rarity.EPIC)
                            .durability(3951)
            );

    public static final Supplier<Item> DRAGON_GOD_SWORD_BROKEN =
            ITEMS.registerItem(
                    "dragon_god_sword_broken",
                    props -> new DragonGodSwordBrokenItem(props, Tiers.IRON),
                    new Item.Properties()
                            .rarity(Rarity.RARE)
                            .durability(42)
            );

    /* ------------------------------------------------------------ */
    /* Spawn egg                                                     */
    /* ------------------------------------------------------------ */

    public static final Supplier<Item> AMOGUS_SPAWN_EGG =
            ITEMS.registerItem(
                    "amogus_spawn_egg",
                    props -> new SpawnEggItem(
                            t0001Entities.AMOGUS.get(),
                            0xFF0000,
                            0x20FF50,
                            props
                    ),
                    new Item.Properties().fireResistant()
            );

    /* ------------------------------------------------------------ */

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
