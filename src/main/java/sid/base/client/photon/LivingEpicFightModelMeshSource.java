package sid.base.client.photon;

import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.photon.client.gameobject.emitter.data.model.IModelSource;
import com.lowdragmc.photon.client.gameobject.emitter.data.model.PhotonMesh;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;

import java.util.Objects;
import java.util.function.Supplier;

import static sid.base.client.photon.EFPhotonMeshUtil.bakeSkinnedMesh;

@SuppressWarnings("ClassCanBeRecord")
@OnlyIn(Dist.CLIENT)
@LDLRegisterClient(name = "epicfight_model", registry = "photon:model_source")
public class LivingEpicFightModelMeshSource implements IModelSource {

    private final AssetAccessor<SkinnedMesh> meshAccessor;
    private final Armature armature;
    private final Supplier<OpenMatrix4f[]> posesSupplier; //ex: () - armature.getPoseMatrices()

    public LivingEpicFightModelMeshSource(AssetAccessor<SkinnedMesh> meshAccessor, Armature armature,
                                Supplier<OpenMatrix4f[]> posesSupplier) {
        this.meshAccessor = meshAccessor;
        this.armature = armature;
        this.posesSupplier = posesSupplier;
    }

    @Override
    public PhotonMesh getMesh() {
        if (meshAccessor.isEmpty()) return PhotonMesh.EMPTY;
        OpenMatrix4f[] poses = posesSupplier.get();
        if (poses == null) return PhotonMesh.EMPTY;
        return bakeSkinnedMesh(meshAccessor.get(), armature, poses); // fresh instance - MeshData rebuilds
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LivingEpicFightModelMeshSource other)) return false;
        return meshAccessor.registryName().equals(other.meshAccessor.registryName())
                && armature == other.armature;
    }

    @Override
    public int hashCode() {
        return Objects.hash(meshAccessor.registryName(), armature);
    }

    @Override public void invalidate() { /* nothing persistent to drop */ }
    @Override public IModelSource copy() { return new LivingEpicFightModelMeshSource(meshAccessor, armature, posesSupplier); }
    @Override public String name() { return "live_entity_mesh"; }
    @Override public void buildConfigurator(ConfiguratorGroup father) { /* not user-editable */ }
}
