package sid.t0001.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;
import sid.t0001.client.mesh.AmogusMesh;
import sid.t0001.client.model.AmogusModel;
import sid.t0001.world.entity.Amogus;
import sid.t0001.world.entity.AmogusPatch;
import yesman.epicfight.api.asset.AssetAccessor;
import sid.t0001.client.mesh.t0001Meshes;
import yesman.epicfight.client.renderer.patched.entity.PatchedLivingEntityRenderer;

public class AmogusRenderer extends PatchedLivingEntityRenderer<Amogus, AmogusPatch<Amogus>, AmogusModel,NAmogusRenderer, AmogusMesh> {
    public AmogusRenderer(EntityRendererProvider.Context context, EntityType entityType) {
        super(context, entityType);
    }


    @Override
    public AssetAccessor<AmogusMesh> getDefaultMesh() {
        return t0001Meshes.AMOGUS;
    }
}
