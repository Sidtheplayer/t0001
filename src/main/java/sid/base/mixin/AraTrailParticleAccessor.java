package sid.base.mixin;

import com.lowdragmc.photon.client.gameobject.particle.aratrail.AraTrailParticle;
import com.lowdragmc.photon.client.gameobject.particle.aratrail.ElasticArray;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

//MIT CODE FROM: https://github.com/dfdyz/EpicVFX
@Mixin(value = AraTrailParticle.class, remap = false)
public interface AraTrailParticleAccessor {

    @Invoker("updateDynamicData")
    void I_updateDynamicData(float t);

    @Invoker("updateVelocity")
    void I_updateVelocity(float t);

    @Invoker("physicsStep")
    void I_physicsStep(float timestep);

    @Invoker("emissionStep")
    void I_emissionStep(float t);

    @Invoker("snapLastPointToTransform")
    void I_snapLastPointToTransform();

    @Invoker("updatePointsLifecycle")
    void I_updatePointsLifecycle(float t);

    @Accessor("points")
    ElasticArray<AraTrailParticle.Point> A_points();

    @Accessor("isRemoved")
    boolean A_isRemoved();

    @Accessor("dieWhenAllTailsRemoved")
    boolean A_dieWhenAllTailsRemoved();

    @Accessor("worldPosition")
    Vector3f A_worldPosition();

    @Accessor("worldForward")
    Vector3f A_worldForward();

    @Accessor("worldUp")
    Vector3f A_worldUp();

    @Accessor("worldRight")
    Vector3f A_worldRight();

    @Accessor("colorMultiplier")
    Vector4f A_colorMultiplier();

    @Accessor("lifeTime")
    float A_lifeTime();
}