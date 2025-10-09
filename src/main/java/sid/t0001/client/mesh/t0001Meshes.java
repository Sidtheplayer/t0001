package sid.t0001.client.mesh;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.xame.t0001;
import yesman.epicfight.api.client.model.Meshes;

@OnlyIn(Dist.CLIENT)
public class t0001Meshes {

    /* Create mesh accessor statically,
    [| OCT 10 2025 2:40AM | all I had to do was delete the modsubscriber thing why did I ####### add it,
     when it was already working T^T] */

    public static final Meshes.MeshAccessor<AmogusMesh> AMOGUS = Meshes.MeshAccessor.create(
            t0001.MODID,
            "entity/amogus",
            (jsonModelLoader) -> jsonModelLoader.loadSkinnedMesh(AmogusMesh::new)
    );
}