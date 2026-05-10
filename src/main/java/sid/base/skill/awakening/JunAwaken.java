package sid.base.skill.awakening;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import sid.base.gameasset.t0001Sounds;
import sid.base.skill.t0001SkillDataKeys;
import sid.base.utils.ldlib2_utils.widgetstuff.UltimateMeterWidget;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;

import java.awt.image.ColorConvertOp;

public class JunAwaken extends AwakeningSkill{

    public static int Meter_Capacity = 69;

    private UltimateMeterWidget ultimateMeterWidget;
    private boolean meterInitialized = false;

    public JunAwaken(SkillBuilder<?> builder) {
        super(builder);
    }

    @Override
    public boolean canExecute(SkillContainer container) {
        return container.getDataManager().getDataValue(t0001SkillDataKeys.ULTIMATE_METER) >= Meter_Capacity;
    }

    @Override
    public void executeOnServer(SkillContainer container, CompoundTag args) {
        super.executeOnServer(container, args);
        container.getExecutor().playLocalSound(t0001Sounds.SLAM_SFX);
        container.getExecutor().playAnimationSynchronized(Animations.BIPED_SLIT_THROAT,0.0f);
        container.getDataManager().setDataSync(t0001SkillDataKeys.ULTIMATE_METER, 0);
    }

    @Override
    public boolean shouldDraw(SkillContainer container) {
        return true;
    }

    @Override
    public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
        super.drawOnGui(gui, container, guiGraphics, x, y, partialTick);

        var element = new UIElement();
        element.animation().start();

        if (!meterInitialized) {
            ultimateMeterWidget = new UltimateMeterWidget(
                    () -> (float) container.getDataManager().getDataValue(t0001SkillDataKeys.ULTIMATE_METER) / Meter_Capacity,
                    () -> true,
                    (int) (x + 150), (int) (y + 20),
                    "Ultimate Ready"
            );
            meterInitialized = true;
        }

        int MeterValCur = container.getDataManager().getDataValue(t0001SkillDataKeys.ULTIMATE_METER);

        if (ultimateMeterWidget != null) {

            guiGraphics.drawString(gui.getFont(), "Meter_value: " + MeterValCur, (int) x, (int) y,
                    -16711681);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(x + 10, y + 60, 0);
            guiGraphics.pose().popPose();
        }
    }

}
