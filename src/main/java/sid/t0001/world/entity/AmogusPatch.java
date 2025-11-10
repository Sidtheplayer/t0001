package sid.t0001.world.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import sid.t0001.client.model.t0001Armatures;
import sid.t0001.gameasset.t0001Entities;
import sid.t0001.world.ai.CustomMobCombatBehaviours;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;

public class AmogusPatch extends HumanoidMobPatch<Amogus> {
    public AmogusPatch() {
        super(Factions.WITHER);
        this.armature = t0001Armatures.AMOGUS.get();
       // do we actually need this line? :heavythonk: idk ¯\_(ツ)_/¯
    }

    // you also need to make patched renderer class,
    // along with this and register it to bus (check main class and Clientmodevents)

    public static void initAttributes(EntityAttributeModificationEvent event) {
        event.add(t0001Entities.AMOGUS.get(), EpicFightAttributes.MAX_STRIKES.get(), 4.0D);
        event.add(t0001Entities.AMOGUS.get(), EpicFightAttributes.IMPACT.get(), 2.0D);
    }

    @Override
    public HitParticleType getWeaponHitParticle(InteractionHand hand) {
        return EpicFightParticles.HIT_BLADE.get();
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
        this.original.goalSelector.addGoal(0, new AnimatedAttackGoal<>(this, CustomMobCombatBehaviours.AMOGUS_AXE_INNATE_SPAM.build(this)));
        this.original.goalSelector.addGoal(1, new TargetChasingGoal(this, this.original, 1.0D, true));
    }

    @Override
    protected void setWeaponMotions() {
        this.weaponAttackMotions = Maps.newHashMap();
        this.weaponAttackMotions.put(CapabilityItem.WeaponCategories.DAGGER, ImmutableMap.of(CapabilityItem.Styles.COMMON,CustomMobCombatBehaviours.AMOGUS_AXE_INNATE_SPAM));
    }

    @Override
    public AnimationAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
        return null;
    }

    @Override
    public SoundEvent getWeaponHitSound(InteractionHand hand) {
        return EpicFightSounds.BLADE_HIT.get();
    }

    @Override
    public SoundEvent getSwingSound(InteractionHand hand) {
        return EpicFightSounds.WHOOSH_SMALL.get();
    }
}
