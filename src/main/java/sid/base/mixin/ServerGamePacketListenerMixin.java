package sid.base.mixin;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sid.base.gameasset.animations.types.TitleCardAttackAnimation;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerMixin {

    @Shadow
    public ServerPlayer player;
    //Solves player snapping back in the "IT's OVER" animation, I was at my wit's end and been forced to have a clanker(i know its bad) assist me a little
    @Inject(method = "handleMovePlayer", at = @At("HEAD"), cancellable = true)
    private void skipMoveValidationDuringExecution(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        ServerPlayer player = this.player;
        LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(player, LivingEntityPatch.class);

        if (patch != null) {
            AnimationPlayer animationPlayer = patch.getAnimator().getPlayerFor(null);

            if (animationPlayer != null && animationPlayer.getAnimation().checkType(TitleCardAttackAnimation.class)) {
                player.absMoveTo(packet.getX(player.getX()), packet.getY(player.getY()), packet.getZ(player.getZ()),
                        packet.getYRot(player.getYRot()), packet.getXRot(player.getXRot()));
                ci.cancel();
            }
        }

    }


}
