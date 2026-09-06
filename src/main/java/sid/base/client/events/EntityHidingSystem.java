package sid.base.client.events;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EntityHidingSystem {

    private EntityHidingSystem(){
    }


    private static final Set<UUID> HIDDEN = new HashSet<>();

    //HE HID FOR 30 YEARS!
    public static void setHidden(Collection<UUID> uuids) {
        HIDDEN.clear();
        HIDDEN.addAll(uuids);
    }


    public static void clear() {
        HIDDEN.clear();
    }

    public static boolean isHidden(UUID uuid) {
        return HIDDEN.contains(uuid);
    }


}
