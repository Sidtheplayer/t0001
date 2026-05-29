package sid.base.client.photon.gameobject.emitter.ef_trail;

import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.photon.client.gameobject.emitter.aratrail.AraTrailConfig;

public class EFTrailConfig extends AraTrailConfig {

    @Configurable(
            name = "EF_TRAIL.use_EF_lifetime",
            tips = {"epicvfx.emitter.config.use_EF_lifetime"}
    )
    protected boolean use_EF_lifetime = false;

    public boolean useEFLifetime(){ return use_EF_lifetime; }

    @Configurable(
            name = "EF_TRAIL.destroy_root_on_death",
            tips = {"epicvfx.emitter.config.destroy_root_on_death"}
    )
    protected boolean destroy_root_on_death = false;

    public boolean isDestroyRootOnDeath(){ return destroy_root_on_death; }

}
