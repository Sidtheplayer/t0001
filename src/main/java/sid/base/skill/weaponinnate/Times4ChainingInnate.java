package sid.base.skill.weaponinnate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.registry.entries.EpicFightMobEffects;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;


//I know im shit at naming things.
public class Times4ChainingInnate extends Skill {

    public static final class Builder extends WeaponInnateSkill.Builder<Times4ChainingInnate.Builder> {
        private AssetAccessor<? extends AttackAnimation> first;
        private AssetAccessor<? extends AttackAnimation> second;
        private AssetAccessor<? extends AttackAnimation> third;
        private AssetAccessor<? extends AttackAnimation> fourth;
        private AssetAccessor<? extends StaticAnimation> fail;


        public Builder(Function<Times4ChainingInnate.Builder, ? extends Skill> constructor) {
            super(constructor);
        }


        public Times4ChainingInnate.Builder setAnimations(AnimationManager.AnimationAccessor<? extends AttackAnimation> first,
                                                          AnimationManager.AnimationAccessor<? extends AttackAnimation> second,
                                                          AnimationManager.AnimationAccessor<? extends AttackAnimation> third,
                                                          AnimationManager.AnimationAccessor<? extends AttackAnimation> fourth,
                                                          AnimationManager.AnimationAccessor<? extends StaticAnimation> fail
        ) {
            this.first = first;
            this.second = second;
            this.third = third;
            this.fourth = fourth;
            this.fail = fail;
            return this;
        }


    }


    public static Builder createForeFourCutsBuilder() {
        return new Builder(Times4ChainingInnate::new)
                .setCategory(SkillCategories.WEAPON_INNATE)
                .setResource(Resource.WEAPON_CHARGE);

    }


    //NOTE: extend attack animation/specific atk anim type for attacks don't forget----
    protected final AssetAccessor<? extends AttackAnimation> first;
    protected final AssetAccessor<? extends AttackAnimation> second;
    protected final AssetAccessor<? extends AttackAnimation> third;
    protected final AssetAccessor<? extends AttackAnimation> fourth;
    /// non-atk fail
    private AssetAccessor<? extends StaticAnimation> fail;

    public Times4ChainingInnate(Times4ChainingInnate.Builder builder) {
        super(builder);

        this.first = builder.first;
        this.second = builder.second;
        this.third = builder.third;
        this.fourth = builder.fourth;
        this.fail = builder.fail;

    }


    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);


        eventListener.registerEvent(
                EpicFightEventHooks.Animation.END,
                event -> {

                    this.fail = eventListener.getEntityPatch().getAnimator().getLivingAnimation(LivingMotions.IDLE, Animations.BIPED_IDLE);
                    List<LivingEntity> hurtEntities = event.getEntityPatch().getCurrentlyActuallyHitEntities();

                    if (this.first.equals(event.getAnimation())) {
                        if (!hurtEntities.isEmpty() && hurtEntities.getFirst().isAlive()) {
                            event.getEntityPatch().getCurrentlyActuallyHitEntities().clear();
                            container.getExecutor().reserveAnimation(this.second);
                            Objects.requireNonNull(container.getExecutor().getServerAnimator().getPlayerFor(null)).reset();
                        }
                    }

                    if (this.second.equals(event.getAnimation())) {

                        if (!hurtEntities.isEmpty() && hurtEntities.getFirst().isAlive()) {
                            event.getEntityPatch().getCurrentlyActuallyHitEntities().clear();
                            container.getExecutor().reserveAnimation(this.third);
                            Objects.requireNonNull(container.getExecutor().getServerAnimator().getPlayerFor(null)).reset();
                        }

                    }

                    if (this.third.equals(event.getAnimation())) {
                        // was supposed to use TFU3 but I "accidentally" broke the anim in blender
                        if (!hurtEntities.isEmpty() && hurtEntities.getFirst().isAlive()) {
                            event.getEntityPatch().getCurrentlyActuallyHitEntities().clear();
                            container.getExecutor().reserveAnimation(this.fourth);
                            Objects.requireNonNull(container.getExecutor().getServerAnimator().getPlayerFor(null)).reset();
                        }

                    }



                    if (!eventListener.getEntityPatch().isLastAttackSuccess() &&
                            !this.second.equals(event.getAnimation()) &&
                            !this.third.equals(event.getAnimation()) &&
                            !this.fourth.equals(event.getAnimation())
                            && this.fail.equals(event.getAnimation())
                    ) {
                        container.getExecutor().reserveAnimation(this.fail);
                    }


                }, this);

    }


    @Override
    public void executeOnServer(SkillContainer container, CompoundTag arguments) {
        super.executeOnServer(container, arguments);
        container.getExecutor().playAnimationSynchronized(this.first,0.0F);
        container.getExecutor().getOriginal().addEffect(
                new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY, 38, 2, true, false, false)
        );

    }



}
