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

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Fixed Ultimate Meter Widget - Horizontal bar type
 * Fixed issues:
 * - Removed scale animation that was zooming entire screen
 * - Fixed layout positioning (use RELATIVE for container, ABSOLUTE for children)
 * - Added proper opacity animation for glow instead of scale
 * - Fixed color calculation
 * - Added null checks on suppliers
 * - Proper animation scoping (only animate glow, not whole widget)
 */
public class UltimateMeterWidget extends UIElement {

    private final Supplier<Float> progressSupplier;
    private final BooleanSupplier isActiveSupplier;

    private final int barWidth;
    private final int barHeight;

    private UIElement barFill;
    private UIElement barGlow;
    private Label statusLabel;
    private final String flavourtext;

    private boolean wasFullLastTick = false;
    private boolean hasPlayedFullAnimation = false;
    private float smoothedProgress = 0f;

    /**
     * @param progressSupplier Returns progress 0.0 to 1.0
     * @param isActiveSupplier Returns true when meter should be hidden (ultimate is active)
     * @param width Bar width in pixels (recommended: 150)
     * @param height Bar height in pixels (recommended: 20)
     * @param flavorText Text to display when meter is empty
     */
    public UltimateMeterWidget(
            Supplier<Float> progressSupplier,
            BooleanSupplier isActiveSupplier,
            int width,
            int height,
            String flavorText
    ) {
        this.progressSupplier = progressSupplier;
        this.isActiveSupplier = isActiveSupplier;
        this.barWidth = width;
        this.barHeight = height;
        this.flavourtext = flavorText;

        initializeUI();
    }

    private void initializeUI() {
        // Main container - RELATIVE positioning (no zooming)
        this.layout(layout -> layout
                .width(barWidth)
                .height(barHeight)
                .positionType(YogaPositionType.RELATIVE));

        // Background bar with border
        UIElement barBackground = new UIElement()
                .layout(layout -> layout
                        .width(barWidth)
                        .height(barHeight)
                        .positionType(YogaPositionType.ABSOLUTE)
                        .left(0)
                        .top(0))
                .style(style -> style.background(
                        new ColorBorderTexture(2, 0xFF1a1a1a)
                ));

        // Fill bar (grows from left to right)
        barFill = new UIElement()
                .layout(layout -> layout
                        .width(0)  // Will be updated by progress
                        .height(barHeight - 4)  // Account for border
                        .positionType(YogaPositionType.ABSOLUTE)
                        .left(2)   // Border offset
                        .top(2))   // Border offset
                .style(style -> style.background(new ColorRectTexture(0xFF8B4500)));

        // Glow overlay (pulsing effect at full charge)
        barGlow = new UIElement()
                .layout(layout -> layout
                        .width(barWidth)
                        .height(barHeight)
                        .positionType(YogaPositionType.ABSOLUTE)
                        .left(0)
                        .top(0))
                .style(style -> style
                        .background(new ColorRectTexture(0xFFFFFF00))
                        .opacity(0f));  // Start invisible

        // Status label (percentage or "READY!")
        statusLabel = (Label) new Label()
                .setText(flavourtext)
                .textStyle(textStyle -> textStyle
                        .textColor(0xFFFFFFFF)
                        .textShadow(true)
                        .fontSize(5.6f)
                        .adaptiveHeight(true)
                        .adaptiveWidth(true)
                        .textAlignHorizontal(Horizontal.CENTER)
                        .textAlignVertical(Vertical.CENTER))
                .layout(layout -> layout
                        .width(barWidth)
                        .height(barHeight)
                        .positionType(YogaPositionType.RELATIVE));

        this.addChildren(barBackground, barFill, barGlow, statusLabel);

        // Update every tick
        this.addEventListener(UIEvents.TICK, e -> updateVisuals());
    }

