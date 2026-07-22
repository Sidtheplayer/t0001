package sid.base.client.photon.gameobject.particle.ef_trail;

import com.lowdragmc.photon.client.gameobject.emitter.IParticleEmitter;
import com.lowdragmc.photon.client.gameobject.particle.aratrail.AraTrailParticle;
import org.joml.Vector4f;
import sid.base.client.photon.fx.EFTrailExecutor;
import sid.base.client.photon.gameobject.emitter.ef_trail.EFTrailConfig;
import sid.base.mixin.AraTrailParticleAccessor;
import yesman.epicfight.api.client.animation.property.TrailInfo;

public class EFTrailParticle extends AraTrailParticle {
    public EFTrailExecutor efTrailExecutor;
    private boolean shouldRemove = false;
    private int age = 0;
    private int lifeTick;

    // Mixin accessor for invoking private base methods
    private final AraTrailParticleAccessor THIS = (AraTrailParticleAccessor) this;

    public EFTrailParticle(IParticleEmitter emitter, EFTrailConfig config) {
        super(emitter, config);
        if (emitter.getEffectExecutor() instanceof EFTrailExecutor patchExecutor) {
            this.efTrailExecutor = patchExecutor;
            if (config.useEFLifetime()) {
                this.setLifetimeSupplier(this::getLiftTimeEF);
            }
            this.lifeTick = efTrailExecutor.trailInfo.trailLifetime();
        }

        // ---- Setup fading through colour multiplier ----
        this.setColorMultiplierSupplier(partialTicks -> {
            float fade = getFading(partialTicks);
            return new Vector4f(1.0f, 1.0f, 1.0f, fade);
        });

        // IMPORTANT: do NOT set worldPositionSupplier to far-away – it causes jumps.
    }

    private float getLiftTimeEF() {
        if (efTrailExecutor != null) return efTrailExecutor.trailInfo.trailLifetime() / 20f;
        return config.time;
    }

    private boolean started(float partialTicks) {
        if (efTrailExecutor != null) {
            var animPlayer = efTrailExecutor.entityPatch.getAnimator()
                    .getPlayerFor(efTrailExecutor.animation);
            var tInfo = efTrailExecutor.trailInfo;
            float cet = animPlayer.getElapsedTime();
            float pet = animPlayer.getPrevElapsedTime();
            float ret = (cet - pet) * partialTicks + pet;
            return ret > tInfo.startTime() - 0.02f;
        }
        return true;
    }

    /**
     * Main simulation step – conditionally emits points only when the animation has started.
     */
    @Override
    public void updateTick(float dt) {
        if (dt <= 0) return;
        if (getOnUpdate() != null) getOnUpdate().run();

        float timeStep = dt / 20f;
        THIS.I_updateDynamicData(1.0f);   // always track emitter pose

        if (!isRemoved()) {
            THIS.I_updateVelocity(timeStep);
            if (runtime.physics.isEnable()) THIS.I_physicsStep(timeStep);

            // Only emit and snap when the animation has actually begun.
            if (started(1.0f)) {
                THIS.I_emissionStep(timeStep);
                THIS.I_snapLastPointToTransform();
            }
        } else if (runtime.physics.isEnable()) {
            THIS.I_physicsStep(timeStep);
        }

        THIS.I_updatePointsLifecycle(timeStep);

        // Custom removal logic
        if (efTrailExecutor != null) {
            if (this.shouldRemove) {
                if (!isRemoved() && this.age >= getLifeTime()) {
                    setRemoved(true);
                }
            } else if (!efTrailExecutor.canContinue()) {
                this.shouldRemove = true;
                this.lifeTick = this.age + efTrailExecutor.trailInfo.trailLifetime();
            }
            ++this.age;
        }
    }

    /**
     * Compute the fading alpha based on the EF trail’s “remove” timer.
     */
    private float getFading(float partialTicks) {
        if (efTrailExecutor == null) return 1.0f;
        if (!this.shouldRemove) return 1.0f;

        float fading;
        if (TrailInfo.isValidTime(efTrailExecutor.trailInfo.fadeTime())) {
            fading = (float)(this.lifeTick - this.age) / (float)efTrailExecutor.trailInfo.trailLifetime();
        } else {
            fading = Math.clamp(((float)(this.lifeTick - this.age) + (1.0f - partialTicks))
                    / (float)efTrailExecutor.trailInfo.trailLifetime(), 0.0f, 1.0f);
        }
        return fading;
    }
}