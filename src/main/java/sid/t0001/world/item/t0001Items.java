package sid.t0001.world.item;


import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import org.xame.t0001;
import sid.t0001.gameasset.t0001Entities;
import yesman.epicfight.world.item.EpicFightItemTier;

public class t0001Items {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, t0001.MODID);

    public static final RegistryObject<Item> CHICKEN_TIKAMASALA;

    public static final RegistryObject<Item> SANIC_SWURD;
    public static final RegistryObject<Item> KATANA;
    public static final RegistryObject<Item> DRAGON_GOD_SWORD;

    public static final RegistryObject<Item> DRAGON_GOD_SWORD_SHEATH;
    public static final RegistryObject<Item> SHEATH;
    public static final RegistryObject<Item> SANIC_SHEATH;

    public static final RegistryObject<Item> DRAGON_GOD_SWORD_BROKEN;

    static {
        SHEATH = ITEMS.register("sheath", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
        CHICKEN_TIKAMASALA = ITEMS.register("chicken_tiktok_masala", () -> new Item(new Item.Properties().durability(5)
                .food(new FoodProperties.Builder().meat().nutrition(5).effect(() -> new MobEffectInstance(MobEffects.SLOW_FALLING, 200), 0.10f).saturationMod(6f).build())));
        SANIC_SWURD = ITEMS.register("sanic_swurd", () -> new SwordItem(EpicFightItemTier.UCHIGATANA, 1, -1.86F, new Item.Properties().fireResistant().rarity(Rarity.RARE).defaultDurability(1851)));
        KATANA = ITEMS.register("katana", () -> new T001Item(new Item.Properties().fireResistant().rarity(Rarity.RARE).defaultDurability(2851), Tiers.IRON));
        DRAGON_GOD_SWORD = ITEMS.register("dragon_god_sword", () -> new DragonGodSwordItem(new Item.Properties().fireResistant().rarity(Rarity.create("LEGENDARY", ChatFormatting.GOLD)).defaultDurability(3951), Tiers.NETHERITE));
        SANIC_SHEATH = ITEMS.register("sanic_sheath", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
        DRAGON_GOD_SWORD_SHEATH = ITEMS.register("dragon_god_sword_sheath", () -> new Item(new Item.Properties().rarity(Rarity.create("LEGENDARY", ChatFormatting.GOLD))));
        DRAGON_GOD_SWORD_BROKEN = ITEMS.register("dragon_god_sword_broken", () -> new DragonGodSwordBrokenItem(new Item.Properties().rarity(Rarity.RARE).defaultDurability(42), Tiers.IRON));
    }

    public static final RegistryObject<Item> AMOGUS_SPAWN_EGG = ITEMS.register("amogus_spawn_egg",
            () -> new ForgeSpawnEggItem(t0001Entities.AMOGUS, 0xFF0000, 0x20FF50,
                    new Item.Properties().fireResistant()));


    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);

    }
}
