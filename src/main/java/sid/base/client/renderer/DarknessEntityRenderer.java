package sid.base.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;
import sid.base.client.mesh.DarknessEntittyMesh;
import sid.base.client.mesh.t0001Meshes;
import sid.base.client.model.darkness;
import sid.base.world.entity.DarknessEntity;
import sid.base.world.entity.DarknessEntityPatch;

import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.client.renderer.patched.entity.PatchedLivingEntityRenderer;

public class DarknessEntityRenderer extends PatchedLivingEntityRenderer<DarknessEntity, DarknessEntityPatch, darkness,NDarknessEntityRenderer, DarknessEntittyMesh> {
    public DarknessEntityRenderer(EntityRendererProvider.Context context, EntityType entityType) {
        super(context, entityType);
    }


    @Override
    public AssetAccessor<DarknessEntittyMesh> getDefaultMesh() {
        return  t0001Meshes.DARKNESSMESH;
    }
}
