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
    public int textYOffset = -33;
    public int textXOffset = 0;
    public boolean centerText = true;

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
            // Total duration of fade-in = textFadeInTime
            // Time elapsed in fade-in = (Total Title Time) - age - (Display Time) - (Fade Out Time)
            // Simplified: Time into fade-in = textFadeInTime - (age - (textDisplayTime + textFadeOutTime))
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

        // Center the title if enabled
        if (centerText) {
            // Translate to the center of the screen, then apply offsets
            drawContext.getMatrices().translate(
                    mc.getWindow().getScaledWidth() / 2.0 + textXOffset, // Apply X offset relative to center
                    mc.getWindow().getScaledHeight() / 2.0 + textYOffset, // Apply Y offset relative to center
                    0);
        } else {
            // Translate based on offsets from top-left
            drawContext.getMatrices().translate(textXOffset, textYOffset, 0);
        }

        RenderSystem.enableBlend();
        // Default blend function is usually SRC_ALPHA, ONE_MINUS_SRC_ALPHA
        RenderSystem.defaultBlendFunc();

        // --- Title Rendering ---
        drawContext.getMatrices().push();
        drawContext.getMatrices().scale((float)textSize, (float)textSize, (float)textSize);

        int alpha = opacity << 24; // Apply alpha (no bitwise AND needed here)
        TextRenderer fontRenderer = mc.textRenderer;
        int titleWidth = fontRenderer.getWidth(displayedTitle);

        // Calculate x position based on centering
        int titleXPos = centerText ? -(titleWidth / 2) : 0; // If centered, offset by half width; otherwise, start at 0 (relative to translation)
        // Y position is implicitly 0 relative to the translation applied earlier

        // Draw title
        drawContext.drawText(
                fontRenderer,
                displayedTitle,
                titleXPos, // X position relative to translation/scaling
                0,         // Y position relative to translation/scaling
                titleTextColor | alpha, // Combine color and alpha
                renderShadow);

        drawContext.getMatrices().pop(); // Pop title scaling

        // --- Subtitle Rendering (if exists) ---
        if (displayedSubTitle != null) {
            drawContext.getMatrices().push();
            // Subtitle is typically smaller, scale it relative to the main translation
            float subtitleScale = 1.0F; // Adjust as needed, maybe make configurable?
            drawContext.getMatrices().scale(subtitleScale, subtitleScale, subtitleScale);

            int subtitleWidth = fontRenderer.getWidth(displayedSubTitle);
            // Calculate subtitle position relative to the main translation
            int subtitleXPos = centerText ? -(subtitleWidth / 2) : 0;
            // Position subtitle below the main title (adjust 14 based on font size/scaling if needed)
            int subtitleYPos = (int)(14 / textSize); // Adjust Y based on main title's scale if they share the same translation point

            drawContext.drawText(
                    fontRenderer,
                    displayedSubTitle,
                    subtitleXPos,
                    subtitleYPos, // Position below the title
                    0xFFFFFF | alpha, // White subtitle, maybe make configurable?
                    renderShadow);

            drawContext.getMatrices().pop(); // Pop subtitle scaling
        }

        // Clean up render state
        RenderSystem.disableBlend();
        drawContext.getMatrices().pop(); // Pop main translation
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
                // Only clear display variables, timer is handled in tickTitle
                displayedTitle = null;
                displayedSubTitle = null;
                // Do not reset titleTimer here, tickTitle handles reaching 0
            }
        }
    }

    /**
     * Clears the current title display
     */
    public void clearTimer() {
        // Only clear display variables, timer is handled in tickTitle
        displayedTitle = null;
        displayedSubTitle = null;
        // Do not reset titleTimer here, tickTitle handles reaching 0
    }
}