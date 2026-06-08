package sid.base.events.event_hook;

import yesman.epicfight.api.event.LivingEntityPatchEvent;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class AwakenBeginEvent extends LivingEntityPatchEvent {

    public AwakenBeginEvent(LivingEntityPatch<?> entityPatch) {
        super(entityPatch);
    }

}
