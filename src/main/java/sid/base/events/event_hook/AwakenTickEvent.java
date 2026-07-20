package sid.base.events.event_hook;


import yesman.epicfight.api.event.CancelableEvent;
import yesman.epicfight.api.event.LivingEntityPatchEvent;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;


public class AwakenTickEvent extends LivingEntityPatchEvent implements CancelableEvent {


    public AwakenTickEvent(PlayerPatch<?> playerPatch) {
        super(playerPatch);
    }

    public PlayerPatch<?> getPlayerPatch() {
        return (PlayerPatch<?>)this.getEntityPatch();
    }
}
