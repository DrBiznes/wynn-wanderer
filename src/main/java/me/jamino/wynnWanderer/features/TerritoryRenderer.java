package me.jamino.wynnWanderer.features;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class TerritoryRenderer {
    // Title renderer state
    public Text displayedTitle = null;
    public Text displayedSubTitle = null;
    public int titleTimer = 0;
    public int cooldownTimer = 0;
    public int titleTextColor = 0xFFFFFF;

    // Configuration properties (public so TerritoryTitleCore can access them directly)
    public boolean enabled = true;
    public int textFadeInTime = 10;
    public int textDisplayTime = 50;
    public int textFadeOutTime = 10;
    public int textCooldownTime = 80;
    public String textColor = "ffffff";
    public double textSize = 2.1;
    public boolean renderShadow = true;
    public int textYOffset = -40; // Updated default to move title up
    public int textXOffset = 0;
    public boolean centerText = true;

    // Subtitle positioning properties
    public int subtitleYOffset = -20;
    public int subtitleXOffset = 0;
    public double subtitleSize = 1.3;
    public boolean showSubtitles = true; // New property for subtitle toggle

    // Significant territory settings
    public boolean useEnhancedStyling = true;
    public double titleSizeMultiplier = 1.2;
    public double subtitleSizeMultiplier = 1.1;
    public boolean useCustomColors = true;
    public String defaultSignificantColor = "ffcc00";

    // Current rendering mode flag
    private boolean isRenderingSignificantTerritory = false;

    /**
     * Sets the color of text to render
     *
     * @param textColor Hexadecimal string representation of the color
     */
    public void setColor(String textColor) {
        try {
            // Ensure the color string is treated as hexadecimal
            this.titleTextColor = Integer.parseInt(textColor.replace("#", ""), 16);
        } catch (Exception e) {
            System.err.println("Text color '" + textColor + "' is not a valid hex color (e.g., 'ffffff'). Defaulting to white...");
            this.titleTextColor = 0xFFFFFF; // Default to white
        }
    }

    /**
     * Sets whether the current rendering is for a significant territory
     */
    public void setSignificantTerritoryMode(boolean isSignificant) {
        this.isRenderingSignificantTerritory = isSignificant;
    }

    /**
     * Renders the title on screen
     *
     * @param drawContext The current draw context
     * @param partialTicks Partial tick time for smooth animations
     */
    public void renderTitle(DrawContext drawContext, float partialTicks) {
        if (!enabled || displayedTitle == null || titleTimer <= 0) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        // Check if debug screen is visible
        if (mc.getDebugHud().shouldShowDebugHud()) return;

        // Calculate fade opacity
        float age = (float) titleTimer - partialTicks; // Calculate remaining time precisely
        int opacity = 255; // Default to fully opaque (during display phase)

        // Fade-in phase
        if (age > textFadeOutTime + textDisplayTime) {
            // Calculate progress through fade-in phase
            float fadeInProgress = (float)textFadeInTime - (age - (textDisplayTime + textFadeOutTime));
            if (textFadeInTime > 0) { // Avoid division by zero
                opacity = (int) (MathHelper.clamp(fadeInProgress, 0, textFadeInTime) * 255.0F / (float) textFadeInTime);
            } else {
                opacity = 255; // Instant fade-in if time is 0
            }
        }
        // Fade-out phase
        else if (age <= textFadeOutTime) {
            // Calculate progress through fade-out phase (age is time remaining in fade-out)
            if (textFadeOutTime > 0) { // Avoid division by zero
                opacity = (int) (MathHelper.clamp(age, 0, textFadeOutTime) * 255.0F / (float) textFadeOutTime);
            } else {
                opacity = 0; // Instant fade-out if time is 0
            }
        }
        // Display phase: opacity remains 255

        opacity = MathHelper.clamp(opacity, 0, 255);
        // Don't render if almost fully transparent
        if (opacity < 8) return;

        // Set up rendering state
        drawContext.getMatrices().push();

        // Common alpha value for both title and subtitle
        int alpha = opacity << 24; // Apply alpha

        TextRenderer fontRenderer = mc.textRenderer;

        // --- Title Rendering ---
        drawContext.getMatrices().push();

        // Center the title according to settings
        if (centerText) {
            // Move to center of screen, then apply offsets
            drawContext.getMatrices().translate(
                    mc.getWindow().getScaledWidth() / 2.0 + textXOffset,
                    mc.getWindow().getScaledHeight() / 2.0 + textYOffset,
                    0);
        } else {
            // Use direct offsets
            drawContext.getMatrices().translate(textXOffset, textYOffset, 0);
        }

        // Apply size, with multiplier for significant territories if enabled
        float actualTitleSize = (float)textSize;
        if (isRenderingSignificantTerritory && useEnhancedStyling) {
            actualTitleSize *= (float)titleSizeMultiplier;
        }

        drawContext.getMatrices().scale(actualTitleSize, actualTitleSize, actualTitleSize);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int titleWidth = fontRenderer.getWidth(displayedTitle);

        // Calculate x position based on centering
        int titleXPos = centerText ? -(titleWidth / 2) : 0;

        // Draw title
        drawContext.drawText(
                fontRenderer,
                displayedTitle,
                titleXPos,
                0,
                titleTextColor | alpha,
                renderShadow);

        RenderSystem.disableBlend();
        drawContext.getMatrices().pop(); // Pop title transform

        // --- Subtitle Rendering (if exists and enabled) ---
        if (showSubtitles && displayedSubTitle != null) {
            drawContext.getMatrices().push();

            // Subtitle has its own positioning
            if (centerText) {
                // Move to center of screen, then apply subtitle offsets
                drawContext.getMatrices().translate(
                        mc.getWindow().getScaledWidth() / 2.0 + subtitleXOffset,
                        mc.getWindow().getScaledHeight() / 2.0 + subtitleYOffset,
                        0);
            } else {
                // Use direct subtitle offsets
                drawContext.getMatrices().translate(subtitleXOffset, subtitleYOffset, 0);
            }

            // Apply subtitle size, with multiplier for significant territories if enabled
            float actualSubtitleSize = (float)subtitleSize;
            if (isRenderingSignificantTerritory && useEnhancedStyling) {
                actualSubtitleSize *= (float)subtitleSizeMultiplier;
            }

            // Use subtitle scale
            drawContext.getMatrices().scale(actualSubtitleSize, actualSubtitleSize, actualSubtitleSize);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            int subtitleWidth = fontRenderer.getWidth(displayedSubTitle);
            int subtitleXPos = centerText ? -(subtitleWidth / 2) : 0;

            drawContext.drawText(
                    fontRenderer,
                    displayedSubTitle,
                    subtitleXPos,
                    0,
                    0xFFFFFF | alpha, // White subtitle color
                    renderShadow);

            RenderSystem.disableBlend();
            drawContext.getMatrices().pop(); // Pop subtitle transform
        }

        // Clean up render state
        drawContext.getMatrices().pop(); // Pop main transform
    }

    /**
     * Updates title and cooldown timers
     */
    public void tick() {
        // Tick down cooldown regardless of title display
        if (cooldownTimer > 0) {
            --cooldownTimer;
        }

        // Tick down title timer only if a title is active
        if (titleTimer > 0) {
            --titleTimer;
            if (titleTimer <= 0) {
                // Only clear display variables
                displayedTitle = null;
                displayedSubTitle = null;
                // Reset significant territory flag
                isRenderingSignificantTerritory = false;
            }
        }
    }

    /**
     * Clears the current title display
     */
    public void clearTimer() {
        displayedTitle = null;
        displayedSubTitle = null;
        isRenderingSignificantTerritory = false;
    }
}