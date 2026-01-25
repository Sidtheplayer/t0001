package sid.base.utils;

import com.lowdragmc.photon.client.fx.EntityEffectExecutor;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXHelper;
import com.lowdragmc.photon.client.fx.FXRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import static sid.base.gameasset.animations.UltimateAnimations.fxRuntimeHashBiMap;
/// Client only use cases
public abstract class ReusableAnimEvents {



    public static final AnimationEvent.E0 SEND_BYPASSED_CHAT_MESSAGE = ((entitypatch, animation, params) -> {
        MinecraftServer server = entitypatch.getLevel().getServer();
        if (server != null) {
            for (Player player : entitypatch.getLevel().getNearbyPlayers(TargetingConditions.DEFAULT, entitypatch.getOriginal(), AABB.ofSize(
                            Vec3.fromRGB24(20), 10, 20, 10))) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(entitypatch.getOriginal().getScoreboardName() + ": " + params.first()));
            }
        }
    });

    @ClientOnly
    @OnlyIn(Dist.CLIENT)
    public static void SpawnRootJointTrackFX(LivingEntityPatch<?> e, @SuppressWarnings("SameParameterValue") String FxResourceLocationString, @SuppressWarnings("SameParameterValue") boolean setmulti) {
        FX menacing = FXHelper.getFX(ResourceLocation.parse(FxResourceLocationString));
        Entity eo = e.getOriginal();
        Level l = eo.level().isClientSide ? eo.level() : null;
        if (l != null) {
            Armature ea = e.getArmature();
            JointTrackedEntityEffect jtef = new JointTrackedEntityEffect(menacing, l, eo, ea.rootJoint, Vec3f.ZERO, EntityEffectExecutor.AutoRotate.NONE, false);
            jtef.setOffset(0, 0, 0);
            jtef.setRotation(0, 0, 0);
            jtef.setScale(1, 1, 1);
            jtef.setAllowMulti(setmulti);
            jtef.setForcedDeath(true);
            jtef.setDelay(0);
            jtef.start();
            FXRuntime runtime = jtef.getRuntime();
            fxRuntimeHashBiMap.put(e.getId(),runtime); //Create BiHashMap to map entityIds, and runtimes to destroy or manage outside the origin
        }

    }

}