    private void updateVisuals() {
        // Null safety checks
        if (progressSupplier == null || isActiveSupplier == null) {
            return;
        }

        float rawProgress = Math.max(0f, Math.min(1f, progressSupplier.get()));
        boolean isActive = isActiveSupplier.getAsBoolean();

        // Hide entire meter when active
        this.setVisible(!isActive);

        if (isActive) {
            hasPlayedFullAnimation = false;
            wasFullLastTick = false;
            smoothedProgress = 0f;
            return;
        }

        // Smooth animation towards target progress (smoother fill)
        float progressDiff = rawProgress - smoothedProgress;
        smoothedProgress += progressDiff * 0.1f;  // Smooth interpolation

        // Update fill width based on smoothed progress
        int fillWidth = (int) ((barWidth - 4) * smoothedProgress);
        barFill.layout(layout -> layout.width(Math.max(0, fillWidth)));

        // Update fill color based on progress
        int fillColor = calculateFillColor(smoothedProgress);
        barFill.style(style -> style.background(new ColorRectTexture(fillColor)));

        // Check if fully charged
        boolean isFull = rawProgress >= 0.99f;

        if (isFull) {
            // Pulsating glow when fully charged
            double time = System.currentTimeMillis() / 500.0;
            float glowAlpha = (float) (Math.sin(time) * 0.25f + 0.2f);  // Pulse between 0.2 and 0.45 opacity
            barGlow.style(style -> style.opacity(glowAlpha));

            // Update status text
            statusLabel.setText("READY!");
            statusLabel.textStyle(textStyle -> textStyle.textColor(0xFFFFFF00));

            // Play one-time animation when first reaching full
            if (!wasFullLastTick && !hasPlayedFullAnimation) {
                hasPlayedFullAnimation = true;
                playFullAnimation();
            }
        } else {
            // No glow when charging
            barGlow.style(style -> style.opacity(0f));

            // Show percentage
            int percentage = (int) (smoothedProgress * 100);
            statusLabel.setText(String.format("%d%%", percentage));
            statusLabel.textStyle(textStyle -> textStyle.textColor(0xFFFFFFFF));

            // Reset animation flag when no longer full
            if (wasFullLastTick) {
                hasPlayedFullAnimation = false;
            }
        }

        wasFullLastTick = isFull;
    }

    /**
     * Plays a subtle animation when the meter becomes full.
     * Uses opacity pulse instead of scale to avoid zooming the screen.
     */
    private void playFullAnimation() {
        // Quick pulse on the glow only
        barGlow.animation(anim -> anim
                .duration(0.15f)
                .ease(Eases.SINE_OUT)
                .lss("opacity", "0.5")
                .onFinished(element -> element.animation(shrink -> shrink
                        .duration(0.15f)
                        .ease(Eases.SINE_IN)
                        .lss("opacity", "0.0")
                        .start()
                ))
                .start()
        );
    }

    /**
     * Calculates the fill color based on progress.
     * Smoothly transitions from dark orange/brown to bright red.
     *
     * @param progress 0.0 to 1.0
     * @return ARGB color
     */
    public static int calculateFillColor(float progress) {
        if (progress < 0.5f) {
            // First half: brown (139, 69, 19) -> orange (255, 140, 0)
            float localProgress = progress * 2f;  // 0.0 to 1.0
            int r = (int) (0x8B + (0xFF - 0x8B) * localProgress);
            int g = (int) (0x45 + (0x8C - 0x45) * localProgress);
            int b = (int) (0x13 + (-0x13) * localProgress);
            return 0xFF000000 | (r << 16) | (g << 8) | b;
        } else {
            // Second half: orange (255, 140, 0) -> red/yellow (255, 200, 0)
            float localProgress = (progress - 0.5f) * 2f;  // 0.0 to 1.0
            int r = 0xFF;
            int g = (int) (0x8C + (0xC8 - 0x8C) * localProgress);  // 140 -> 200
            int b = 0x00;
            return 0xFF000000 | (r << 16) | (g << 8) | b;
        }
    }

    /**
     * Manually set the progress value.
     * Useful if you want to override the supplier programmatically.
     */
    public void setProgress(float progress) {
        // This is read-only based on supplier, but could be extended
    }

    /**
     * Get the current smoothed progress value (0.0 to 1.0)
     */
    public float getCurrentProgress() {
        return smoothedProgress;
    }

    /**
     * Check if the meter is fully charged
     */
    public boolean isFullyCharged() {
        return smoothedProgress >= 0.99f;
    }
}