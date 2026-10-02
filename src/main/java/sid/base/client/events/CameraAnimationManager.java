package sid.base.client.events;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import net.neoforged.neoforge.client.event.ViewportEvent;
import sid.base.main.t0001;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;

import java.util.Set;

public class CameraAnimationManager {

    public static void onClientTick(ClientTickEvent.Pre ignoredEvent) {
            CameraAnimator.getInstance().tick();
    }

    public static void ComputeFOV(ViewportEvent.ComputeFov event) {

        if (CameraAnimator.getInstance().isPlaying()) {

            CameraAnimator.CamAnim current = CameraAnimator.getInstance().getCurrentCamAnim();

            if (current != null && X.contains(current.id())) {
                event.setFOV(CameraFOVHelper.focalLengthToFOV(10.3923f));
            }

        }

    }

    private static final Set<ResourceLocation> X = Set.of(
            CameraAnimations.COUNTER.id(),
            CameraAnimations.ITS_OVER.id()
    );

    public static void onCameraBuild(FMLClientSetupEvent evt) {

        EpicFightClientEventHooks.Camera.BUILD_TRANSFORM_PRE.registerContextAwareEvent((event, eventContext) ->
                {
                    if (!CameraAnimator.getInstance().isPlaying()) return;


                    // Apply Camera animation
                    CameraAnimator.getInstance().applyToCamera(
                            event.getCamera(),
                            event.getPartialTick()
                    );

                    // Take full control of camera during animation
                    event.setVanillaCameraSetupCanceled(true);

                }

        );


        evt.enqueueWork(
                () -> {
                    CustomClientEventHooks.camera.REGISTER_CAMERA_ANIMATION.registerEvent(event -> CameraAnimations.CAM_ANIM_LIST.forEach(event::register));
                    CustomClientEventHooks.camera.REGISTER_CAMERA_ANIMATION.post(new CameraAnimationRegistryEvent());
                }
        );



    }




    public static class CameraFOVHelper {


        public enum SensorSize {

            FULL_FRAME(36.0d, 24.0d),
            APS_C(23.6d, 15.7d),
            APS_H(28.7d, 19.1d),
            MICRO_FOUR_THIRDS(17.3d, 13.0d),
            MEDIUM_FORMAT(44.0d, 33.0d);

            public final double width;
            public final double height;

            SensorSize(double width, double height) {
                this.width = width;
                this.height = height;
            }
        }

        static Minecraft mc = Minecraft.getInstance();

        private static double focalLengthToHorizontalFOV(double focalLengthMM, SensorSize sensor) {
            if (focalLengthMM <= 0) {
                t0001.LOGGER.error("focalLength is lower than or equal to 0 : {}", focalLengthMM);
                return 70.0d; // Default fallback
            }

            double fovRadians = 2.0d * Math.atan(sensor.width / (2.0d * focalLengthMM));
            return Math.toDegrees(fovRadians);
        }


        public static double horizontalToVerticalFOV(double hFovDegrees, double aspectRatio) {
            double hFovRad = Math.toRadians(hFovDegrees);
            double vFovRad = 2.0d * Math.atan(Math.tan(hFovRad / 2.0d) / aspectRatio);
            return Math.toDegrees(vFovRad);
        }


        public static double focalLengthToFOV(double focalLengthMM, SensorSize sensor) {
            double hFov = focalLengthToHorizontalFOV(focalLengthMM, sensor);
            double aspectRatio = mc.getWindow().getWidth() / (double) mc.getWindow().getHeight();
            return horizontalToVerticalFOV(hFov, aspectRatio);
        }


        public static double focalLengthToFOV(double focalLengthMM) {
            return focalLengthToFOV(focalLengthMM, SensorSize.FULL_FRAME);
        }

    }


}
