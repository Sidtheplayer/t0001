package sid.base.network;

import net.minecraft.client.Minecraft;
import sid.base.client.input.t0001InputAction;
import sid.base.mixin.ControlEngineInvoker;
import sid.base.skill.t0001SkillSlots;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;


public class KeyMapHandle {

    private static final LocalPlayerPatch localPlayerPatch = EpicFightCapabilities.getLocalPlayerPatch(Minecraft.getInstance().player);
    public static void handleKeybinds()
    {
        if (localPlayerPatch != null)
        {
            castActiveSkill(t0001InputAction.Awakening, t0001SkillSlots.AWAKENING, localPlayerPatch);
        }
    }

    private static void castActiveSkill(t0001InputAction action, SkillSlot skillSlotConsumer, LocalPlayerPatch localPlayerPatch) {
        Runnable castActiveSkill = () -> {
            SkillContainer activeSlot = localPlayerPatch.getSkill(skillSlotConsumer);
            if (activeSlot.sendCastRequest(localPlayerPatch, ControlEngine.getInstance()).shouldReserveKey()) {
                if (ControlEngine.getInstance() instanceof ControlEngineInvoker invoker)
                {
                    invoker.invokeReserveKey(skillSlotConsumer, action);
                }
            }
            else
            {
                ControlEngine.getInstance().lockHotkeys();
            }
        };

        InputManager.triggerOnPress(action, castActiveSkill);
    }


}
