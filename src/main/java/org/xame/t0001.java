package org.xame;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.slf4j.Logger;
import sid.t0001.client.model.t0001Armatures;
import sid.t0001.events.LightningBallHandler;
import sid.t0001.gameasset.t0001Entities;
import sid.t0001.gameasset.t0001Skills;
import sid.t0001.gameasset.t0001Sounds;
import sid.t0001.particle.t0001Particles;
import sid.t0001.skill.t0001SkillCategories;
import sid.t0001.skill.t0001SkillDataKeys;
import sid.t0001.skill.t0001SkillSlots;
import sid.t0001.world.capabilities.item.WeaponCapabilityPresets;
import sid.t0001.world.item.t0001Items;
import sid.t0001.world.item.t0001Tab;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillCategory;
import yesman.epicfight.skill.SkillSlot;

import static sid.t0001.skill.weaponinnate.t0001InnateOne.*;

@Mod(t0001.MODID)
public class t0001 {
    public static final String MODID = "t0001";
    public static final Logger LOGGER =  LogUtils.getLogger();

    public t0001(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);


        SkillCategories.ENUM_MANAGER.registerEnumCls(t0001.MODID,t0001SkillCategories.class);
        SkillSlot.ENUM_MANAGER.registerEnumCls(t0001.MODID, t0001SkillSlots.class);

        // Register deferred registries
        t0001Items.ITEMS.register(bus);
        t0001Sounds.SOUNDS.register(bus);
        t0001SkillDataKeys.DATA_KEYS.register(bus);
        t0001Tab.register(bus);
        t0001Particles.PARTICLES.register(bus);
        t0001Entities.ENTITIES.register(bus);
        LightningBallHandler.register();

        // Mod lifecycle listeners
        bus.addListener(this::addCreative);
        bus.addListener(this::commonSetup);
        bus.addListener(this::constructMod);
        bus.addListener(WeaponCapabilityPresets::registerMovesets);
        bus.addListener(t0001Skills::registert0001Skills);

        // Client-only events
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            // add client-side listeners here if needed
            VideoOverlayRenderer.preloadVideo();
        });

        // Optional compat loading
        if (ModList.get().isLoaded("some_other_mod")) {
            // load compat module
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(t0001Armatures::registerEntityTypes);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // add items to creative tabs if needed
    }
    private void constructMod(final FMLConstructModEvent event){
        event.enqueueWork(SkillCategory.ENUM_MANAGER::loadEnum);
        event.enqueueWork(SkillSlot.ENUM_MANAGER::loadEnum);
    }



}
