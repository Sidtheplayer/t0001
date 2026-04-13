package sid.base.client.events;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import sid.base.main.t0001;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;



public class CameraAnimationManager {

    public static void onClientTick(ClientTickEvent.Pre event) {
            CameraAnimator.getInstance().tick();
    }


    public static void onCameraBuild(FMLClientSetupEvent evt) {
        EpicFightClientEventHooks.Camera.BUILD_TRANSFORM_PRE.registerContextAwareEvent((event, eventContext) ->
        {  if (!CameraAnimator.getInstance().isPlaying()) {
            return;
        }

            // Apply Camera animation
            CameraAnimator.getInstance().applyToCamera(
                    event.getCamera(),
                    event.getPartialTick()
            );

            // Take full control of camera during animation
            event.setVanillaCameraSetupCanceled(true);
        });

        evt.enqueueWork(
                ()->{
                    CameraAnimator animator = CameraAnimator.getInstance();

                    animator.registerAnimation(
                            "counter",
                            ResourceLocation.fromNamespaceAndPath(t0001.MODID, "camera/oneinchcamera.json")
                    );

                    animator.registerAnimation("test",
                            ResourceLocation.fromNamespaceAndPath(t0001.MODID,"camera/testcamera.json"));
                }
        );

    }


}
