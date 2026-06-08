package sid.base.events.event_hook;

import yesman.epicfight.api.event.LivingEntityPatchEvent;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class AwakenEndEvent extends LivingEntityPatchEvent {

    public AwakenEndEvent(LivingEntityPatch<?> entityPatch) {
        super(entityPatch);
    }


}
