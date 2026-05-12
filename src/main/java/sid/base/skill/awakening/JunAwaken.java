package sid.base.skill.awakening;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.photon.command.BlockEffectCommand;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import sid.base.gameasset.t0001Sounds;
import sid.base.main.t0001;
import sid.base.skill.t0001SkillDataKeys;
import sid.base.utils.ReusableAnimEvents;
import sid.base.utils.ldlib2_utils.widgetstuff.UltimateMeterWidget;
import yesman.epicfight.api.utils.LevelUtil;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;

import java.awt.*;

public class JunAwaken extends AwakeningSkill{

    public static int Meter_Capacity = 100;

    private UltimateMeterWidget ultimateMeterWidget;
    private boolean meterInitialized = false;

    public JunAwaken(SkillBuilder<?> builder) {
        super(builder);
    }

    @Override
    public boolean canExecute(SkillContainer container) {
        //TODO: ADD MORE RULES
        return container.getDataManager().getDataValue(t0001SkillDataKeys.ULTIMATE_METER) >= Meter_Capacity;
    }

    @Override
    public void executeOnServer(SkillContainer container, CompoundTag args) {
        super.executeOnServer(container, args);

        LivingEntity entity = container.getServerExecutor().getOriginal();

        Vec3 slamPos = entity.position();

        BlockPos blockPos = BlockPos.containing(slamPos.x, slamPos.y - 0.1, slamPos.z);

        if (!LevelUtil.canTransferShockWave(entity.level(), blockPos, entity.level().getBlockState(blockPos))) {
            blockPos = blockPos.below();
        }

        Vec3 fracturePos = Vec3.atCenterOf(blockPos);

        LevelUtil.circleSlamFracture(
                entity,
                entity.level(),
                fracturePos,
                3.399D,
                true,
                true
        );

        entity.level().playSound(null,
                entity.getOnPos(),
                SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.BLOCKS);

        BlockEffectCommand packet = new BlockEffectCommand();
        packet.setLocation(t0001.identifier("shockwave_fracture"));
        packet.setPos(blockPos);
        packet.setOffset(new Vec3(0D,0.2D,0D));
        packet.setRotation(Vec3.ZERO);
        packet.setScale(ReusableAnimEvents.NORMAL_SCALE);
        packet.setAllowMulti(true);
        packet.setForcedDeath(false);
        packet.setCheckState(false);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, packet);

        container.getExecutor().playAnimationSynchronized(Animations.BIPED_LANDING,0.0f);
        container.getDataManager().setDataSync(t0001SkillDataKeys.ULTIMATE_METER, 0);
    }

    @ClientOnly
    @Override
    public void executeOnClient(SkillContainer container, CompoundTag args) {
        super.executeOnClient(container, args);
        container.getExecutor().playLocalSound(t0001Sounds.SLAM_SFX);
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
                    (int) (x - 150), (int) (y + 20),
                    "Ultimate Ready"
            );
            meterInitialized = true;
        }

        int MeterValCur = container.getDataManager().getDataValue(t0001SkillDataKeys.ULTIMATE_METER);
        float progress = (float) MeterValCur / Meter_Capacity;
        int color = Mth.lerpInt(progress, Mth.color(255,0,0), Mth.color(0,250,250));

        if (ultimateMeterWidget != null) {

            guiGraphics.drawString(gui.getFont(), "Ultimate Fillup: " + MeterValCur + "%",   288,  286,
                    color);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(x + 10, y + 15, 0);
            guiGraphics.pose().popPose();
        }
    }

}
