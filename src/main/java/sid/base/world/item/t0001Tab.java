package sid.base.world.item;


import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.registries.DeferredRegister;
import sid.base.main.t0001;

import java.util.function.Supplier;


public class t0001Tab {

    private t0001Tab() {}


    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, t0001.MODID);


    public static final Supplier<CreativeModeTab> T0001_TAB = REGISTRY.register("t0001_tab", () -> CreativeModeTab.builder().icon(() ->
                    new ItemStack(t0001Items.SANIC_SHEATH.get()))
            .title(Component.translatable("creativetab.t0001_tab"))
            .displayItems((itemDisplayParameters, output) -> {
                output.accept(t0001Items.CHICKEN_TIKTOK_MASALA.get());
                output.accept(t0001Items.SANIC_SWURD.get());
                output.accept(t0001Items.SANIC_SHEATH.get());
                output.accept(t0001Items.KATANA.get());
                output.accept(t0001Items.AMOGUS_SPAWN_EGG.get());
                output.accept(t0001Items.DRAGON_GOD_SWORD.get());
            })
            .build()
    );


}