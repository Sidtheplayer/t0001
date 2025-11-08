package sid.t0001.world.item;


import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import org.xame.t0001;
import sid.t0001.gameasset.t0001Entities;
import yesman.epicfight.world.item.ExtendedRarity;

public class t0001Items {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, t0001.MODID);

    public static final RegistryObject<Item> CHICKEN_TIKAMASALA =
            ITEMS.register("chicken_tiktok_masala", () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().nutrition(5).saturationMod(6f).build())));
    public static final RegistryObject<Item> SANIC_SWURD =
            ITEMS.register("sanic_swurd", () -> new T001Item(new Item.Properties().fireResistant().rarity(Rarity.RARE).defaultDurability(2851), Tiers.IRON));
    public static final RegistryObject<Item> SANIC_SHEATH = ITEMS.register("sanic_sheath", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> SHEATH = ITEMS.register("sheath", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> KATANA =
            ITEMS.register("katana", () -> new T001Item(new Item.Properties().fireResistant().rarity(Rarity.RARE).defaultDurability(2851), Tiers.IRON));
    public static final RegistryObject<Item> DRAGON_GOD_SWORD =
            ITEMS.register("dragon_god_sword", () -> new T001Item(new Item.Properties().fireResistant().rarity(ExtendedRarity.UNIQUE).defaultDurability(2851), Tiers.NETHERITE));



    public static final RegistryObject<Item> AMOGUS_SPAWN_EGG = ITEMS.register("amogus_spawn_egg",
            () -> new ForgeSpawnEggItem(t0001Entities.AMOGUS, 0xFF0000, 0x20FF50,
                    new Item.Properties().fireResistant()));


    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);

    }
}
