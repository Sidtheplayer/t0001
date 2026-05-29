package sid.base.skill.awakening;

import com.lowdragmc.lowdraglib2.editor.resource.FilePath;
import com.lowdragmc.lowdraglib2.editor.resource.UIResource;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SupplierDataSource;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UITemplate;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ProgressBar;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.photon.command.BlockEffectCommand;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import sid.base.client.input.t0001KeyMappings;
import sid.base.gameasset.animations.t0001Animations;
import sid.base.gameasset.t0001Skills;
import sid.base.main.t0001;
import sid.base.skill.t0001SkillDataKeys;
import sid.base.utils.ReusableAnimEvents;
import yesman.epicfight.api.utils.LevelUtil;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;

import java.util.Objects;
import java.util.Optional;

public class SunSwordZenith extends AwakeningSkill{

    public static float Meter_Capacity = 100;

    public SunSwordZenith(SkillBuilder<?> builder) {
        super(builder);
    }

    @Override
    public boolean canExecute(SkillContainer container) {
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
                5.399D,
                false,
                true
        );

        entity.level().playSound(
                null,
                entity.getOnPos(),
                SoundEvents.DRAGON_FIREBALL_EXPLODE,
                SoundSource.BLOCKS
        );

        BlockEffectCommand packet = new BlockEffectCommand();
        packet.setLocation(t0001.identifier("shockwave_fracture"));
        packet.setPos(blockPos);
        packet.setOffset(new Vec3(0D, 0.2D, 0D));
        packet.setRotation(Vec3.ZERO);
        packet.setScale(ReusableAnimEvents.NORMAL_SCALE);
        packet.setAllowMulti(true);
        packet.setForcedDeath(false);
        packet.setCheckState(false);

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, packet);

        container.getExecutor().playAnimationSynchronized(t0001Animations.UNARMEDBLOCKFULL, 0.0f);

        container.getDataManager().setDataSync(
                t0001SkillDataKeys.ULTIMATE_METER, 20.0f
        );


    }


    public static ModularUI createUI(Player player) {

        var ui = Optional.ofNullable(UIResource.INSTANCE.getResourceInstance()
                        // resource location based
                        .getResource(new FilePath(ResourceLocation.parse("ldlib2:resources/global/dragon_booster.ui.nbt"))))

                // file based
                //.getResource(new FilePath(new File(LDLib2.getAssetsDir(), "ldlib2/resources/examples/example_layout.ui.nbt"))) // LDLib2.getAssetsDir() = ".minecraft/ldlib2/assets"

                .map(UITemplate::createUI)
                .orElseGet(UI::empty);

        ui.getRootElement().addEventListener(UIEvents.TICK, event -> {
            boolean hasSkill = ReusableAnimEvents.localPlayerHasSkill(t0001Skills.SOLAR_ZENITH.get());


            ui.getRootElement().setVisible(hasSkill);
            try {
                SkillContainer skillContainer = ReusableAnimEvents.getLocalSkillContainer(t0001Skills.SOLAR_ZENITH.get());
                float meterval = (Objects.requireNonNull(skillContainer).getDataManager().getDataValue(t0001SkillDataKeys.ULTIMATE_METER));


                ui.selectId("realbar").findFirst().ifPresent(element -> {
                    // Assuming element is a ProgressBar, if not check your template in editor.
                    if (element instanceof ProgressBar bar) {
                        bar.setRange(0.0f, SunSwordZenith.Meter_Capacity);
                        bar.bindDataSource(SupplierDataSource.of(() -> meterval));
                    }
                });

                ui.selectId("barlabel").findFirst().ifPresent(element -> {
                    String s;
                    if(meterval >= SunSwordZenith.Meter_Capacity){
                        s = "Press " + t0001KeyMappings.SUPER_SKILL.getKey() + " To activate";
                    } else {
                        s = meterval + "%";
                    }
                    //same logic as before
                    if(element instanceof Label label){
                        label.bindDataSource(SupplierDataSource.of(
                                ()-> Component.nullToEmpty(Optional.of(s).orElse("0.0%"))
                        ));
                    }
                });


            } catch (Exception ignored) {
            }
        });

        ui.selectId("characterawaken").findFirst().ifPresent(uiElement -> {
            if(uiElement instanceof TextElement textElement){
                textElement.setText("Zenith of Solar SwordStyle");
            }
        });

        return ModularUI.of(ui, player);
    }

}
