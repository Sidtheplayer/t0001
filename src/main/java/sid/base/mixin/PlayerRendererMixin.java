package sid.base.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sid.base.client.events.CameraAnimator;
import sid.base.gameasset.ReusableEventsAndUtils;
import sid.base.main.Config;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.patched.entity.PPlayerRenderer;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.gameasset.Armatures;

@Mixin(value = PPlayerRenderer.class, remap = false)
public class PlayerRendererMixin {

    @Inject(method = "prepareModel(Lyesman/epicfight/client/mesh/HumanoidMesh;Lnet/minecraft/client/player/AbstractClientPlayer;Lyesman/epicfight/client/world/capabilites/entitypatch/player/AbstractClientPlayerPatch;Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;)V",
    at = @At("TAIL")
    )
    private static void hideHeadOnCameraClip(HumanoidMesh mesh, AbstractClientPlayer entity, AbstractClientPlayerPatch<AbstractClientPlayer> entitypatch, PlayerRenderer renderer, CallbackInfo ci){
        if(CameraAnimator.getInstance().isPlaying()){

            if (Minecraft.getInstance().cameraEntity != null && Config.camHeadClipToggle) {

                Vec3 cameraPosition = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

                Vec3 jointPos = ReusableEventsAndUtils.JointTrack.getjointpos(entity, Armatures.BIPED.get().head, Vec3f.ZERO);
                try {

                    mesh.head.setHidden(jointPos != null && cameraPosition.closerThan(
                            jointPos,
                            0.04D
                    ) && !mesh.head.isHidden());

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        }

    }
}
