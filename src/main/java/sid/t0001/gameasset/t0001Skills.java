package sid.t0001.gameasset;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.xame.t0001;
import sid.t0001.skill.dodge.AccelerateSkill;
import yesman.epicfight.api.forgeevent.SkillBuildEvent;
import yesman.epicfight.api.forgeevent.SkillBuildEvent.ModRegistryWorker;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.dodge.DodgeSkill;

@Mod.EventBusSubscriber(modid = t0001.MODID, bus=EventBusSubscriber.Bus.MOD)
public class t0001Skills {

    public static Skill ACCELERATE;

    @SubscribeEvent
    public static void buildSkillEvent(SkillBuildEvent build) {
        ModRegistryWorker modRegistry = build.createRegistryWorker("t0001");

        ACCELERATE = modRegistry.build("accelerate", AccelerateSkill::new, DodgeSkill.createDodgeBuilder().setAnimations(t0001Animations.ACCELERATE, t0001Animations.ACCELERATE_BACK));




    }
}
