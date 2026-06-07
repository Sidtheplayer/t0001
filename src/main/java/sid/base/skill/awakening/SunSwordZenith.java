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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import sid.base.client.input.t0001KeyMappings;
import sid.base.gameasset.animations.UltimateAnimations;
import sid.base.gameasset.t0001Skills;
import sid.base.skill.t0001SkillDataKeys;
import sid.base.utils.ReusableAnimEvents;
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
        return !container.getExecutor().isInAir() && container.getDataManager().getDataValue(t0001SkillDataKeys.ULTIMATE_METER) >= Meter_Capacity;
    }



    @Override
    public void executeOnServer(SkillContainer container, CompoundTag args) {
        super.executeOnServer(container, args);


        container.getExecutor().playAnimationSynchronized(UltimateAnimations.SON_SUN, 0.0f);

        container.getDataManager().setDataSync(
                t0001SkillDataKeys.ULTIMATE_METER, 0.0f
        );
        container.getDataManager().setDataSync(
                t0001SkillDataKeys.IS_AWAKENED, true
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
          //  Objects.requireNonNull(ui.selectId("background").layout(l->l.width(120).height(20).bottom(30));


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
                        s = "Press " + t0001KeyMappings.SUPER_SKILL.getTranslatedKeyMessage().getString() + " To activate";
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
                textElement.setText("Solstice Totality");
            }
        });

        return ModularUI.of(ui, player);
    }

}
