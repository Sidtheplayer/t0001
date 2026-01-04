package sid.t0001.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;
import sid.t0001.client.mesh.DarknessEntittyMesh;
import sid.t0001.client.mesh.t0001Meshes;
import sid.t0001.client.model.darkness;
import sid.t0001.world.entity.DarknessEntity;
import sid.t0001.world.entity.DarknessEntityPatch;

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
