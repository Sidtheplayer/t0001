package sid.base.utils.ldlib2_utils.widgetstuff;

import com.lowdragmc.lowdraglib2.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;

import com.lowdragmc.lowdraglib2.math.interpolate.Eases;
import org.appliedenergistics.yoga.YogaPositionType;
import sid.base.client.input.t0001KeyMappings;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Ultimate Meter Widget
 */
public class UltimateMeterWidget extends UIElement {

    private final Supplier<Float> progressSupplier; // 0.0 to 1.0
    private final BooleanSupplier isUltimateActiveSupplier;
    private final Consumer<UltimateMeterWidget> onTriggerCallback;

    private final int barWidth;
    private final int barHeight;

    private UIElement barFill;
    private UIElement barGlow;
    private Label statusLabel;

    private boolean wasFullLastTick = false;
    private boolean hasPlayedFullAnimation = false;

    /**
     * Create ultimate meter widget
     *
     * @param progressSupplier Returns current progress (0.0 to 1.0)
     * @param isUltimateActiveSupplier Returns true if ultimate is currently active
     * @param onTriggerCallback Called when player clicks the bar while it's full
     * @param width Width of the bar in pixels (recommended: 100-200)
     * @param height Height of the bar in pixels (recommended: 16-24)
     *
     *               damn  this time i totally didn't suck naming variables and objects
     */
    public UltimateMeterWidget(
            Supplier<Float> progressSupplier,
            BooleanSupplier isUltimateActiveSupplier,
            Consumer<UltimateMeterWidget> onTriggerCallback,
            int width,
            int height
    ) {
        this.progressSupplier = progressSupplier;
        this.isUltimateActiveSupplier = isUltimateActiveSupplier;
        this.onTriggerCallback = onTriggerCallback;
        this.barWidth = width;
        this.barHeight = height;

        initializeUI();
    }

    private void initializeUI() {
        // Container layout
        this.layout(layout -> layout
                .width(barWidth)
                .height(barHeight)
                .positionType(YogaPositionType.RELATIVE));

        // Background (dark border with fill)
        UIElement barBackground = new UIElement()
                .layout(layout -> layout
                        .width(barWidth)
                        .height(barHeight)
                        .positionType(YogaPositionType.ABSOLUTE)
                        .left(0)
                        .top(0))
                .style(style -> style.background(
                        new ColorBorderTexture(2, 0xFF000000)
                                .setBorder(0xFF1a1a1a)
                ));

        // Fill bar (grows as meter fills)
        barFill = new UIElement()
                .layout(layout -> layout
                        .width(0) // Will be updated dynamically
                        .height(barHeight - 4)
                        .positionType(YogaPositionType.ABSOLUTE)
                        .left(2)
                        .top(2))
                .style(style -> style.background(new ColorRectTexture(0xFF8B4500)));

        // Glow overlay (only visible when full)
        barGlow = new UIElement()
                .layout(layout -> layout
                        .width(barWidth)
                        .height(barHeight)
                        .positionType(YogaPositionType.ABSOLUTE)
                        .left(0)
                        .top(0))
                .style(style -> style
                        .background(new ColorRectTexture(0xFFFFFF00))
                        .opacity(0f));


        statusLabel = (Label) new Label()
                .setText("ULTIMATE")
                //kinda cringe ik but its placeholder,
                // sid why the fuck do you keep painstakingly explain shit for every class like this, you know nobody will come into this hell hole of a code you wrote
                .textStyle(textStyle -> textStyle
                        .textColor(0xFFFFFFFF)
                        .textShadow(true)
                        .fontSize(10)
                        .textAlignHorizontal(Horizontal.CENTER)
                        .textAlignVertical(Vertical.CENTER))
                .layout(layout -> layout
                        .width(barWidth)
                        .height(barHeight)
                        .positionType(YogaPositionType.ABSOLUTE)
                        .left(0)
                        .top(0));

        this.addChildren(barBackground, barFill, barGlow, statusLabel);

        // Update every tick
        this.addEventListener(UIEvents.TICK, e -> updateVisuals());
    }

    private void updateVisuals() {
        float progress = Math.max(0f, Math.min(1f, progressSupplier.get()));
        boolean isUltimateActive = isUltimateActiveSupplier.getAsBoolean();

        // Hide widget if ultimate active
        this.setVisible(!isUltimateActive);

        if (isUltimateActive) {
            // Reset flag if ultimate becomes inactive
            hasPlayedFullAnimation = false;
            wasFullLastTick = false;
            return;
        }

        // Update fill width
        int fillWidth = (int) ((barWidth - 4) * progress);
        barFill.layout(layout -> layout.width(fillWidth));

        // Update fill color based on progress bar
        int fillColor = calculateFillColor(progress);
        barFill.style(style -> style.background(new ColorRectTexture(fillColor)));

        // Update percentage
        int percentage = (int) (progress * 100);

        // Glow
        boolean isFull = progress >= 1.0f;
        if (isFull) {
            // pulsar glow
            double time = System.currentTimeMillis() / 500.0;
            float glowAlpha = (float) (Math.sin(time) * 0.3 + 0.5); // 0.2 to 0.8

            barGlow.style(style -> style.opacity(glowAlpha));

            // Change label, make it yellow
            statusLabel.setText("READY!");
            statusLabel.textStyle(textStyle -> textStyle.textColor(0xFFFFFF00));

            // Trigger animation only once when becoming full
            if (!wasFullLastTick && !hasPlayedFullAnimation) {
                hasPlayedFullAnimation = true;
                playFullAnimation();
            }
        } else {
            barGlow.style(style -> style.opacity(0f));
            statusLabel.setText(String.format("%d%%", percentage));
            statusLabel.textStyle(textStyle -> textStyle.textColor(0xFFFFFFFF));

            // Reset animation flag when not full
            if (wasFullLastTick) {
                hasPlayedFullAnimation = false;
            }
        }

        wasFullLastTick = isFull;
    }

    private void playFullAnimation() {
        this.animation(anim -> anim
                .duration(0.3f)
                .ease(Eases.ELASTIC_OUT)
                .lss("transform-2d", "scale(1.1, 1.1)")
                .onFinished(element -> {
                    element.animation(shrink -> shrink
                            .duration(0.2f)
                            .ease(Eases.SINE_IN)
                            .lss("transform-2d", "scale(1.0, 1.0)")
                            .start()
                    );
                })
                .start()
        );
    }


    //Calculate fill color
    private int calculateFillColor(float progress) {
        if (progress < 0.5f) {
            float localProgress = progress * 2f;
            int r1 = 0x8B, g1 = 0x45, b1 = 0x00;
            int r2 = 0xFF, g2 = 0x8C, b2 = 0x00;

            int r = (int) (r1 + (r2 - r1) * localProgress);
            int g = (int) (g1 + (g2 - g1) * localProgress);
            int b = (int) (b1 + (0) * localProgress);

            return 0xFF000000 | (r << 16) | (g << 8) | b;
        } else {
            float localProgress = (progress - 0.5f) * 2f;
            int r1 = 0xFF, g1 = 0x8C, b1 = 0x00;
            int r2 = 0xFF, g2 = 0x00, b2 = 0x00;

            int r = (int) (r1 + (r2 - r1) * localProgress);
            int g = (int) (g1 + (g2 - g1) * localProgress);
            int b = (int) (b1 + (b2 - b1) * localProgress);

            return 0xFF000000 | (r << 16) | (g << 8) | b;
        }
    }
}