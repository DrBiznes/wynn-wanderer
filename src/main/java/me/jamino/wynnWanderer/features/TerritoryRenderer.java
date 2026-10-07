package me.jamino.wynnWanderer.features;

import me.jamino.wynnWanderer.WynnWanderer;
import me.jamino.wynnWanderer.config.WynnWandererConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class TerritoryRenderer {
    private final TerritoryTitleCore core;

    // How many frames a title was rendered in, used by the smoke tests
    private int renderCount = 0;

    public TerritoryRenderer(TerritoryTitleCore core) {
        this.core = core;
    }

    /**
     * Renders the title on screen
     *
     * @param guiGraphics The current gui graphics
     * @param deltaTracker Delta tracker for smooth animations
     */
    public void renderTitle(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        WynnWandererConfig.TerritoryTitlesConfig config = WynnWanderer.getConfig().territoryTitles;
        TerritoryTitleCore.DisplayedTitle displayedTitle = core.getDisplayedTitle();
        if (!config.enabled || displayedTitle == null) return;

        Minecraft mc = Minecraft.getInstance();
        // Check if debug screen is visible
        if (mc.debugEntries.isOverlayVisible()) return;

        int opacity = core.getAnimation().getOpacity(deltaTracker.getGameTimeDeltaPartialTick(false));
        // Don't render if almost fully transparent
        if (opacity < TitleAnimation.MIN_VISIBLE_OPACITY) return;

        WynnWandererConfig.TerritoryTitlesConfig.PositioningConfig positioning = config.positioning;
        boolean shadow = config.appearance.renderShadow;

        // Apply size, with multiplier for significant territories if enabled
        float titleSize = (float) config.appearance.textSize;
        float subtitleSize = (float) config.appearance.subtitleSize;
        if (displayedTitle.significant() && config.significantTerritories.useEnhancedStyling) {
            titleSize *= (float) config.significantTerritories.titleSizeMultiplier;
            subtitleSize *= (float) config.significantTerritories.subtitleSizeMultiplier;
        }

        renderLine(
                guiGraphics,
                mc.font,
                displayedTitle.title(),
                positioning.textXOffset,
                positioning.textYOffset,
                positioning.centerText,
                titleSize,
                TitleColor.withAlpha(displayedTitle.color(), opacity),
                shadow,
                // The shadow would show through a background that is fading
                displayedTitle.background() && shadow && opacity < 255);

        // Subtitle has its own positioning
        if (displayedTitle.subtitle() != null) {
            renderLine(
                    guiGraphics,
                    mc.font,
                    displayedTitle.subtitle(),
                    positioning.subtitleXOffset,
                    positioning.subtitleYOffset,
                    positioning.centerText,
                    subtitleSize,
                    TitleColor.withAlpha(displayedTitle.subtitleColor(), opacity),
                    shadow,
                    false);
        }

        renderCount++;
    }

    private void renderLine(
            GuiGraphics guiGraphics,
            Font font,
            Component text,
            int xOffset,
            int yOffset,
            boolean centerText,
            float size,
            int color,
            boolean shadow,
            boolean separateShadow) {
        int width = font.width(text);
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        TitlePosition position = TitlePosition.of(screenWidth, screenHeight, xOffset, yOffset, centerText, width, size);

        if (!separateShadow) {
            drawLine(guiGraphics, font, text, position, size, color, shadow);
            return;
        }

        // A translucent background lets the shadow underneath it show through, which makes the two fade
        // differently. So the shadow is only drawn where it is not covered: to the right of and below it.
        TitlePosition.BackgroundEdges edges = position.backgroundEdges(width, size);

        guiGraphics.enableScissor(0, 0, edges.right(), edges.bottom());
        drawLine(guiGraphics, font, text, position, size, color, false);
        guiGraphics.disableScissor();

        guiGraphics.enableScissor(edges.right(), 0, screenWidth, screenHeight);
        drawLine(guiGraphics, font, text, position, size, color, true);
        guiGraphics.disableScissor();

        guiGraphics.enableScissor(0, edges.bottom(), edges.right(), screenHeight);
        drawLine(guiGraphics, font, text, position, size, color, true);
        guiGraphics.disableScissor();
    }

    private void drawLine(
            GuiGraphics guiGraphics,
            Font font,
            Component text,
            TitlePosition position,
            float size,
            int color,
            boolean shadow) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(position.x(), position.y());
        guiGraphics.pose().scale(size, size);
        guiGraphics.drawString(font, text, 0, 0, color, shadow);
        guiGraphics.pose().popMatrix();
    }

    public int getRenderCount() {
        return renderCount;
    }
}
