package sid.base.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import sid.base.main.t0001;


@EventBusSubscriber(modid = t0001.MODID,value = Dist.CLIENT)
public class t0001KeyMappings {

    public static final KeyMapping SUPER_SKILL = new KeyMapping(
            "key." + t0001.MODID + ".awakening",
            InputConstants.UNKNOWN.getValue(), // grraaahaahh {kms}
            "key." + t0001.MODID + ".combat"
    );



    public static final KeyMapping SHADOW_CLONE = new KeyMapping(
            "key." + t0001.MODID + ".shadow_clone",
            InputConstants.KEY_SEMICOLON,
            "key." + t0001.MODID + ".combat"
    );

    public static final KeyMapping UTILITY = new KeyMapping(
            "key." + t0001.MODID + ".utility_skill",
            InputConstants.KEY_SEMICOLON,
            "key." + t0001.MODID + ".combat"
    );

    public static final KeyMapping QUICK_TIME = new KeyMapping(
            "key." + t0001.MODID + ".quick_time",
            InputConstants.KEY_SPACE,
            "key." + t0001.MODID + ".combat"
    );



    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(SUPER_SKILL);
        event.register(QUICK_TIME);
        event.register(UTILITY);
        event.register(SHADOW_CLONE);
    }

    @SubscribeEvent
    public static void onKeybindPress(ClientTickEvent.Post event){

//        if(DAGGER_THROW.consumeClick()){
//            RPCPacketDistributor.rpcToServer(RpcPacketIds.TRIGGER_KUNAI_TELEPORT.id);
//        }

    }

}
