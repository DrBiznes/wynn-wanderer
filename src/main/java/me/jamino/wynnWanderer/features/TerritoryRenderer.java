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
                shadow);

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
                    // White subtitle color
                    TitleColor.withAlpha(TitleColor.WHITE, opacity),
                    shadow);
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
            boolean shadow) {
        TitlePosition position = TitlePosition.of(
                guiGraphics.guiWidth(), guiGraphics.guiHeight(), xOffset, yOffset, centerText, font.width(text), size);

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
