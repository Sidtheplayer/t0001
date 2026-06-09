package sid.base.skill.awakening;

import com.lowdragmc.lowdraglib2.editor.resource.FilePath;
import com.lowdragmc.lowdraglib2.editor.resource.UIResource;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SupplierDataSource;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UITemplate;
import com.lowdragmc.lowdraglib2.gui.ui.data.Translate2D;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ProgressBar;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import sid.base.client.input.t0001KeyMappings;
import sid.base.events.event_hook.MyEventHooks;
import sid.base.gameasset.animations.UltimateAnimations;
import sid.base.gameasset.t0001Skills;
import sid.base.skill.t0001SkillDataKeys;
import sid.base.utils.ReusableAnimEvents;
import sid.base.utils.RpcPacketIds;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;


import java.util.Objects;
import java.util.Optional;

public class SunSwordZenith extends AwakeningSkill{

    public static float Meter_Capacity = 100;

    public static float Reduction_co = 0.1f;


    @Override
    public void loadDatapackParameters(CompoundTag parameters) {
        super.loadDatapackParameters(parameters);
        if (parameters.contains("reduction_coefficient")) {
            Reduction_co = parameters.getFloat("reduction_coefficient");
        }
    }

    @Override
    public float reduction_coefficient() {
        return Reduction_co;
    }

    public SunSwordZenith(SkillBuilder<?> builder) {
        super(builder);
    }

    @Override
    public boolean canExecute(SkillContainer container) {
        return !container.getExecutor().isInAir() && container.getDataManager().getDataValue(t0001SkillDataKeys.ULTIMATE_METER) >= Meter_Capacity;
    }

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);

        eventListener.registerEvent(MyEventHooks.Awakening.END,(event) -> {

                        RPCPacketDistributor.rpcToAllPlayers(
                                RpcPacketIds.DESTROY_VFX_PACKET.id,
                                false,
                                "photon:sun_blade",
                                container.getExecutor().getId()
                        );

        },this);

    }

    @Override
    public void executeOnServer(SkillContainer container, CompoundTag args) {
        super.executeOnServer(container, args);

        container.getExecutor().playAnimationSynchronized(UltimateAnimations.SON_SUN, 0.1f);

        container.getDataManager().setDataSync(
                t0001SkillDataKeys.IS_AWAKENED,
                true
        );


    }

    @ClientOnly
    public static ModularUI createUI(Player player) {

        var ui = Optional.ofNullable(UIResource.INSTANCE.getResourceInstance()
                        // resource location based
                        .getResource(new FilePath(ResourceLocation.parse("ldlib2:resources/global/dragon_booster.ui.nbt"))))

                // file based
                //.getResource(new FilePath(new File(LDLib2.getAssetsDir(), "ldlib2/resources/examples/example_layout.ui.nbt"))) // LDLib2.getAssetsDir() = ".minecraft/ldlib2/assets"

                .map(UITemplate::createUI)
                .orElseGet(UI::empty);

        ui.getRootElement().addEventListener(UIEvents.TICK,event -> {
            ui.selectId("background").findFirst().ifPresent(uiElement -> {
                int gui_scale = Minecraft.getInstance().options.guiScale().get();
                  switch (gui_scale){
                      case 1 -> uiElement.getStyle().transform2D().translate(Translate2D.percent(0,825.0F));
                      case 2 -> uiElement.getStyle().transform2D().translate(Translate2D.percent(0,410.0F));
                      case 3 -> uiElement.getStyle().transform2D().translate(Translate2D.percent(0,275.0F));
                      case 4 -> uiElement.getStyle().transform2D().translate(Translate2D.percent(0,206.25F));
                  }
            });
        });

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
                        s = "Press " + t0001KeyMappings.SUPER_SKILL.getTranslatedKeyMessage().getString() + " To activate";
                    } else {
                        s = String.format("%.1f%%", meterval);
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
