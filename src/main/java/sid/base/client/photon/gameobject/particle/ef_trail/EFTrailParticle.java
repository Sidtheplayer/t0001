package sid.base.client.photon.gameobject.particle.ef_trail;


import com.lowdragmc.photon.client.gameobject.emitter.IParticleEmitter;
import com.lowdragmc.photon.client.gameobject.particle.aratrail.AraTrailParticle;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import org.joml.*;
import sid.base.client.photon.fx.EFTrailExecutor;
import sid.base.client.photon.gameobject.emitter.ef_trail.EFTrailConfig;
import sid.base.mixin.AraTrailParticleAccessor;
import yesman.epicfight.api.client.animation.property.TrailInfo;

public class EFTrailParticle extends AraTrailParticle {
    public EFTrailExecutor efTrailExecutor;
    public final AraTrailParticleAccessor THIS;

    public EFTrailParticle(IParticleEmitter emitter, EFTrailConfig config) {
        super(emitter, config);
        if(emitter.getEffectExecutor() instanceof EFTrailExecutor patchExecutor){
            efTrailExecutor = patchExecutor;
            if(config.useEFLifetime())
                this.setLifetimeSupplier(this::getLiftTimeEF);
            this.lifeTick = efTrailExecutor.trailInfo.trailLifetime();
        }
        this.THIS = (AraTrailParticleAccessor) this;
    }

    private float getLiftTimeEF(){
        if(efTrailExecutor != null) return efTrailExecutor.trailInfo.trailLifetime() / 20.f;
        return config.time;
    }

    boolean shouldRemove = false;
    int age = 0;
    int lifeTick;
    @Override
    public void updateTick() {
        super.updateTick();

        if(efTrailExecutor != null){
            if (this.shouldRemove) {
                if (!isRemoved && this.age >= getLifeTime()) {
                    setRemoved(true);
                }
            } else if (!efTrailExecutor.canContinue()) {
                this.shouldRemove = true;
                this.lifeTick = this.age + efTrailExecutor.trailInfo.trailLifetime();
            }
            ++this.age;
        }
    }

    private float getDeltaTime() {
        return this.emitter.getDeltaTime() / 20.0F;
    }

    private boolean started(float partialTicks){
        if(efTrailExecutor != null){
            var animPlayer = efTrailExecutor.entityPatch.getAnimator()
                    .getPlayerFor(efTrailExecutor.animation);
            var tInfo = efTrailExecutor.trailInfo;

            float cet = animPlayer.getElapsedTime();
            float pet = animPlayer.getPrevElapsedTime();
            float ret = (cet - pet) * partialTicks + pet;

            if(ret <= tInfo.startTime() - 0.02f){
                return false;
            }
        }
        return true;
    }



    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        //if(!started(partialTicks)) return;

        float deltaTime = getDeltaTime();
        THIS.I_updateDynamicData(partialTicks);

        boolean removed = false;
        if(efTrailExecutor != null && !efTrailExecutor.canContinue(partialTicks)){
            removed = true;
        }

        if (deltaTime > EPSILON) {
            THIS.I_updateVelocity(deltaTime);
            if(!removed && started(partialTicks))
            {
                THIS.I_emissionStep(deltaTime);
            }
            THIS.I_snapLastPointToTransform();
            THIS.I_updatePointsLifecycle(deltaTime);
        }

        var points = THIS.A_points();
        var discontinuities = THIS.A_discontinuities();
        
        THIS.I_clearMeshData();
        if (points.size() > 1) {
            Matrix4f worldToTrail = this.getWorldToTrail();
            Vector3f localCamPosition = worldToTrail.transformPosition(camera.getPosition().toVector3f());
            discontinuities.clear();

            int start;
            for(start = 0; start < points.size(); ++start) {
                if (points.get(start).discontinuous || start == points.size() - 1) {
                    discontinuities.add(start);
                }
            }

            start = 0;

            for(int i = 0; i < discontinuities.size(); ++i) {
                THIS.I_updateSegmentMesh(start, discontinuities.getInt(i), localCamPosition, partialTicks);
                start = discontinuities.getInt(i) + 1;
            }

            this.renderMesh(buffer, camera, partialTicks);
        }
    }

    private float getFading(float pt){
        if(efTrailExecutor == null) return 1;
        float fading = 1.0F;

        if (this.shouldRemove) {
            if (TrailInfo.isValidTime(efTrailExecutor.trailInfo.fadeTime())) {
                fading = (float)(this.lifeTick - this.age) / (float)efTrailExecutor.trailInfo.trailLifetime();
            } else {
                fading = Mth.clamp(((float)(this.lifeTick - this.age)
                        + (1.0F - pt)) / (float)efTrailExecutor.trailInfo.trailLifetime(), 0.0F, 1.0F);
            }
        }

        return fading;
    }

    private void renderMesh(VertexConsumer buffer, Camera cam, float pt) {
        var tris = THIS.A_tris();
        if (!THIS.A_vertices().isEmpty() && !tris.isEmpty()) {
            Matrix4f renderMatrix = this.getWorldToTrail().invert(new Matrix4f()).translateLocal(cam.getPosition().toVector3f().negate());

            float fading = getFading(pt);

            for(int i = 0; i < tris.size(); i += 3) {
                int i0 = tris.getInt(i);
                int i1 = tris.getInt(i + 1);
                int i2 = tris.getInt(i + 2);
                this.renderVertex(buffer, renderMatrix, i0, fading);
                this.renderVertex(buffer, renderMatrix, i1, fading);
                this.renderVertex(buffer, renderMatrix, i2, fading);
            }

        }
    }

    private void renderVertex(VertexConsumer buffer, Matrix4f renderMatrix, int vertexIndex, float fading) {
        var vertices = THIS.A_vertices();
        var normals = THIS.A_normals();
        var uvs = THIS.A_uvs();
        var vertColors = THIS.A_vertColors();

        if (vertexIndex < vertices.size()) {
            Vector3f pos = new Vector3f(vertices.get(vertexIndex));
            renderMatrix.transformPosition(pos);
            Vector3f normal = vertexIndex < normals.size() ?
                    new Vector3f(normals.get(vertexIndex))
                    :
                    new Vector3f(0.0F, 1.0F, 0.0F);
            renderMatrix.transformDirection(normal);
            Vector2f uv = vertexIndex < uvs.size() ? new Vector2f(uvs.get(vertexIndex)) : new Vector2f(0.0F);
            Vector4f color = (vertexIndex < vertColors.size() ?
                    vertColors.get(vertexIndex) : new Vector4f(1.0F));

            buffer.addVertex(pos.x, pos.y, pos.z).setUv(uv.x, uv.y).setColor(color.x, color.y, color.z,
                    color.w*fading).setLight(15728880).setNormal(normal.x, normal.y, normal.z);
        }
    }

}
