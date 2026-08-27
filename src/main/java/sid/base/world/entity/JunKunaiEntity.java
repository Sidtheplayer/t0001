package sid.base.world.entity;

import com.lowdragmc.photon.client.fx.*;
import com.lowdragmc.photon.client.gameobject.emitter.particle.ParticleEmitter;
import com.lowdragmc.photon.command.BlockEffectCommand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import sid.base.gameasset.t0001Entities;
import sid.base.main.Config;
import sid.base.main.t0001;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


//IN PROGRESS: May have bugs
public class JunKunaiEntity extends AbstractArrow {

    public static Map<LivingEntity, JunKunaiEntity> KunaiMap = new ConcurrentHashMap<>();


    @OnlyIn(Dist.CLIENT)
    private FXRuntime kunai_body_fx;

    @OnlyIn(Dist.CLIENT)
    private IFXEffectExecutor kunai_body_exec;

    @OnlyIn(Dist.CLIENT)
    private FX fx;

    private static final int MaxLifetime = Config.junKunaiLifetime;

    private int rOt;


    public JunKunaiEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public JunKunaiEntity(LivingEntity shooter, Level level) {
        super(t0001Entities.JUN_KUNAI_PROJECTILE.get(), shooter, level, Items.AIR.getDefaultInstance(), null);
        this.setInvisible(true);
    }

    public boolean isGrounded() {
        return inGround;
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();

        if (this.level().isClientSide) {

            fx = FXHelper.getFX(ResourceLocation.parse("photon:jun_kunai"));

            if (fx == null) return;

            EntityEffectExecutor executor = new EntityEffectExecutor(
                    fx,
                    this.level(),
                    this,
                    EntityEffectExecutor.AutoRotate.NONE
            );
            executor.setScale(1, 1, 1);
            executor.setRotation(0, 0, 0);
            executor.setOffset(0, 0, 0);
            executor.setForcedDeath(false);
            executor.setAllowMulti(false);
            executor.setDelay(0);
            executor.start();

            kunai_body_exec = executor;

            kunai_body_fx = (executor.getRuntime());


        }

    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {


            if (fx == null) return;

            FXRuntime cachedRuntime = kunai_body_fx;


            if (cachedRuntime == null || !cachedRuntime.isValid()) {
                cachedRuntime = fx.createRuntime();
                cachedRuntime.emit(kunai_body_exec);
            } else if (!isGrounded()) {
                Quaternionf cachedRot = cachedRuntime.root.transform().localRotation();

                if(cachedRuntime.findObject("kunai") instanceof ParticleEmitter emitter){
                    float spin = (rOt * 0.1f) % ((float) (Math.PI * 2.0));
                    Quaternionf spinRot = new Quaternionf().rotateY(spin).rotateZ((float) Math.toRadians( this.getXRot()));

                    Quaternionf newRot = new Quaternionf(cachedRot).mul(spinRot);

                    emitter.transform.localRotation(newRot);

                    rOt++;
                }

            }

        }

        if (!this.level().isClientSide && this.inGround && this.tickCount >= MaxLifetime) {
            this.discard();
        }

    }

    @Override
    public void onRemovedFromLevel() {

        if (kunai_body_fx != null && kunai_body_fx.isValid()) {
            kunai_body_fx.destroy(true);
        }

        super.onRemovedFromLevel();

        try {

            LivingEntity owner = (LivingEntity) this.getOwner();

            if (owner instanceof LivingEntity living
                    && KunaiMap.get(living) == this) {

                KunaiMap.remove(living, this);
            }

        } catch (Exception e) {
            t0001.LOGGER.error("Failed to remove Kunai Entity: {}", e.getMessage());
        }

    }


    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }


    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        LivingEntity owner = (LivingEntity) this.getOwner(); //Cap entity before discard


        if (this.level().isClientSide) return;

        if (owner != null) {
            tryTeleportShooterToKunai(owner);
        }

        super.onHitEntity(result); //Call super at last owner isn't nullified

    }

    public static void tryTeleportShooterToKunai(LivingEntity entity) {

        if (!entity.level().isClientSide) {

            JunKunaiEntity junKunai = KunaiMap.get(entity);

            if (junKunai == null) {

                if (entity instanceof ServerPlayer player) {
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("§cYou don't have an active kunai!"),
                            false
                    );
                }

                return;

            }

            LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);

            if(patch == null) return;

            //Only for Humanoids
            if (!(patch.getArmature() instanceof HumanoidArmature)) return;

            try {
                entity.teleportTo(junKunai.getX(), junKunai.getY(), junKunai.getZ());

                BlockEffectCommand command = new BlockEffectCommand();

                command.setLocation(ResourceLocation.parse("photon:jun_teleport"));
                command.setPos(junKunai.getOnPos(1.0f));
                command.setOffset(new Vec3(0,1.5, 0));
                command.setAllowMulti(true);
                command.setForcedDeath(false);
                command.setCheckState(false);
                command.setRotation(Vec3.ZERO);
                command.setScale(new Vec3(1, 1, 1));

                PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, command);

                junKunai.discard();
                KunaiMap.remove(entity);

                //TODO: ADD DATA KEY TO PLAYERS HERE

            } catch (Exception e) {
                t0001.LOGGER.error("something went wrong with jun's kunai : {}", e.getMessage());
            }


        }


    }


}
