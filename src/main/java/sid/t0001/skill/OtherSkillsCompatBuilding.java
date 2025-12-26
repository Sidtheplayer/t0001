package sid.t0001.skill;


import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xame.t0001;
import sid.t0001.gameasset.animations.DragonGodSwordAnimations;
import sid.t0001.world.capabilities.t0001WeaponCategories;
import yesman.epicfight.api.forgeevent.SkillBuildEvent;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.guard.GuardSkill;

@Mod.EventBusSubscriber(modid = t0001.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class OtherSkillsCompatBuilding {
    public static void forceGuard(SkillBuildEvent bus){
    }

    @SubscribeEvent
    public static void onGuardSkillCreation(SkillBuildEvent.ModRegistryWorker.SkillCreateEvent<GuardSkill.Builder> event) {
        if(event.getRegistryName().equals(ResourceLocation.fromNamespaceAndPath("epicfight","guard"))) {
            GuardSkill.Builder builder = event.getSkillBuilder();

            builder
                    .addGuardMotion(t0001WeaponCategories.DRAGON_GOD_SWORD, (item, player) -> DragonGodSwordAnimations.GUARD_HIT)
                    .addGuardBreakMotion(t0001WeaponCategories.DRAGON_GOD_SWORD, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED);





        }
    }

    @SubscribeEvent
    public static void onParrySkillCreation(SkillBuildEvent.ModRegistryWorker.SkillCreateEvent<GuardSkill.Builder> evt) {

        if (evt.getRegistryName().equals(ResourceLocation.fromNamespaceAndPath("epicfight", "parrying"))) {
            GuardSkill.Builder builder = evt.getSkillBuilder();

            builder
                    .addGuardMotion(t0001WeaponCategories.DRAGON_GOD_SWORD, (item, player) -> DragonGodSwordAnimations.GUARD_HIT)
                    .addGuardBreakMotion(t0001WeaponCategories.DRAGON_GOD_SWORD, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED);

            //will add parry motion later
            // because there will be more than 2-3 parry motions
            // and special parry motions for projectiles

        }

    }

}
