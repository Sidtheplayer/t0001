package sid.base.client.events;

import com.lowdragmc.lowdraglib2.editor.resource.FilePath;
import com.lowdragmc.lowdraglib2.editor.resource.UIResource;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.*;
import com.lowdragmc.lowdraglib2.gui.ui.data.Transform2D;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.style.PropertyRegistry;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.syncdata.ISubscription;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import sid.base.client.input.t0001KeyMappings;
import sid.base.network.RPC.RpcPacketIds;
import yesman.epicfight.api.utils.side.ClientOnly;

import javax.annotation.Nullable;
import java.util.*;

public class QuickTimeEventHandle {

    public record QuickTimeResult(boolean isQTESuccess, String QuickTimeContext) {
    }

    public record QuickTimeData(int KeyMapCode, Float Time, String QuickTimeContext) {
    }

    public static final List<QuickTimeData> CurrentQuickTimes = new ArrayList<>();

    public static final float START_SCALE = 1.6f;
    public static final float MIN_SCALE = 0.35f;
    public static final float SUCCESS_MAX_SCALE = 0.8f;

    @Nullable private static ModularUIScreen qteScreen;
    @Nullable private static ModularUI qteUI;
    @Nullable private static ISubscription scaleAnim;

    @OnlyIn(Dist.CLIENT)
    @ClientOnly
    public static ModularUI createUI() {

        var ui = Optional.ofNullable(UIResource.INSTANCE.getResourceInstance()
                        .getResource(new FilePath(ResourceLocation.parse("ldlib2:resources/global/quicktime.ui.nbt"))))
                .map(UITemplate::createUI)
                .orElseGet(UI::empty);

        //key label
        ui.selectId("keylabel").findFirst().ifPresent(el -> {
            el.addEventListener(UIEvents.TICK, e -> {
                if (!CurrentQuickTimes.isEmpty() && el instanceof Label label) {
                    int keyCode = CurrentQuickTimes.getFirst().KeyMapCode();
                    Component name = InputConstants.Type.KEYSYM.getOrCreate(keyCode).getDisplayName();
                    if (!name.equals(label.getValue())) {
                        label.setValue(name);
                    }
                }
            });
        });

        //Decoration hehe
        ui.rootElement.addEventListener(UIEvents.TICK, e -> {
            ui.selectId("start").findFirst().ifPresent(el ->
                    el.getStyle().transform2D().rotation(el.getStyle().transform2D().rotation() + 9f));
            ui.selectId("end").findFirst().ifPresent(el ->
                    el.getStyle().transform2D().rotation(el.getStyle().transform2D().rotation() - 9f));
        });


        ui.rootElement.addEventListener(UIEvents.TICK, e -> {
            ui.selectId("quicktime").findFirst().ifPresent(uiElement -> {

                boolean hasQuickTime = !CurrentQuickTimes.isEmpty();
                ui.rootElement.setVisible(hasQuickTime);

                if (!hasQuickTime) {
                    if (uiElement.isFocused()) uiElement.blur();
                    cancelScaleAnim();
                    return;
                }

                if (!uiElement.isFocused()) uiElement.focus();

                QuickTimeData q = CurrentQuickTimes.getFirst();

                if (scaleAnim == null) {
                    String context = q.QuickTimeContext();
                    float duration = Math.max(0.05f, q.Time() / 20f);
                    scaleAnim = uiElement.animation()
                            .duration(duration)
                            .style(PropertyRegistry.TRANSFORM_2D, new Transform2D().scale(MIN_SCALE))
                            .onFinished(el -> {
                                scaleAnim = null;

                                if (!CurrentQuickTimes.contains(q)) return;
                                resetElementScale(el);
                                sendResult(false, context);
                                finishAndClose();
                            })
                            .start();
                }
            });
        });


        ui.selectId("quicktime").findFirst().ifPresent(uiElement -> {
            uiElement.addEventListener(UIEvents.KEY_DOWN, uiEvent -> {
                if (CurrentQuickTimes.isEmpty()) return;
                QuickTimeData q = CurrentQuickTimes.getFirst();

                if (uiEvent.keyCode != q.KeyMapCode()) {
                    if (uiEvent.keyCode == t0001KeyMappings.DAGGER_THROW.getKey().getValue()) {
                        cancelScaleAnim();
                        resetElementScale(uiElement);
                        sendResult(false, q.QuickTimeContext());
                        finishAndClose();
                    }
                    return;
                }

                float sx = uiElement.getStyle().transform2D().scale().x;
                cancelScaleAnim();

                boolean inWindow = sx <= SUCCESS_MAX_SCALE && sx >= MIN_SCALE;
                sendResult(inWindow, q.QuickTimeContext());
                resetElementScale(uiElement);

                if (inWindow) {
                    CurrentQuickTimes.remove(q);
                    if (CurrentQuickTimes.isEmpty()) finishAndClose();
                } else {
                    finishAndClose();
                }
            });
        });

        return ModularUI.of(ui);
    }

    private static void sendResult(boolean success, String context) {
        RPCPacketDistributor.rpcToServer(RpcPacketIds.QUICK_TIME_HANDLE.id, success, context);
    }

    private static void resetElementScale(UIElement element) {
        element.getStyle().transform2D().scale().set(START_SCALE, START_SCALE);
    }

    private static void cancelScaleAnim() {
        if (scaleAnim != null) {
            scaleAnim.unsubscribe();
            scaleAnim = null;
        }
    }

    private static void finishAndClose() {
        cancelScaleAnim();
        CurrentQuickTimes.clear();
        if (qteScreen != null) {
            Minecraft mc = Minecraft.getInstance();
            mc.execute(() -> mc.setScreen(null));
            qteScreen = null;
            qteUI = null;
        }
    }

    public static void addQuickTime(QuickTimeData data) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        mc.execute(() -> {
            CurrentQuickTimes.add(data);

            if (qteScreen == null) {
                qteUI = createUI();
                qteScreen = new ModularUIScreen(qteUI, Component.empty());
                mc.setScreen(qteScreen);

                var qt = qteUI.getElementById("quicktime");
                if (qt != null) {
                    qt.setFocusable(true);
                    qt.focus();
                }
            }
        });
    }

    public static void endQuickTimes() {
        Minecraft.getInstance().execute(QuickTimeEventHandle::finishAndClose);
    }
}