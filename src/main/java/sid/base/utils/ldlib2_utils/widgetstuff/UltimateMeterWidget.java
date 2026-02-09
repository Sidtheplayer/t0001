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
 * Util Ultimate Meter Widget - Horizontal bar type
 * FIXME
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

    /**
     * @param progressSupplier Returns progress 0.0 to 1.0
     * @param isActiveSupplier Returns true when meter should be hidden (ultimate is active)
     * @param width Bar width in pixels (recommended: 150)
     * @param height Bar height in pixels (recommended: 20)
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
        // Container - RELATIVE positioning is key
        this.layout(layout -> layout
                .width(barWidth)
                .height(barHeight)
                .positionType(YogaPositionType.RELATIVE));

        // ...existing code...
        UIElement barBackground = new UIElement()
                .layout(layout -> layout
                        .width(barWidth)
                        .height(barHeight)
                        .positionType(YogaPositionType.ABSOLUTE)
                        .left(0)
                        .top(0))
                .style(style -> style.background(
                        new ColorBorderTexture(2, 0xFF000000).setBorder(0xFF1a1a1a)
                ));

        // Fill bar
        barFill = new UIElement()
                .layout(layout -> layout
                        .width(0)
                        .height(barHeight - 2)
                        .positionType(YogaPositionType.ABSOLUTE)
                        .left(2)
                        .top(2))
                .style(style -> style.background(new ColorRectTexture(0xFF8B4500)));

        // Glow overlay
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

        // Status label
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
                        .positionType(YogaPositionType.RELATIVE)
                        .left(0)
                        .top(0));

        this.addChildren(barBackground, barFill, barGlow, statusLabel);

        // Update every tick
        this.addEventListener(UIEvents.TICK, e -> updateVisuals());
    }

    private void updateVisuals() {
        float progress = Math.max(0f, Math.min(1f, progressSupplier.get()));
        boolean isActive = isActiveSupplier.getAsBoolean();

        this.setVisible(!isActive);

        if (isActive) {
            hasPlayedFullAnimation = false;
            wasFullLastTick = false;
            return;
        }

        // Update fill width
        int fillWidth = (int) ((barWidth - 4) * progress);
        barFill.layout(layout -> layout.width(fillWidth));

        // Update color
        int fillColor = calculateFillColor(progress);
        barFill.style(style -> style.background(new ColorRectTexture(fillColor)));

        // Update percentage
        int percentage = (int) (progress * 100);

        boolean isFull = progress >= 1.0f;
        if (isFull) {
            // Pulsating glow
            double time = System.currentTimeMillis() / 500.0;
            float glowAlpha = (float) (Math.sin(time) * 0.3 + 0.5);

            barGlow.style(style -> style.opacity(glowAlpha));

            statusLabel.setText("READY!");
            statusLabel.textStyle(textStyle -> textStyle.textColor(0xFFFFFF00));

            if (!wasFullLastTick && !hasPlayedFullAnimation) {
                hasPlayedFullAnimation = true;
                playFullAnimation();
            }
        } else {
            barGlow.style(style -> style.opacity(0f));
            statusLabel.setText(String.format("%d%%", percentage));
            statusLabel.textStyle(textStyle -> textStyle.textColor(0xFFFFFFFF));

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
                .onFinished(element -> element.animation(shrink -> shrink
                        .duration(0.2f)
                        .ease(Eases.SINE_IN)
                        .lss("transform-2d", "scale(1.0, 1.0)")
                        .start()
                ))
                .start()
        );
    }

    private int calculateFillColor(float progress) {
        if (progress < 0.5f) {
            float localProgress = progress * 2f;
            int r = (int) (0x8B + (0xFF - 0x8B) * localProgress);
            int g = (int) (0x45 + (0x8C - 0x45) * localProgress);
            return 0xFF000000 | (r << 16) | (g << 8);
        } else {
            float localProgress = (progress - 0.5f) * 2f;
            int r = 0xFF;
            int g = (int) (0x8C - (0x8C * localProgress));
            return 0xFF000000 | (r << 16) | (g << 8);
        }
    }
}