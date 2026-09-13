package sid.base.client.photon.executor;

import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.gameobject.IFXObject;
import com.lowdragmc.photon.client.gameobject.emitter.data.shape.Mesh;
import com.lowdragmc.photon.client.gameobject.emitter.data.shape.MeshData;
import com.lowdragmc.photon.client.gameobject.emitter.particle.ParticleEmitter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import sid.base.client.photon.LivingEpicFightModelMeshSource;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.renderer.patched.entity.PatchedEntityRenderer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public class EpicFightPatchEmitterMeshEffect extends JointTrackedEntityEffect {

    public static Map<Entity, List<EpicFightPatchEmitterMeshEffect>> CACHE = new HashMap<>();

    private AssetAccessor<SkinnedMesh> meshAccessor;
    private PatchedEntityRenderer patchedEntityRenderer;
    private Mesh.Type type;

    /**
     * @param fx             photon fx — FXHelper.getFX(ResourceLocation.parse("photon:trail"))
     * @param level          the world level
     * @param entity         the entity to track
     * @param joint          the joint for rotation and position updates
     * @param translation    bone-space offset
     * @param autoRotate     AutoRotate.NONE works for most cases
     * @param updateRotation if true, syncs rotation from the joint matrix each frame
     * @param type Mesh primitive type on which the particle should spawn
     */
    public EpicFightPatchEmitterMeshEffect(FX fx, Level level, Entity entity, Joint joint, Vec3f translation, AutoRotate autoRotate, boolean updateRotation, Mesh.Type type) {
        super(fx, level, entity, joint, translation, autoRotate, updateRotation);
        this.type = type;
    }

    @Override //Directly taken from EntityEffectExecutor
    public void start() {
        if (!entity.isAlive()) return;

        var effects = CACHE.computeIfAbsent(entity, p -> new ArrayList<>());
        if (shouldSkipStart(effects)) {
            return;
        }
        resetFinishedNotification();
        this.runtime = fx.createRuntime(true); //Setting deepCopy to true to hopefully fix overwriting problem of emitter shape
        var root = this.runtime.getRoot();
        root.updatePos(entity.getEyePosition().toVector3f().add(offset.x, offset.y, offset.z));
        root.updateRotation(rotation);
        root.updateScale(scale);
        this.runtime.emit(this, delay);
        effects.add(this);

    }

    @Override
    public void updateFXObjectTick(IFXObject fxObject) {
        if (runtime == null || fxObject != runtime.root) {
            return;
        }

        if (!entity.isAlive()) {
            runtime.destroy(forcedDeath);
            retire(CACHE, entity);
        } else if (runtimeEnded()) {
            retire(CACHE, entity);
        }
    }


    @Override
    public void updateFXObjectFrame(IFXObject fxObject, float partialTicks) {
        super.updateFXObjectFrame(fxObject, partialTicks);

        setEmitterMesh(fxObject, partialTicks);


    }

    public void setEmitterMesh(IFXObject fxObject, float ignoredDt){
        if(runtime != null) {
            if (fxObject instanceof ParticleEmitter emitter) {
                var values = emitter.runtime();
                LivingEntityPatch<?> entityPatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
                if (entityPatch == null) return;

                //added caching like this, if any problems remove them
                if (patchedEntityRenderer == null) {
                    patchedEntityRenderer = RenderEngine.getInstance().getEntityRenderer(entityPatch.getOriginal());
                }

                if (meshAccessor == null) {
                    meshAccessor = patchedEntityRenderer.getMeshProvider(entityPatch);
                }

                Mesh mesh = new Mesh();

                if(type == null){
                    type = Mesh.Type.Triangle;
                }

                mesh.setType(type);

                mesh.setMeshData(new MeshData(new LivingEpicFightModelMeshSource(entityPatch, meshAccessor)));

                values.config.shape.setShape(mesh);

            }
        }
    }


}
