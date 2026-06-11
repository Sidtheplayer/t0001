package sid.base.client.photon.gameobject.emitter.ef_trail;


import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.photon.Photon;
import com.lowdragmc.photon.client.gameobject.emitter.Emitter;
import com.lowdragmc.photon.client.gameobject.emitter.data.RendererSetting;
import com.lowdragmc.photon.client.gameobject.emitter.renderpipeline.RenderPassPipeline;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import sid.base.client.photon.fx.EFPatchExecutor;
import sid.base.client.photon.fx.EFTrailExecutor;
import sid.base.client.photon.gameobject.particle.ef_trail.EFTrailParticle;


import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;


@ParametersAreNonnullByDefault
@LDLRegisterClient(
        name = "ef_trail_emitter",
        registry = "photon:fx_object"
)
public class EFTrailEmitter extends Emitter {
    public static final IGuiTexture ICON = Icons.icon(Photon.MOD_ID, "trail");

    public static int VERSION = 2;

    @Persisted(subPersisted = true)
    public final EFTrailConfig config;

    // runtime
    protected EFTrailParticle trailParticle;

    public EFTrailEmitter() {
        this(new EFTrailConfig());
    }

    public EFTrailEmitter(EFTrailConfig config) {
        this.config = config;
    }

    @Override
    public IGuiTexture getIcon() {
        return ICON;
    }

    @Override
    public EFTrailEmitter shallowCopy() {
        return new EFTrailEmitter(config);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        var tag = super.serializeNBT(provider);
        tag.putInt("_version", VERSION);
        return tag;
    }

    @Override
    public void buildConfigurator(ConfiguratorGroup father) {
        super.buildConfigurator(father);
        config.buildConfigurator(father);
    }

    //////////////////////////////////////
    //*****     particle logic     *****//
    //////////////////////////////////////

    @Override
    public int getLifetime() {
        return config.getDuration();
    }

    @Override
    public int getDelay() {
        return super.getDelay();
    }

    @Override
    protected void updateOrigin() {
        super.updateOrigin();
        setLifetime(config.getDuration());
    }

    @Override
    public boolean isLooping() {
        if(effectExecutor instanceof EFPatchExecutor) return false;
        return config.isLooping();
    }

    @Override
    public int getParticleAmount() {
        return trailParticle.isAlive() ? 1 : 0;
    }

    @Override
    protected void update() {
        if(effectExecutor instanceof EFTrailExecutor efTrailExecutor){
            if (trailParticle.isAlive()) {
                trailParticle.updateTick();
            } else {
                remove();
            }

            var animPlayer = efTrailExecutor.entityPatch.getAnimator()
                    .getPlayerFor(efTrailExecutor.animation);
            var tInfo = efTrailExecutor.trailInfo;

            this.t = Math.clamp( (animPlayer.getElapsedTime() - tInfo.startTime())
                    / (tInfo.endTime() - tInfo.startTime())  ,0, 1);
        }
        else {
            if (trailParticle.isAlive()) {
                trailParticle.updateTick();
            } else {
                remove();
            }
            super.update();
        }
    }

    @Override
    public void reset() {
        super.reset();
        trailParticle = new EFTrailParticle(this, config);
    }

    @Override
    public boolean useTranslucentPipeline() {
        return config.renderer.getLayer() == RendererSetting.Layer.Translucent;
    }

    public void prepareRenderPass(RenderPassPipeline buffer) {
        if (isVisible()) {
            buffer.pipeQueue(trailParticle.getRenderType(), Collections.singleton(trailParticle));
        }
    }

    //////////////////////////////////////
    //********      Emitter    *********//
    //////////////////////////////////////

    @Override
    @Nullable
    public AABB getCullBox(float partialTicks) {
        return config.renderer.getCull().isEnable() ? config.renderer.getCull().getCullAABB(this, partialTicks) : null;
    }

    @Override
    public void remove(boolean force) {
        trailParticle.setRemoved(true);
        super.remove(force);
        if (force) {
            trailParticle.clear();
        }
        if(config.isDestroyRootOnDeath() && effectExecutor instanceof EFPatchExecutor efPatchExecutor){
            efPatchExecutor.destroy();
        }
    }
}
