package sid.t0001.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class t0001NetworkManager {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("t0001", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        INSTANCE.messageBuilder(SpawnLightningFxPacket.class, id())
                .encoder(SpawnLightningFxPacket::encode)
                .decoder(SpawnLightningFxPacket::new)
                .consumerMainThread(SpawnLightningFxPacket::handle)
                .add();


        INSTANCE.messageBuilder(StopLightningFxPacket.class, id())
                .encoder(StopLightningFxPacket::encode)
                .decoder(StopLightningFxPacket::new)
                .consumerMainThread(StopLightningFxPacket::handle)
                .add();
    }
}
