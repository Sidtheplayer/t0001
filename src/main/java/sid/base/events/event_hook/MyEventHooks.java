package sid.base.events.event_hook;

import yesman.epicfight.api.event.CancelableEventHook;
import yesman.epicfight.api.event.EventHook;
import yesman.epicfight.api.utils.side.LogicalSide;


public class MyEventHooks {

    public static final class Awakening {
        public static final EventHook<AwakenBeginEvent> BEGIN = EventHook.createEventHook();
        public static final EventHook<AwakenEndEvent> END = EventHook.createEventHook();
        public static final CancelableEventHook<AwakenTickEvent> TICK = CancelableEventHook.createCancelableEventHook();

        private Awakening(){}
    }

    public static final class MISC {
        public static final EventHook<QuickTimeResultEvent> QTE_RESULT = EventHook.createSidedEventHook(LogicalSide.SERVER);
    }

}
