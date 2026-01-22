package sid.base.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.EntityType;
import sid.base.client.mesh.AmogusMesh;
import sid.base.client.model.AmogusModel;
import sid.base.world.entity.Amogus;
import sid.base.world.entity.AmogusPatch;
import yesman.epicfight.api.asset.AssetAccessor;
import sid.base.client.mesh.t0001Meshes;
import yesman.epicfight.client.renderer.patched.entity.PatchedLivingEntityRenderer;
import yesman.epicfight.client.renderer.patched.layer.PatchedItemInHandLayer;

public class AmogusRenderer extends PatchedLivingEntityRenderer<Amogus, AmogusPatch, AmogusModel,NAmogusRenderer, AmogusMesh> {
    public AmogusRenderer(EntityRendererProvider.Context context, EntityType entityType) {
        super(context, entityType);
        this.addPatchedLayer(ItemInHandLayer.class, new PatchedItemInHandLayer<>());
    }


    @Override
    public AssetAccessor<AmogusMesh> getDefaultMesh() {
        return  t0001Meshes.AMOGUS;
    }
}
