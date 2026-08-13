package sid.base.skill.weaponinnate;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.ModList;
import org.watermedia.WaterMedia;
import sid.base.gameasset.animations.t0001Animations;
import sid.base.utils.RpcPacketIds;
import yesman.epicfight.api.animation.AnimationPlayer;
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
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.StunType;


import java.util.List;
import java.util.Objects;
import java.util.function.Function;


public class t0001InnateOne extends WeaponInnateSkill {


    public static final class Builder extends WeaponInnateSkill.Builder<Builder> {
        public Builder(Function<t0001InnateOne.Builder, ? extends Skill> constructor) {
            super(constructor);
        }
    }


    public static Builder createT0001InnateBuilder() {
        return new Builder(t0001InnateOne::new)
                .setCategory(SkillCategories.WEAPON_INNATE)
                .setResource(Resource.WEAPON_CHARGE);
    }


    //NOTE: extend attack animation/specific atk anim type for attacks don't forget----
    private final AssetAccessor<? extends AttackAnimation> first;
    private final AssetAccessor<? extends AttackAnimation> second;
    private final AssetAccessor<? extends AttackAnimation> third;
    private final AssetAccessor<? extends AttackAnimation> fourth;
    private final AssetAccessor<? extends AttackAnimation> fifth;
    /// non-atk fail
    private AssetAccessor<? extends  StaticAnimation> fail;

    public t0001InnateOne(Builder builder) {
        super(builder);
        this.first = t0001Animations.TFU1;
        this.second = t0001Animations.TFU2;
        this.third = t0001Animations.TFU4_COPY;
        this.fourth = t0001Animations.TFU4;
        this.fifth = t0001Animations.TFU5_REMADE;

    }



    //HUGE thanks to Yonichi(refm) and arcane(Ascended arts)!
    // note to self - check if statements' indentations, if something doesn't work after you add another anim.

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);


        eventListener.registerEvent(
                EpicFightEventHooks.Animation.END,
                event -> {

                    this.fail = eventListener.getEntityPatch().getAnimator().getLivingAnimation(LivingMotions.IDLE,Animations.BIPED_IDLE);
                    List<LivingEntity> hurtEntities = event.getEntityPatch().getCurrentlyActuallyHitEntities();

                        if (this.first.equals(event.getAnimation()) ) {
                            if(!hurtEntities.isEmpty() && hurtEntities.getFirst().isAlive()){

                                //the "Haaaah!" sounds
                                event.getEntityPatch().playSound(SoundEvents.VILLAGER_HURT, 75, 0, 155);
                                ServerPlayer player = (ServerPlayer) event.getEntityPatch().getOriginal();
                                PlayerChatMessage chatMessage = PlayerChatMessage.unsigned(player.getUUID(), "Pathetic");
                                player.sendChatMessage(
                                        new OutgoingChatMessage.Player(chatMessage),
                                        false,//If ykyk
                                        ChatType.bind(ChatType.TEAM_MSG_COMMAND_INCOMING, player)
                                );
                                event.getEntityPatch().getCurrentlyActuallyHitEntities().clear();
                                container.getExecutor().reserveAnimation(this.second); // maybe this was the key to not fucking up
                                Objects.requireNonNull(container.getExecutor().getServerAnimator().getPlayerFor(null)).reset();
                            }
                        }

                        if (this.second.equals(event.getAnimation())) {

                            if(!hurtEntities.isEmpty() && hurtEntities.getFirst().isAlive()){
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

                        if (this.fourth.equals(event.getAnimation())) {

                            if (!hurtEntities.isEmpty() && hurtEntities.getFirst().isAlive()) {
                                event.getEntityPatch().getCurrentlyActuallyHitEntities().clear();
                                container.getExecutor().reserveAnimation(this.fifth);
                                Objects.requireNonNull(container.getExecutor().getServerAnimator().getPlayerFor(null)).reset();

                            }

                        }

                        if (this.fifth.equals(event.getAnimation())) {

                            if (!hurtEntities.isEmpty() && hurtEntities.getFirst().isAlive())
                            {
                                var opponentEntity = hurtEntities.getFirst();
                                opponentEntity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 55, 6, false, false, false));
                                opponentEntity.addTag("SetToFallBoom");
                                EpicFightCapabilities.<LivingEntity, LivingEntityPatch<LivingEntity>>getParameterizedEntityPatch(opponentEntity, LivingEntity.class, LivingEntityPatch.class).ifPresentOrElse(
                                        patch -> {
                                                patch.applyStun(StunType.HOLD,10.0F);
                                        },
                                        ()-> opponentEntity.knockback(2.0D, opponentEntity.getX() + 0.5, opponentEntity.getZ() + 0.5)
                                );
                                Objects.requireNonNull(container.getExecutor().getServerAnimator().getPlayerFor(null)).reset();


                            }
                        }

                        if(!eventListener.getEntityPatch().isLastAttackSuccess() &&
                                !this.second.equals(event.getAnimation()) &&
                                !this.third.equals(event.getAnimation()) &&
                                !this.fourth.equals(event.getAnimation()) &&
                                !this.fifth.equals(event.getAnimation())
                         && this.fail.equals(event.getAnimation())
                        ){
                            container.getExecutor().reserveAnimation(this.fail);
                        }



                }, this , 2);

        eventListener.registerContextAwareEvent(
                EpicFightEventHooks.Entity.DELIVER_DAMAGE_INCOME,
                (event,context) ->{
                    AnimationPlayer animationPlayer = event.getEntityPatch().getServerAnimator().animationPlayer;
                    var currentAnim = animationPlayer.getAnimation();
                    if(currentAnim.get().getRealAnimation().equals(this.fifth)){
                        if(event.getDamageSource().getDirectEntity() instanceof  ServerPlayer player){

                            ServerPlayer executor = container.getServerExecutor().getOriginal();

                            if (ModList.get().isLoaded(WaterMedia.ID)) {
                                RPCPacketDistributor.rpcToPlayer(player, RpcPacketIds.SEND_VIDEO.id,"t0001:hit_skullbreak_cg2.mov", player.getId() ,0.5f);
                                RPCPacketDistributor.rpcToPlayer(executor, RpcPacketIds.SEND_VIDEO.id,"t0001:hit_skullbreak_cg2.mov", executor.getId() ,0.5f);
                            }

                        }

                    }


                },this, 1


        );

    }



    @Override
    public void executeOnServer(SkillContainer container, CompoundTag arguments) {
        super.executeOnServer(container, arguments);
        container.getExecutor().playAnimationSynchronized(this.first,0.0F);
        container.getExecutor().getOriginal().addEffect(
                new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY, 198, 2, false, false, false)
        );
        container.getExecutor().getOriginal().addEffect(
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 198, 2, false, false, false)

        );


    }

}