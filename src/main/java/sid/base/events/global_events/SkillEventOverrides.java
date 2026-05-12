package sid.base.events.global_events;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import sid.base.main.t0001;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;


public class SkillEventOverrides {


   // @EventBusSubscriber(modid = t0001.MODID, value = Dist.CLIENT)
    public static class ClientOverrides{

      //  @SubscribeEvent
        public static void OverrideWithEventHook(FMLClientSetupEvent evt) {
            /// Disable BlockMotion if the item isn't a weapon - failure
            EpicFightClientEventHooks.Entity.MODIFY_PLAYER_LIVING_MOTION_COMPOSITE.registerEvent(
                    (event) -> {
                        ItemStack stack = event.getEntityPatch().getOriginal()
                                .getItemInHand(InteractionHand.MAIN_HAND);

                        if (event.getMotion().isSame(LivingMotions.BLOCK) &&
                                !stack.is(ItemTags.WEAPON_ENCHANTABLE)
                        ) {

                        }
                    }, -1
            );
        }


    }


}
