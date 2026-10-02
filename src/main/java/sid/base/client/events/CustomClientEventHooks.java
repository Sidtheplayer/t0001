package sid.base.client.events;

import yesman.epicfight.api.event.EventHook;

public class CustomClientEventHooks {

    public static final class camera {
        public static final EventHook<CameraAnimationRegistryEvent> REGISTER_CAMERA_ANIMATION = EventHook.createEventHook();

        private camera() {}
    }


}
