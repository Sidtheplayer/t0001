package org.xame;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import sid.t0001.gameasset.t0001Sounds;
import sid.t0001.particle.t0001Particles;
import sid.t0001.skill.t0001SkillDataKeys;
import sid.t0001.world.item.t0001Items;
import sid.t0001.world.item.t0001Tab;

@Mod(t0001.MODID)
public class t0001 {
    public static final String MODID = "t0001";

    public t0001(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);

        // Register deferred registries
        t0001Items.ITEMS.register(bus);
        t0001Sounds.SOUNDS.register(bus);
        t0001SkillDataKeys.DATA_KEYS.register(bus);
        t0001Tab.register(bus);
        t0001Particles.PARTICLES.register(bus);

        // Mod lifecycle listeners
        bus.addListener(this::addCreative);
        bus.addListener(sid.t0001.gameasset.t0001Skills::registert0001Skills);

        // Client-only events
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            // add client-side listeners here if needed
        });

        // Optional compat loading
        if (ModList.get().isLoaded("some_other_mod")) {
            // load compat module
        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // add items to creative tabs if needed
    }
}
