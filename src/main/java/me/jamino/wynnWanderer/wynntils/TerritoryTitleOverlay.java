package me.jamino.wynnWanderer.wynntils;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import me.jamino.wynnWanderer.WynnWanderer;
import me.jamino.wynnWanderer.config.WynnWandererConfig;
import me.jamino.wynnWanderer.features.TerritoryTitleCore;
import me.jamino.wynnWanderer.features.TitleAnimation;
import me.jamino.wynnWanderer.features.TitleColor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Renders the territory title inside a Wynntils overlay. The position and alignment of the title
 * are managed by the Wynntils overlay manager, the text size comes from the Wynn Wanderer config.
 */
public class TerritoryTitleOverlay extends Overlay {
    // Gap between the title and the subtitle, in unscaled pixels
    private static final float SUBTITLE_GAP = 3f;

    // Shown in the Wynntils overlay manager so there is something to position
    private static final TerritoryTitleCore.DisplayedTitle PREVIEW_TITLE = new TerritoryTitleCore.DisplayedTitle(
            Component.translatable("wynn_wanderer.territory.detlas.title"),
            Component.translatable("wynn_wanderer.territory.detlas.subtitle"),
            0x669933,
            true);

    // How many times the preview was rendered, used by the smoke tests
    private int previewRenderCount = 0;

    public TerritoryTitleOverlay() {
        super(
                new OverlayPosition(
                        0,
                        0,
                        VerticalAlignment.MIDDLE,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.TOP_MIDDLE),
                new OverlaySize(300, 50));
    }

    @Override
    protected boolean isVisible() {
        TerritoryTitleCore core = WynnWanderer.getTerritoryTitleCore();
        return core != null && core.getDisplayedTitle() != null;
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Window window) {
        TerritoryTitleCore core = WynnWanderer.getTerritoryTitleCore();
        if (core == null) return;

        TerritoryTitleCore.DisplayedTitle displayedTitle = core.getDisplayedTitle();
        if (displayedTitle == null) return;

        // Check if debug screen is visible
        if (Minecraft.getInstance().debugEntries.isOverlayVisible()) return;

        int opacity = core.getAnimation().getOpacity(deltaTracker.getGameTimeDeltaPartialTick(false));
        // Don't render if almost fully transparent
        if (opacity < TitleAnimation.MIN_VISIBLE_OPACITY) return;

        renderTitle(guiGraphics, displayedTitle, opacity);
    }

    @Override
    public void renderPreview(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Window window) {
        WynnWandererConfig.TerritoryTitlesConfig.AppearanceConfig appearance =
                WynnWanderer.getConfig().territoryTitles.appearance;
        TerritoryTitleCore.DisplayedTitle preview = appearance.showSubtitles
                ? PREVIEW_TITLE
                : new TerritoryTitleCore.DisplayedTitle(
                        PREVIEW_TITLE.title(), null, PREVIEW_TITLE.color(), PREVIEW_TITLE.significant());

        renderTitle(guiGraphics, preview, 255);
        previewRenderCount++;
    }

    public int getPreviewRenderCount() {
        return previewRenderCount;
    }

    private void renderTitle(GuiGraphics guiGraphics, TerritoryTitleCore.DisplayedTitle displayedTitle, int opacity) {
        WynnWandererConfig.TerritoryTitlesConfig config = WynnWanderer.getConfig().territoryTitles;
        Font font = Minecraft.getInstance().font;

        // Apply size, with multiplier for significant territories if enabled
        float titleSize = (float) config.appearance.textSize;
        float subtitleSize = (float) config.appearance.subtitleSize;
        if (displayedTitle.significant() && config.significantTerritories.useEnhancedStyling) {
            titleSize *= (float) config.significantTerritories.titleSizeMultiplier;
            subtitleSize *= (float) config.significantTerritories.subtitleSizeMultiplier;
        }

        float titleHeight = font.lineHeight * titleSize;
        float totalHeight = titleHeight;
        if (displayedTitle.subtitle() != null) {
            totalHeight += SUBTITLE_GAP + font.lineHeight * subtitleSize;
        }

        // Place the title and subtitle as a block inside the overlay
        float y = switch (getRenderVerticalAlignment()) {
            case TOP -> getRenderY();
            case MIDDLE -> getRenderY() + (getHeight() - totalHeight) / 2f;
            case BOTTOM -> getRenderY() + getHeight() - totalHeight;
        };

        boolean shadow = config.appearance.renderShadow;
        renderLine(guiGraphics, font, displayedTitle.title(), y, titleSize, displayedTitle.color(), opacity, shadow);

        if (displayedTitle.subtitle() != null) {
            float subtitleY = y + titleHeight + SUBTITLE_GAP;
            // White subtitle color
            renderLine(
                    guiGraphics, font, displayedTitle.subtitle(), subtitleY, subtitleSize, TitleColor.WHITE, opacity, shadow);
        }
    }

    private void renderLine(
            GuiGraphics guiGraphics, Font font, Component text, float y, float size, int color, int opacity, boolean shadow) {
        float width = font.width(text) * size;
        float x = switch (getRenderHorizontalAlignment()) {
            case LEFT -> getRenderX();
            case CENTER -> getRenderX() + (getWidth() - width) / 2f;
            case RIGHT -> getRenderX() + getWidth() - width;
        };

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x, y);
        guiGraphics.pose().scale(size, size);
        guiGraphics.drawString(font, text, 0, 0, TitleColor.withAlpha(color, opacity), shadow);
        guiGraphics.pose().popMatrix();
    }
}
