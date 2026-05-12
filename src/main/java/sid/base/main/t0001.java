package sid.base.main;

import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXHelper;
import com.mojang.logging.LogUtils;
import io.netty.util.internal.UnstableApi;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import sid.base.client.events.CameraAnimationManager;
import sid.base.client.input.t0001InputAction;
import sid.base.client.model.t0001Armatures;
import sid.base.events.ModBusEvents;
import sid.base.skill.VanillaSkillsCompatBuilding;
import sid.base.skill.t0001SkillCategories;
import sid.base.skill.t0001SkillSlots;
import sid.base.utils.ModRegistries;
import sid.base.world.capabilities.item.ExCapMovesets;
import sid.base.world.capabilities.item.ExCapWeaponPresets;
import sid.base.world.capabilities.t0001WeaponCategories;
import sid.base.world.item.CustomEnumParams;
import yesman.epicfight.api.client.input.action.InputAction;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.skill.SkillCategory;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(t0001.MODID)
public class t0001 {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "t0001";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "t0001" namespace



    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public t0001(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::constructMod);

        modEventBus.addListener(CameraAnimationManager::onCameraBuild);
        NeoForge.EVENT_BUS.addListener(CameraAnimationManager::onClientTick);
        NeoForge.EVENT_BUS.addListener(CameraAnimationManager::ComputeFOV);

        SkillSlot.ENUM_MANAGER.registerEnumCls(t0001.MODID, t0001SkillSlots.class);
        SkillCategory.ENUM_MANAGER.registerEnumCls(t0001.MODID, t0001SkillCategories.class);
        WeaponCategory.ENUM_MANAGER.registerEnumCls(t0001.MODID, t0001WeaponCategories.class);

        InputAction.ENUM_MANAGER.registerEnumCls(t0001.MODID, t0001InputAction.class);

        ModRegistries.DEFERRED_REGISTER_LIST.forEach(deferredRegister -> deferredRegister.register(modEventBus));

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        ICompatModule.loadCompatModule(modEventBus, VanillaSkillsCompatBuilding.class);



    }
    @UnstableApi
    public static String format(String s) {
        return String.format(s, MODID);
    }

    public static ResourceLocation identifier(String path){
        return ResourceLocation.fromNamespaceAndPath(MODID,path);
    }

    public static FX getmodfx(String fxname){
        return FXHelper.getFX(ResourceLocation.fromNamespaceAndPath(MODID,fxname));
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

        event.enqueueWork(t0001Armatures::registerEntityTypes);
        event.enqueueWork(CustomEnumParams::initExtensibleEnums);
        event.enqueueWork(this::registerCapabilities);
    }

    private void constructMod(final FMLConstructModEvent event){
        event.enqueueWork(SkillCategory.ENUM_MANAGER::loadEnum);
        event.enqueueWork(SkillSlot.ENUM_MANAGER::loadEnum);
        event.enqueueWork(InputAction.ENUM_MANAGER::loadEnum);

    }

    private void registerCapabilities() {
        EpicFightEventHooks.Registry.ENTITY_PATCH.registerEvent(ModBusEvents::registerEntityPatch);
        ExCapMovesets.modifyMoveset();
        ExCapWeaponPresets.addMoveset();
    }

}
