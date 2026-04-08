package sid.base.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import sid.base.main.t0001;


@EventBusSubscriber(modid = t0001.MODID,value = Dist.CLIENT)
public class t0001KeyMappings {

    public static final KeyMapping SUPER_SKILL = new KeyMapping(
            "key." + t0001.MODID + ".awakening",
            InputConstants.UNKNOWN.getValue(), // grraaahaahh {kms}
            "key." + t0001.MODID + ".combat"
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(SUPER_SKILL);
    }

}
