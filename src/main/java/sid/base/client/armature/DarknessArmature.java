package sid.base.client.armature;

import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.model.Armature;

import java.util.Map;



public class DarknessArmature extends Armature  {
    //I was pretty high on life when I made this guy in blender
    public final Joint topring;
    public final Joint leftring;
    public final Joint outershell;

    public DarknessArmature(String name, int jointNumber, Joint rootJoint, Map<String, Joint> jointMap) {
        super(name, jointNumber, rootJoint, jointMap);

        this.topring = this.getOrLogException(jointMap, "topring");
        this.leftring = this.getOrLogException(jointMap, "leftring");
        this.outershell = this.getOrLogException(jointMap, "outershell");
    }
}
