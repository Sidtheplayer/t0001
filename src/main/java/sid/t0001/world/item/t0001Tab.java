package sid.t0001.world.item;


import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import org.xame.t0001;

public class t0001Tab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, t0001.MODID);

    public static final RegistryObject<CreativeModeTab> T0001_TAB = CREATIVE_MODE_TABS.register("t0001_tab", () -> CreativeModeTab.builder().icon(() ->
                    new ItemStack(t0001Items.SANIC_SHEATH.get()))
            .title(Component.translatable("creativetab.t0001_tab"))
            .displayItems((itemDisplayParameters, output) -> {
                output.accept(t0001Items.CHICKEN_TIKAMASALA.get());
                output.accept(t0001Items.SANIC_SWURD.get());
                output.accept(t0001Items.SANIC_SHEATH.get());
                output.accept(t0001Items.KATANA.get());
                output.accept(t0001Items.AMOGUS_SPAWN_EGG.get());
            })
            .build()
    );

    public static void register (IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }
}