package sid.t0001.skill.weapon_passives;


import com.lowdragmc.photon.client.fx.EntityEffect;
import com.lowdragmc.photon.client.fx.FXHelper;
import com.lowdragmc.photon.client.fx.FXRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import sid.t0001.utils.JointTrackedEntityEffect;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.BasicAttackAnimation;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.UUID;

public class FreeKatanaPassive extends Skill {

    private static FXRuntime Trail_runtime = null;
    private static final UUID EVENT_UUID = UUID.fromString("ed9ea085-9021-40c4-878d-5253bcd77eef");

    public FreeKatanaPassive(SkillBuilder<? extends Skill> builder) {
        super(builder);
    }

    //trail utility passive
     /* Not gonna to lie,
     I thought I was going to spend hours on this
     when I wrote the code I theorized, was happy when it worked the first time. */
    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);

        container.getServerExecutor().getEventListener().addEventListener(PlayerEventListener.EventType.ANIMATION_BEGIN_EVENT, EVENT_UUID, (evt) -> {
            var chek = evt.getAnimation().getAccessor();

            if (chek.checkType(AttackAnimation.class) || chek.checkType(BasicAttackAnimation.class)) {
                JointTrackedEntityEffect test_trail = new JointTrackedEntityEffect(
                        FXHelper.getFX(ResourceLocation.parse("photon:firetrail")),
                        evt.getPlayerPatch().getOriginal().level(), evt.getPlayerPatch().getOriginal(),
                        Minecraft.getInstance().player,
                        Armatures.BIPED.get().toolR,
                        new Vec3f(
                                0.0, -0.24, -1.51),// 0.0, -0.24, -1.8 (reference from normal trail, will make this passive skill universal later)
                        EntityEffect.AutoRotate.NONE,
                        true); //for trails

                test_trail.setRotation(0, 0, 0);
                test_trail.setOffset(0.0, -0.24, -1.51);
                test_trail.setScale(1.5, 1.24, 2.8);
                test_trail.setAllowMulti(false);
                test_trail.setForcedDeath(true);
                test_trail.setDelay(4);

                test_trail.start();
                Trail_runtime = test_trail.getRuntime();

            }

        }, -1);

        container.getExecutor().getEventListener().addEventListener(PlayerEventListener.EventType.ANIMATION_END_EVENT, EVENT_UUID, (event) -> {
            if (Trail_runtime != null && Trail_runtime.isAlive()) {
                Trail_runtime.destroy(false);
            }

        }, -2);

    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
        var dawg = container.getExecutor().getEventListener();
        dawg.removeListener(PlayerEventListener.EventType.ANIMATION_END_EVENT, EVENT_UUID);
        dawg.removeListener(PlayerEventListener.EventType.ANIMATION_BEGIN_EVENT, EVENT_UUID);
    }


}
