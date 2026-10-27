package sid.base.client.camera;

import net.minecraft.resources.ResourceLocation;
import sid.base.main.t0001;

import java.util.ArrayList;
import java.util.List;



public final class CameraAnimations {

    public static final List<CameraAnimator.CamAnim> CAM_ANIM_LIST = new ArrayList<>();

    public static CameraAnimator.CamAnim register(String name, ResourceLocation path) {

        CameraAnimator.CamAnim anim = new CameraAnimator.CamAnim(
                ResourceLocation.fromNamespaceAndPath(t0001.MODID, name),
                path);

        for (CameraAnimator.CamAnim existing : CAM_ANIM_LIST) {
            if (existing.animationName().equals(anim.animationName())) {
                throw new IllegalStateException("CamAnimation named " + anim.animationName() + " already registered.");
            }
        }

        CAM_ANIM_LIST.add(anim);

        return anim;
    }

    public static final CameraAnimator.CamAnim COUNTER_OLD = register("counter_old",
            ResourceLocation.fromNamespaceAndPath(t0001.MODID, "camera/oneinchcamera_old.json"));

    public static final CameraAnimator.CamAnim ITS_OVER = register(
            "its_over",
            ResourceLocation.fromNamespaceAndPath(t0001.MODID, "camera/its_over_camera.json"));

    public static final CameraAnimator.CamAnim COUNTER = register(
            "counter",
            ResourceLocation.fromNamespaceAndPath(t0001.MODID, "camera/oneinchcamera.json")
    );

    public static final CameraAnimator.CamAnim TEST = register(
            "test",
            ResourceLocation.fromNamespaceAndPath(t0001.MODID, "camera/youfoolcountercamerax.json"));

    public static final CameraAnimator.CamAnim TEST2 = register(
            "test2",
            ResourceLocation.fromNamespaceAndPath(t0001.MODID, "camera/testcamera.json"));


}
