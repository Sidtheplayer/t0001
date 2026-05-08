package sid.base.client.events;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import sid.base.main.t0001;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;

import java.util.Objects;

public class CameraAnimationManager {

    public static void onClientTick(ClientTickEvent.Pre ignoredEvent) {
            CameraAnimator.getInstance().tick();
    }

    public static void COmputeFOV(ViewportEvent.ComputeFov event){
        if(CameraAnimator.getInstance().isPlaying() && Objects.equals(CameraAnimator.getInstance().getCurrentAnimationName(), "counter")){
            event.setFOV(98D);
        }
    }

    public static void onMouseTurn(InputEvent.MouseScrollingEvent event) {
        if (CameraAnimator.getInstance().isPlaying() && CameraAnimator.getInstance().isLockMousePanning()) {
            event.setCanceled(true);
        }
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
                            ResourceLocation.fromNamespaceAndPath(t0001.MODID,"camera/youfoolcountercamerax.json"));

                    animator.registerAnimation("test2",
                            ResourceLocation.fromNamespaceAndPath(t0001.MODID,"camera/testcamera.json"));
                }
        );

    }


}
