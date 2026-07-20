package sid.base.events.event_hook;

import yesman.epicfight.api.event.CancelableEventHook;
import yesman.epicfight.api.event.EventHook;


public class MyEventHooks {

    public static final class Awakening {
        public static final EventHook<AwakenBeginEvent> BEGIN = EventHook.createEventHook();
        public static final EventHook<AwakenEndEvent> END = EventHook.createEventHook();
        public static final EventHook<AwakenTickEvent> TICK = EventHook.createEventHook();

        private Awakening(){}
    }

}
