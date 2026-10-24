package sid.base.events;

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
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import sid.base.client.events.CustomClientEventHooks;
import sid.base.events.event_hook.QuickTimeResultEvent;
import sid.base.main.t0001;
import sid.base.network.RPC.RpcPacketIds;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Level;

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


    private static boolean cursorHidden = false;

    @OnlyIn(Dist.CLIENT)
    @ClientOnly
    public static ModularUI createUI() {

        var ui = Optional.ofNullable(UIResource.INSTANCE.getResourceInstance()
                        .getResource(new FilePath(ResourceLocation.parse("ldlib2:resources/global/quicktime.ui.nbt"))))
                .map(UITemplate::createUI)
                .orElseGet(UI::empty);

        // key label
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

        // decoration hehe
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

        // key handling
        ui.selectId("quicktime").findFirst().ifPresent(uiElement -> {
            uiElement.addEventListener(UIEvents.KEY_DOWN, uiEvent -> {
                if (CurrentQuickTimes.isEmpty()) return;
                QuickTimeData q = CurrentQuickTimes.getFirst();

                if (uiEvent.keyCode != q.KeyMapCode()) {
                    if (QuickTimeKeys.contains(uiEvent.keyCode)) {
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

    public static final Logger logger = LogUtils.getLogger();

    private static void sendResult(boolean success, String context) {
        RPCPacketDistributor.rpcToServer(RpcPacketIds.QUICK_TIME_HANDLE.id, success, context);
        try {
            LocalPlayerPatch patch = EpicFightCapabilities.getLocalPlayerPatch(Minecraft.getInstance().player);
            QuickTimeResultEvent event = new QuickTimeResultEvent(patch ,new QuickTimeResult(success, context));
                CustomClientEventHooks.Misc.QTE_CLIENT.postWithListener(event, patch.getEventListener());
        } catch (Throwable e) {
            logger.error(e.getMessage());
        }
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

    @OnlyIn(Dist.CLIENT)
    public static void finishAndClose() {
        cancelScaleAnim();
        CurrentQuickTimes.clear();
        if (qteScreen != null) {
            Minecraft mc = Minecraft.getInstance();
            mc.execute(() -> mc.setScreen(null));
            qteScreen = null;
            qteUI = null;
        }
        restoreCursor();
    }

    @OnlyIn(Dist.CLIENT)
    public static void addQuickTime(QuickTimeData data) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        System.out.println("[QTE] addQuickTime: screen=" + qteScreen + " player=" + Minecraft.getInstance().player);

        mc.execute(() -> {
            CurrentQuickTimes.add(data);

            if (qteScreen == null) {
                qteUI = createUI();
                qteScreen = new QuickTimeScreen(qteUI);
                mc.setScreen(qteScreen);
                hideCursor();

                var qt = qteUI.getElementById("quicktime");
                if (qt != null) {
                    qt.setFocusable(true);
                    qt.focus();
                }
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public static void endQuickTimes() {
        Minecraft.getInstance().execute(QuickTimeEventHandle::finishAndClose);
    }



    @OnlyIn(Dist.CLIENT)
    private static void hideCursor() {
        if (cursorHidden) return;
        Minecraft mc = Minecraft.getInstance();
        GLFW.glfwSetInputMode(mc.getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
        cursorHidden = true;
    }

    @OnlyIn(Dist.CLIENT)
    private static void restoreCursor() {
        if (!cursorHidden) return;
        Minecraft mc = Minecraft.getInstance();
        GLFW.glfwSetInputMode(mc.getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        cursorHidden = false;
    }


    @OnlyIn(Dist.CLIENT)
    private static final class QuickTimeScreen extends ModularUIScreen {
        QuickTimeScreen(ModularUI ui) {
            super(ui, Component.empty());
        }

        @Override
        public void removed() {
            super.removed();
            restoreCursor();
        }
    }

    public static final List<Integer> QuickTimeKeys = List.of(
            GLFW.GLFW_KEY_R, GLFW.GLFW_KEY_F, GLFW.GLFW_KEY_V,
            GLFW.GLFW_KEY_C, GLFW.GLFW_KEY_X, GLFW.GLFW_KEY_Q,
            GLFW.GLFW_KEY_E, GLFW.GLFW_KEY_Z, GLFW.GLFW_KEY_SPACE
    );

    public static int getRandomKeyCode(LivingEntity entity){
        return QuickTimeEventHandle.QuickTimeKeys.get(entity.getRandom().nextInt(QuickTimeEventHandle.QuickTimeKeys.size()));
    }

}