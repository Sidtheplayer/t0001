package sid.base.events.event_hook;

import sid.base.events.QuickTimeEventHandle;
import yesman.epicfight.api.event.LivingEntityPatchEvent;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class QuickTimeResultEvent extends LivingEntityPatchEvent {

    private final QuickTimeEventHandle.QuickTimeResult result;


    public QuickTimeResultEvent(LivingEntityPatch<?> entityPatch, QuickTimeEventHandle.QuickTimeResult result) {
        super(entityPatch);

        this.result = result;
    }

    public QuickTimeEventHandle.QuickTimeResult getResult() {
        return result;
    }

    public boolean isQteSuccess(){
        return result.isQTESuccess();
    }

    public String getQteContext(){
        return result.QuickTimeContext();
    }


}
