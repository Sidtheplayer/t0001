package sid.t0001.skill.weaponinnate;



import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ServerChatEvent;
import sid.t0001.gameasset.t0001Animations;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.effect.EpicFightMobEffects;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;


import java.lang.annotation.Target;
import java.util.*;
import java.util.List;

//Big thanks to Yonichi :D and Arcane

public class t0001InnateOne extends WeaponInnateSkill {
    private static final UUID EVENT_UUID = UUID.fromString("2b9a70cf-893d-47a7-9dd3-c82000b6f080");
    public final AssetAccessor<? extends AttackAnimation> first;
    public final AssetAccessor<? extends AttackAnimation> second;
    public final AssetAccessor<? extends AttackAnimation> third;
    public final AssetAccessor<? extends AttackAnimation> fourth;
    public final AssetAccessor<? extends AttackAnimation> fifth;
    public final AnimationManager.AnimationAccessor<StaticAnimation> fail;




    public t0001InnateOne(SkillBuilder<? extends WeaponInnateSkill> builder) {
        super(builder);
        this.first = t0001Animations.TFU1;
        this.second = t0001Animations.TFU2;
        this.third = t0001Animations.TFU4_COPY;
        this.fourth = t0001Animations.TFU4;
        this.fifth = t0001Animations.TFU5;
        this.fail = Animations.BIPED_IDLE;
    }

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);
        container.getExecutor().getEventListener().addEventListener(EventType.ATTACK_ANIMATION_END_EVENT, EVENT_UUID, (event) -> {
            if (t0001Animations.TFU1.equals(event.getAnimation())) {
                List<LivingEntity> hurtEntities = event.getPlayerPatch().getCurrentlyActuallyHitEntities();
                if (!hurtEntities.isEmpty() && hurtEntities.get(0).isAlive()) {
                    Objects.requireNonNull(event.getPlayerPatch().getServerAnimator().getPlayerFor(null)).reset();
                    event.getPlayerPatch().reserveAnimation(this.second);
                    ServerPlayer player = event.getPlayerPatch().getOriginal();
                    PlayerChatMessage chatMessage = PlayerChatMessage.unsigned(player.getUUID(), "Pathetic");
                    event.getPlayerPatch().getOriginal().sendChatMessage(new OutgoingChatMessage.Player(chatMessage), false, ChatType.bind(ChatType.CHAT, player));;
                    event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
                }
             else {
                Objects.requireNonNull(event.getPlayerPatch().getServerAnimator().getPlayerFor(null)).reset();
                event.getPlayerPatch().reserveAnimation(this.fail);
                event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
            }
            }
            if (t0001Animations.TFU2.equals(event.getAnimation())) {
                List<LivingEntity> hurtEntities = event.getPlayerPatch().getCurrentlyActuallyHitEntities();


                if (!hurtEntities.isEmpty() && hurtEntities.get(0).isAlive() ) {
                    Objects.requireNonNull(event.getPlayerPatch().getServerAnimator().getPlayerFor(null)).reset();
                    event.getPlayerPatch().reserveAnimation(this.third);
                    event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
                }else {
                    Objects.requireNonNull(event.getPlayerPatch().getServerAnimator().getPlayerFor(null)).reset();
                    event.getPlayerPatch().reserveAnimation(this.fail);
                    event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
                }
            }
            if (t0001Animations.TFU4_COPY.equals(event.getAnimation())){
                List<LivingEntity> hurtEntities = event.getPlayerPatch().getCurrentlyActuallyHitEntities();
                if (!hurtEntities.isEmpty() && hurtEntities.get(0).isAlive()) {
                    Objects.requireNonNull(event.getPlayerPatch().getServerAnimator().getPlayerFor(null)).reset();
                    event.getPlayerPatch().reserveAnimation(this.fourth);
                    event.getPlayerPatch().getAngleTo(hurtEntities.get(0));
                    event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
                }
                else {
                    Objects.requireNonNull(event.getPlayerPatch().getServerAnimator().getPlayerFor(null)).reset();
                    event.getPlayerPatch().reserveAnimation(this.fail);
                    event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
            }
        }
            if (t0001Animations.TFU4.equals(event.getAnimation())) {
                List<LivingEntity> hurtEntities = event.getPlayerPatch().getCurrentlyActuallyHitEntities();
                if (!hurtEntities.isEmpty() && hurtEntities.get(0).isAlive()) {
                    Objects.requireNonNull(event.getPlayerPatch().getServerAnimator().getPlayerFor(null)).reset();
                    event.getPlayerPatch().reserveAnimation(this.fifth);
                    ServerPlayer player = event.getPlayerPatch().getOriginal();
                    event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
                } else {
                    Objects.requireNonNull(event.getPlayerPatch().getServerAnimator().getPlayerFor(null)).reset();
                    event.getPlayerPatch().reserveAnimation(this.fail);
                    event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
                }
            }

        });
    }











    @Override
    public void onRemoved(SkillContainer container) {
        container.getExecutor().getEventListener().removeListener(EventType.ATTACK_ANIMATION_END_EVENT, EVENT_UUID);
    }

    @Override
    public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
        container.getExecutor().playAnimationSynchronized(this.first, 0);
        container.getExecutor().getOriginal().addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY.get(), 38, 10, true, false, false));
        }



    }




