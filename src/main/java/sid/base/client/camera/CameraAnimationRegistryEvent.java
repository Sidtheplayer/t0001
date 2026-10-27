package sid.base.client.camera;

import yesman.epicfight.api.event.Event;


public class CameraAnimationRegistryEvent extends Event {

   public CameraAnimationRegistryEvent register(CameraAnimator.CamAnim anim){
       CameraAnimator.getInstance().registerAnimation(anim);
       return this;
   }

}
