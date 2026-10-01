package sid.base.client.events;

import net.minecraft.resources.ResourceLocation;
import sid.base.main.t0001;

import java.util.List;

public final class CameraAnimations {

    public static CameraAnimationManager.CamAnim COUNTER_OLD = new CameraAnimationManager.CamAnim(
            "counter_old",
            ResourceLocation.fromNamespaceAndPath(t0001.MODID, "camera/oneinchcamera_old.json"));

    public static CameraAnimationManager.CamAnim ITS_OVER = new CameraAnimationManager.CamAnim(
            "its_over",
            ResourceLocation.fromNamespaceAndPath(t0001.MODID, "camera/its_over_camera.json"));

    public static CameraAnimationManager.CamAnim COUNTER = new CameraAnimationManager.CamAnim(
            "counter",
            ResourceLocation.fromNamespaceAndPath(t0001.MODID, "camera/oneinchcamera.json"));

    public static CameraAnimationManager.CamAnim TEST = new CameraAnimationManager.CamAnim(
            "test",
            ResourceLocation.fromNamespaceAndPath(t0001.MODID, "camera/youfoolcountercamerax.json"));

    public static CameraAnimationManager.CamAnim TEST2 = new CameraAnimationManager.CamAnim(
            "test2",
            ResourceLocation.fromNamespaceAndPath(t0001.MODID, "camera/testcamera.json"));

    public static final List<CameraAnimationManager.CamAnim> CAM_ANIM_LIST = List.of(
            COUNTER_OLD, ITS_OVER, COUNTER, TEST, TEST2
    );

}
