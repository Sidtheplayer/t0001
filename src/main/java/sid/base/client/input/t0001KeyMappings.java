package sid.base.client.input;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import sid.base.main.t0001;
import sid.base.network.RPC.RpcPacketIds;


@EventBusSubscriber(modid = t0001.MODID,value = Dist.CLIENT)
public class t0001KeyMappings {

    public static final KeyMapping SUPER_SKILL = new KeyMapping(
            "key." + t0001.MODID + ".awakening",
            InputConstants.UNKNOWN.getValue(), // grraaahaahh {kms}
            "key." + t0001.MODID + ".combat"
    );

    public static final KeyMapping DAGGER_THROW = new KeyMapping(
            "key." + t0001.MODID + ".dagger_throw",
            InputConstants.KEY_Z,
            "key." + t0001.MODID + ".combat"
    );

    public static final KeyMapping SHADOW_CLONE = new KeyMapping(
            "key." + t0001.MODID + ".shadow_clone",
            InputConstants.KEY_SEMICOLON,
            "key." + t0001.MODID + ".combat"
    );



    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(SUPER_SKILL);
        event.register(DAGGER_THROW);
        event.register(SHADOW_CLONE);
    }

    @SubscribeEvent
    public static void onKeybindPress(ClientTickEvent.Post event){

        if(DAGGER_THROW.consumeClick()){
            RPCPacketDistributor.rpcToServer(RpcPacketIds.TRIGGER_KUNAI_TELEPORT.id);
        }

    }

}
