package sid.base.client.photon;

import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.photon.client.gameobject.emitter.data.model.IModelSource;
import com.lowdragmc.photon.client.gameobject.emitter.data.model.PhotonMesh;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.Objects;


@SuppressWarnings("ClassCanBeRecord")
@OnlyIn(Dist.CLIENT)
@LDLRegisterClient(name = "epicfight_model", registry = "photon:model_source")
public class LivingEpicFightModelMeshSource implements IModelSource {

    private final LivingEntityPatch<?> entityPatch;
    private final AssetAccessor<SkinnedMesh> meshAccessor;

    public LivingEpicFightModelMeshSource(LivingEntityPatch<?> entityPatch, AssetAccessor<SkinnedMesh> meshAccessor) {
        this.entityPatch = entityPatch;
        this.meshAccessor = meshAccessor;
    }

    @Override
    public PhotonMesh getMesh() {
        if (meshAccessor.isEmpty()) return PhotonMesh.EMPTY;
        return EFPhotonMeshUtil.bakeEntityWithArmor(entityPatch, meshAccessor.get());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LivingEpicFightModelMeshSource other)) return false;
        return entityPatch.getOriginal().getId() == other.entityPatch.getOriginal().getId()
                && meshAccessor.registryName().equals(other.meshAccessor.registryName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityPatch.getOriginal().getId(), meshAccessor.registryName());
    }

    @Override public void invalidate() { }
    @Override public IModelSource copy() { return new LivingEpicFightModelMeshSource(entityPatch, meshAccessor); }
    @Override public String name() { return "live_entity_mesh"; }
    @Override public void buildConfigurator(ConfiguratorGroup father) { }
}
