package sid.base.client.events;

import sid.base.events.event_hook.QuickTimeResultEvent;
import yesman.epicfight.api.event.EventHook;
import yesman.epicfight.api.utils.side.LogicalSide;

public class CustomClientEventHooks {

    public static final class camera {
        public static final EventHook<CameraAnimationRegistryEvent> REGISTER_CAMERA_ANIMATION = EventHook.createEventHook();

        private camera() {}
    }

    public static final class Misc {
        public static final EventHook<QuickTimeResultEvent> QTE_CLIENT = EventHook.createSidedEventHook(LogicalSide.CLIENT);
    }


}
