package sid.t0001.client.mesh;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xame.t0001;
import yesman.epicfight.api.client.forgeevent.PrepareModelEvent;
import yesman.epicfight.api.client.model.Meshes;

@Mod.EventBusSubscriber(
        modid = t0001.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = {Dist.CLIENT}
)
@OnlyIn(Dist.CLIENT)
public class t0001Meshes {

    public static Meshes.MeshAccessor<AmogusMesh> AMOGUS;

    @SubscribeEvent
    public static void onMeshBuild(PrepareModelEvent event) {
        AMOGUS = Meshes.MeshAccessor.create(
                t0001.MODID,
                "entity/amogus",
                (jsonModelLoader) -> jsonModelLoader.loadSkinnedMesh(AmogusMesh::new)
        );
    }
}