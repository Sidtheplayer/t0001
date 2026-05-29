package sid.base.mixin;

import com.lowdragmc.photon.client.gameobject.particle.aratrail.AraTrailParticle;
import com.lowdragmc.photon.client.gameobject.particle.aratrail.ElasticArray;
import it.unimi.dsi.fastutil.ints.IntList;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

//MIT CODE FROM: https://github.com/dfdyz/EpicVFX
@Mixin(value = AraTrailParticle.class, remap = false)
public interface AraTrailParticleAccessor {

    @Invoker("updateDynamicData")
    void I_updateDynamicData(float t);

    @Invoker("updateVelocity")
    void I_updateVelocity(float t);

    @Invoker("emissionStep")
    void I_emissionStep(float t);

    @Invoker("snapLastPointToTransform")
    void I_snapLastPointToTransform();

    @Invoker("updatePointsLifecycle")
    void I_updatePointsLifecycle(float t);

    @Invoker("clearMeshData")
    void I_clearMeshData();

    @Invoker("updateSegmentMesh")
    void I_updateSegmentMesh(int start, int end, Vector3f localCamPosition, float partialTicks);

    @Accessor("points")
    ElasticArray<AraTrailParticle.Point> A_points();

    @Final
    @Accessor("discontinuities")
    IntList A_discontinuities();

    @Final
    @Accessor("vertices")
    List<Vector3f> A_vertices();

    @Final
    @Accessor("tris")
    IntList A_tris();

    @Final
    @Accessor("normals")
    List<Vector3f> A_normals();

    @Final
    @Accessor("tangents")
    List<Vector4f> A_tangents();

    @Final
    @Accessor("uvs")
    List<Vector2f> A_uvs();

    @Final
    @Accessor("vertColors")
    List<Vector4f> A_vertColors();

}