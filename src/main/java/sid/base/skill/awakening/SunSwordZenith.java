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
import com.lowdragmc.photon.client.fx.FXRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import sid.base.client.input.t0001KeyMappings;
import sid.base.events.event_hook.MyEventHooks;
import sid.base.gameasset.animations.UltimateAnimations;
import sid.base.gameasset.t0001Skills;
import sid.base.network.PacketDelegations;
import sid.base.skill.t0001SkillDataKeys;
import sid.base.utils.HelperUtils;
import sid.base.utils.ReusableAnimEvents;
import sid.base.utils.RpcPacketIds;
import sid.base.world.item.t0001Items;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.Objects;
import java.util.Optional;

import static sid.base.utils.ReusableAnimEvents.fxRuntimeTable;
import static sid.base.utils.ReusableAnimEvents.spawnJointEffect;

public class SunSwordZenith extends AwakeningSkill{

    public static float Meter_Capacity = meter_capacity;

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

            if (!event.getEntityPatch().getLevel().isClientSide) {
                RPCPacketDistributor.rpcToAllPlayers(
                        RpcPacketIds.DESTROY_VFX_PACKET.id,
                        false,
                        "photon:sun_blade",
                        container.getExecutor().getId()
                );
            }

        },this);



    }

    @ClientOnly
    @Override
    public void onInitiateClient(SkillContainer container) {
        super.onInitiateClient(container);

        container.getExecutor().getEventListener().registerContextAwareEvent(MyEventHooks.Awakening.TICK, (event,context) -> {
            PlayerPatch<?> playerPatch = event.getPlayerPatch();

            //Manage Vfx LifeCycle
             {

                if (playerPatch.getValidItemInHand(playerPatch.getPrimaryHand()).is(t0001Items.KATANA.get())) {
                    LivingEntity entity = event.getPlayerPatch().getOriginal();
                    if (fxRuntimeTable.get(playerPatch.getId(), "photon:sun_blade") == null) {
                        spawnJointEffect("photon:sun_blade_sub", entity, Armatures.BIPED.get().toolR, true, false);
                    }
                } else {

                    FXRuntime old;

                    old = fxRuntimeTable.get(playerPatch.getId(), "photon:sun_blade");
                    if (old != null) old.destroy(true);

                    old = fxRuntimeTable.get(playerPatch.getId(), "photon:sun_blade_sub");
                    if (old != null) old.destroy(true);
                }
            }


        }, this);


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
            //React to gui_scale changes
            ui.selectId("background").findFirst().ifPresent(uiElement -> {
                int gui_scale = Minecraft.getInstance().options.guiScale().get();
                float fullscreen_cut = 20.0f;
                if(HelperUtils.is_fullscreen()){
                    switch (gui_scale){
                        case 1 -> fullscreen_cut = 50f;
                        case 2 -> fullscreen_cut = 25f;
                    }
                }
                  switch (gui_scale){
                      //Manually get values by experiment (These are Translate Values in Basic Style)
                      case 1 -> uiElement.getStyle().transform2D().translate(Translate2D.percent(0,925.0F -fullscreen_cut));
                      case 2 -> uiElement.getStyle().transform2D().translate(Translate2D.percent(0,415.0F -fullscreen_cut));
                      case 3 -> uiElement.getStyle().transform2D().translate(Translate2D.percent(0,360.0F -fullscreen_cut));
                      case 4 -> uiElement.getStyle().transform2D().translate(Translate2D.percent(0,206.25F - (fullscreen_cut * 2) ));
                  }
            });
        });

        ui.getRootElement().addEventListener(UIEvents.TICK, event -> {
            boolean hasSkill = ReusableAnimEvents.localPlayerHasSkill(t0001Skills.SOLAR_ZENITH.get());
            ui.getRootElement().setVisible(hasSkill);


            try {
                SkillContainer skillContainer = ReusableAnimEvents.getLocalSkillContainer(t0001Skills.SOLAR_ZENITH.get());
                float meterval = (Objects.requireNonNull(skillContainer).getDataManager().getDataValue(t0001SkillDataKeys.ULTIMATE_METER));

                //These realbar , background, barlabel etc. are names I manually named in the editor for ease of work
                ui.selectId("realbar").findFirst().ifPresent(element -> {
                    // Assuming element is a ProgressBar, if not check your template in editor.
                    if (element instanceof ProgressBar bar) {
                        bar.setRange(0.0f, SunSwordZenith.Meter_Capacity);
                        bar.bindDataSource(SupplierDataSource.of(() -> meterval));
                    }
                });

                ui.selectId("barlabel").findFirst().ifPresent(element -> {
                    String s;
                    //custom message if full
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

        //Put Custom Name or add Translatable key that can be data driven
        ui.selectId("characterawaken").findFirst().ifPresent(uiElement -> {
            if(uiElement instanceof TextElement textElement){
                textElement.setText("Solstice Totality");
            }
        });

        return ModularUI.of(ui, player);
    }

    @ClientOnly
    @Override
    public void onRemoveClient(SkillContainer container) {
        super.onRemoveClient(container);
        int entityId = container.getClientExecutor().getId();
        PacketDelegations.destroyFX(false,"photon:sun_blade", entityId);
        PacketDelegations.destroyFX(false,"photon:sun_blade_blade", entityId);
    }

}
