package sid.t0001.world.entity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import sid.t0001.client.model.t0001Armatures;
import sid.t0001.gameasset.t0001Entities;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;

public class AmogusPatch <T extends PathfinderMob> extends MobPatch<T> {
    public AmogusPatch() {
        super(Factions.WITHER);
        this.armature = t0001Armatures.AMOGUS.get(); // do we actually need this line? :heavythonk: idk ¯\_(ツ)_/¯
    }

    public static void initAttributes(EntityAttributeModificationEvent event) {
        event.add(t0001Entities.AMOGUS.get(), EpicFightAttributes.MAX_STRIKES.get(), 4.0D);
        event.add(t0001Entities.AMOGUS.get(), EpicFightAttributes.IMPACT.get(), 2.0D);
    }


    @Override
    public void onStartTracking(ServerPlayer trackingPlayer) {
        if (!this.getHoldingItemCapability(InteractionHand.MAIN_HAND).isEmpty()) {
            SPEntityPairingPacket packet = new SPEntityPairingPacket(this.original.getId(), EntityPairingPacketTypes.ZOMBIE_SPAWN);
            EpicFightNetworkManager.sendToPlayer(packet, trackingPlayer);
        }

        super.onStartTracking(trackingPlayer);
    }


    @Override
    public void initAnimator(Animator animator) {
        super.initAnimator(animator);
        animator.addLivingAnimation(LivingMotions.IDLE, Animations.BIPED_IDLE);
        animator.addLivingAnimation(LivingMotions.WALK, Animations.BIPED_WALK);
        animator.addLivingAnimation(LivingMotions.RUN, Animations.BIPED_RUN);
        animator.addLivingAnimation(LivingMotions.DEATH, Animations.BIPED_DEATH);
    }

    @Override
    public void updateMotion(boolean considerInaction) {
        super.commonMobUpdateMotion(considerInaction);
    }

    @Override
    protected void initAI() {
        super.initAI();
        this.original.goalSelector.addGoal(1, new TargetChasingGoal(this, this.original, 1.0D, false));
    }



    @Override
    public AnimationAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
        return null;
    }

    @Override
    public SoundEvent getWeaponHitSound(InteractionHand hand) {
        return EpicFightSounds.BLUNT_HIT.get();
    }

    @Override
    public SoundEvent getSwingSound(InteractionHand hand) {
        return EpicFightSounds.WHOOSH_SMALL.get();
    }
}
