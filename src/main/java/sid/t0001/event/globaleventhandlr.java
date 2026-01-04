package sid.t0001.event;


import com.lowdragmc.photon.client.fx.EntityEffectExecutor;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;


import sid.t0001.main.T0001;
import yesman.epicfight.api.neoevent.playerpatch.TakeDamageEvent;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.skill.SkillEvent;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

@EventBusSubscriber(modid = T0001.MODID,value = Dist.CLIENT)
public class globaleventhandlr {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    @SkillEvent(
            caller = T0001.MODID,
            side = SkillEvent.Side.CLIENT,
            priority = -2
    )
    public static void onparryevt(TakeDamageEvent.Income event) {
        ServerPlayerPatch playerPatch = event.getPlayerPatch();

        T0001.LOGGER.info("=== TakeDamageEvent.Income fired ===");
        T0001.LOGGER.info("Result: " + event.getResult());
        T0001.LOGGER.info("Is Parried: " + event.isParried());
        T0001.LOGGER.info("Damage: " + event.getDamage());
        T0001.LOGGER.info("Damage Source: " + event.getDamageSource());

//        if (event.getResult() == AttackResult.ResultType.BLOCKED) {
//            T0001.LOGGER.info("BLOCKED detected!");
//        }
//
//            Player player = playerPatch.getOriginal();
//            Level level = player.level();
//            T0001.LOGGER.debug(String.valueOf(level));
//
//            if (level.isClientSide || Dist.CLIENT.isClient()) {
//                FX fx = FXHelper.getFX(ResourceLocation.parse("photon:cgparry"));
//                EntityEffectExecutor parry = new EntityEffectExecutor(fx,level,player, EntityEffectExecutor.AutoRotate.XROT);
//                parry.setOffset(0,0.35,0);
//                parry.setRotation(0,0,0);
//                parry.setScale(1,1,1);
//                parry.setAllowMulti(true);
//                parry.setForcedDeath(false);
//                parry.start();
//
//                T0001.LOGGER.info("Command executed!");
//            } else {
//                T0001.LOGGER.info("Can't execute command - wrong side or no server");
//            }
      }

    }





