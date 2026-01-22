package sid.base.client.mesh;


import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import sid.base.main.t0001;
import yesman.epicfight.api.client.model.Meshes;

@OnlyIn(Dist.CLIENT)
public class t0001Meshes {

    /* Create mesh accessor statically,
                                        amazingly nothing changed from 1.20.1 forge to 1.21.1 neoforge*/

    public static final Meshes.MeshAccessor<AmogusMesh> AMOGUS = Meshes.MeshAccessor.create(
            t0001.MODID,
            "entity/amogus",
            (jsonModelLoader) -> jsonModelLoader.loadSkinnedMesh(AmogusMesh::new)
    );

    public static final Meshes.MeshAccessor<DarknessEntittyMesh> DARKNESSMESH = Meshes.MeshAccessor.create(
            t0001.MODID,
            "entity/darkness_entity",
            (jsonModelLoader) -> jsonModelLoader.loadSkinnedMesh(DarknessEntittyMesh::new)
    );
}