package sid.base.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sid.base.gameasset.animations.t0001Animations;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Mixin(PlayerPatch.class)
public class PlayerPatchMixin {

    @Inject(remap = false, method = "initAnimator", at = @At("TAIL"))
    public void ts$addUnarmedBlock(Animator animator, CallbackInfo ci) {

    animator.addLivingAnimation(LivingMotions.BLOCK, t0001Animations.UNARMEDBLOCKFULL);

    }

}
