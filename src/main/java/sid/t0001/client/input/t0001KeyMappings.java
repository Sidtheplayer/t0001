package sid.t0001.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.main.EpicFightMod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.MOD)
public class t0001KeyMappings {
    public static final KeyMapping SUPER_SKILL = new KeyMapping(
            "key." + EpicFightMod.MODID + ".super_skill",
            InputConstants.UNKNOWN.getValue(), // grraaahaahh {kms}
            "key." + EpicFightMod.MODID + ".combat"
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(SUPER_SKILL);
    }
}
