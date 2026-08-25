package sid.base.client.photon.executor;

import com.lowdragmc.photon.client.fx.EntityEffectExecutor;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.gameobject.IFXObject;
import com.lowdragmc.photon.client.gameobject.emitter.data.shape.MeshData;
import com.lowdragmc.photon.client.gameobject.emitter.particle.ParticleEmitter;
import com.lowdragmc.photon.client.gameobject.emitter.particle.ParticleRendererSetting;
import com.lowdragmc.photon.gui.editor.FXEditor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import sid.base.client.photon.LivingEpicFightModelMeshSource;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.renderer.patched.entity.PatchedEntityRenderer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@SuppressWarnings({"unchecked", "rawtypes"})
public class LivingEntityPatchEffect extends EntityEffectExecutor {

    public LivingEntityPatchEffect(FX fx, Level level, Entity entity, AutoRotate autoRotate) {
        super(fx, level, entity, autoRotate);
    }

    @Override
    public void updateFXObjectFrame(IFXObject fxObject, float partialTicks) {
        super.updateFXObjectFrame(fxObject, partialTicks);
        if(fxObject instanceof ParticleEmitter emitter){
            var values = emitter.runtime();
            LivingEntityPatch<?> entityPatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
            if(entityPatch == null) return;

            values.renderer.renderMode.set(ParticleRendererSetting.Mode.Model);
            PatchedEntityRenderer patchedrenderer = RenderEngine.getInstance().getEntityRenderer(entityPatch.getOriginal());
            AssetAccessor<SkinnedMesh> meshAccessor = patchedrenderer.getMeshProvider(entityPatch);
            values.renderer.model.set(new MeshData(new LivingEpicFightModelMeshSource(meshAccessor, entityPatch.getArmature(), entityPatch.getArmature()::getPoseMatrices)));
            values.renderer.useBlockUV.set(true);
        }

    }
}
