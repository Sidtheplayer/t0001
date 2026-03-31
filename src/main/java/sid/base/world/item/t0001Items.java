package sid.base.world.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredRegister;
import sid.base.gameasset.t0001Entities;
import sid.base.main.t0001;
import yesman.epicfight.world.item.TieredWeaponItem;

import java.util.function.Supplier;

public final class t0001Items {
    private t0001Items() {}

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(t0001.MODID);


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

    public static final Supplier<Item> DRAGON_GOD_SWORD_SHEATHED =
            ITEMS.registerSimpleItem(
                    "dragon_god_sword_sheathed",
                    new Item.Properties().rarity(CustomEnumParams.CustomRarities.TRANSCENDENT));



    public static final Supplier<Item> CHICKEN_TIKTOK_MASALA =
            ITEMS.registerItem(
                    "chicken_tiktok_masala", // don't question me intelliJ said this was the correct name
                    Item::new,
                    new Item.Properties()
                            .rarity(Rarity.UNCOMMON)
                            .stacksTo(16)
                            .jukeboxPlayable(JukeboxSongs.STAL) // little easter egg
                            .food(new FoodProperties.Builder()
                                    .nutrition(5)
                                    .saturationModifier(6.0F)
                                    .effect(() -> new MobEffectInstance(MobEffects.SLOW_FALLING, 200), 0.50F)
                                    .effect(() -> new MobEffectInstance(MobEffects.SATURATION, 200), 0.70F)
                                    .effect(() -> new MobEffectInstance(MobEffects.WIND_CHARGED, 200), 0.30F)
                                    .build()
                            )
            );


    public static final Supplier<Item> SANIC_SWURD =
            ITEMS.registerItem(
                    "sanic_swurd",
                    props -> new SanicSwordItem(
                            props,Tiers.IRON),
                    new Item.Properties()
                            .fireResistant()
                            .rarity(Rarity.RARE)
                            .durability(1851)
                            .attributes(TieredWeaponItem.createAttributes(1, -1.86F))
            );

    public static final Supplier<Item> KATANA =
            ITEMS.registerItem(
                    "katana",
                    props -> new SwordItem(Tiers.IRON,
                    new Item.Properties()
                            .fireResistant()
                            .rarity(Rarity.RARE)
                            .durability(2851)
                            .attributes(TieredWeaponItem.createAttributes(6.5f,0.05f))
            ));

    public static final Supplier<Item> DRAGON_GOD_SWORD =
         ITEMS.register("dragon_god_sword",
                 ()-> new DragonGodSwordItem(
                         new Item.Properties()
                                 .attributes(DragonGodSwordItem.createDragonGodSwordAttributes(5.0f,-1.75f,Tiers.NETHERITE).withTooltip(true))
                                 .rarity(CustomEnumParams.CustomRarities.TRANSCENDENT)
                                 .durability(3951)
                                 .fireResistant()
                                 .setNoRepair()

                 ));

    public static final Supplier<Item> DRAGON_GOD_SWORD_BROKEN =
            ITEMS.registerItem(
                    "dragon_god_sword_broken",
                    props -> new DragonGodSwordBrokenItem(props, Tiers.IRON),
                    new Item.Properties()
                            .rarity(Rarity.RARE)
                            .durability(42)
                            .attributes(TieredWeaponItem.createAttributes(Tiers.DIAMOND,1.75f,1.25f))
            );


    /* Spawn eggs */
    @SuppressWarnings("deprecation") //I cannot be bothered
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


}
