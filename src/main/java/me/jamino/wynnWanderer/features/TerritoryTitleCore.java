package me.jamino.wynnWanderer.features;

import com.wynntils.core.components.Models;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.utils.mc.McUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import java.util.LinkedList;
import java.util.function.Predicate;

public class TerritoryTitleCore {
    private TerritoryProfile lastTerritoryProfile = null;
    private static final int CHECK_INTERVAL_TICKS = 10;
    private int tickCounter = 0;
    private boolean isEnabled = true;

    // Configuration properties
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
    public int recentTerritoryCacheSize = 3;

    // Title renderer state
    private final LinkedList<TerritoryProfile> recentEntries = new LinkedList<>();
    private Text displayedTitle = null;
    private Text displayedSubTitle = null;
    private int titleTimer = 0;
    private int cooldownTimer = 0;
    private int titleTextColor = 0xFFFFFF;

    public void initialize() {
        // Register tick event to periodically check for territory changes
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isEnabled) return;

            // Only run when on Wynncraft world
            try {
                if (!Models.WorldState.onWorld()) return;
            } catch (Exception e) {
                // Wynntils might not be loaded yet
                return;
            }

            // Territory check timer
            tickCounter++;
            if (tickCounter >= CHECK_INTERVAL_TICKS) {
                tickCounter = 0;
                checkTerritory();
            }

            // Title animation timer
            tickTitle();
        });

        // Register HUD render event
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            renderTitle(drawContext, tickDelta);
        });
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        if (!enabled) {
            lastTerritoryProfile = null;
            clearTitle();
        }
    }

    /**
     * Updates configuration settings for the title renderer
     */
    public void updateSettings(
            boolean enabled,
            int fadeInTime,
            int displayTime,
            int fadeOutTime,
            int cooldownTime,
            String textColor,
            double textSize,
            boolean renderShadow,
            int xOffset,
            int yOffset,
            boolean centerText,
            int cacheSize
    ) {
        this.enabled = enabled;
        this.textFadeInTime = fadeInTime;
        this.textDisplayTime = displayTime;
        this.textFadeOutTime = fadeOutTime;
        this.textCooldownTime = cooldownTime;
        this.textColor = textColor;
        setTextColor(textColor);
        this.textSize = textSize;
        this.renderShadow = renderShadow;
        this.textXOffset = xOffset;
        this.textYOffset = yOffset;
        this.centerText = centerText;
        this.recentTerritoryCacheSize = cacheSize;
    }

    private void checkTerritory() {
        if (McUtils.player() == null) return;

        try {
            // Get player position using getPos() instead of position()
            Vec3d playerPos = McUtils.player().getPos();

            // Get territory at current position
            TerritoryProfile currentTerritory = Models.Territory.getTerritoryProfileForPosition(playerPos);

            // If player entered a new territory
            if (currentTerritory != null && !currentTerritory.equals(lastTerritoryProfile)) {
                // Skip if the territory is in the recent list and cooldown is active
                if (cooldownTimer > 0 && matchesAnyRecentEntry(t -> t.equals(currentTerritory))) {
                    lastTerritoryProfile = currentTerritory;
                    return;
                }

                String territoryName = currentTerritory.getFriendlyName();
                displayTerritoryTitle(territoryName);

                // Update last territory and add to recent entries
                lastTerritoryProfile = currentTerritory;
                addRecentEntry(currentTerritory);
            } else if (currentTerritory == null && lastTerritoryProfile != null) {
                // Player left a territory and is not in a new one
                lastTerritoryProfile = null;
            }
        } catch (Exception e) {
            System.err.println("Error checking territory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayTerritoryTitle(String territoryName) {
        // Create the title and subtitle components with translation keys
        Text title = Text.translatable("wynn_wanderer.territory.entering.title", territoryName);
        Text subtitle = Text.translatable("wynn_wanderer.territory.entering.subtitle");

        // Start displaying the title
        displayedTitle = title;
        displayedSubTitle = subtitle;
        titleTimer = textFadeInTime + textDisplayTime + textFadeOutTime;
        cooldownTimer = textCooldownTime;
    }

    private void tickTitle() {
        if (titleTimer > 0) {
            --titleTimer;
            if (titleTimer <= 0) {
                clearTitle();
            }
        }
        if (cooldownTimer > 0) {
            --cooldownTimer;
        }
    }

    private void clearTitle() {
        titleTimer = 0;
        displayedTitle = null;
        displayedSubTitle = null;
    }

    private void renderTitle(DrawContext drawContext, float partialTicks) {
        if (!isEnabled || displayedTitle == null || titleTimer <= 0) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.debugEnabled) return;  // Use field instead of method

        // Calculate fade opacity
        float age = (float) titleTimer - partialTicks;
        int opacity = 255;

        if (titleTimer > textFadeOutTime + textDisplayTime) {
            float fadeIn = (float) (textFadeInTime + textDisplayTime + textFadeOutTime) - age;
            opacity = (int) (fadeIn * 255.0F / (float) textFadeInTime);
        } else if (titleTimer <= textFadeOutTime) {
            opacity = (int) (age * 255.0F / (float) textFadeOutTime);
        }

        opacity = MathHelper.clamp(opacity, 0, 255);
        if (opacity < 8) return;

        // Set up rendering state
        drawContext.getMatrices().push();

        // Center the title if enabled
        if (centerText) {
            drawContext.getMatrices().translate(
                    mc.getWindow().getScaledWidth() / 2.0,
                    mc.getWindow().getScaledHeight() / 2.0,
                    0);
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Title rendering
        drawContext.getMatrices().push();
        drawContext.getMatrices().scale((float)textSize, (float)textSize, (float)textSize);

        int alpha = opacity << 24 & 0xFF000000;
        TextRenderer fontRenderer = mc.textRenderer;
        int titleWidth = fontRenderer.getWidth(displayedTitle);

        // Calculate x position - centered or from left
        int xPos = centerText
                ? textXOffset - (titleWidth / 2)
                : textXOffset;

        // Draw title
        drawContext.drawText(
                fontRenderer,
                displayedTitle,
                xPos,
                textYOffset,
                titleTextColor | alpha,
                renderShadow);

        drawContext.getMatrices().pop();

        // Subtitle rendering (if exists)
        if (displayedSubTitle != null) {
            drawContext.getMatrices().push();
            float subtitleScale = 1.0F;
            drawContext.getMatrices().scale(subtitleScale, subtitleScale, subtitleScale);

            int subtitleWidth = fontRenderer.getWidth(displayedSubTitle);
            int subtitleXPos = centerText
                    ? -subtitleWidth / 2
                    : 0;

            drawContext.drawText(
                    fontRenderer,
                    displayedSubTitle,
                    subtitleXPos,
                    textYOffset + 14,
                    0xFFFFFF | alpha,
                    renderShadow);

            drawContext.getMatrices().pop();
        }

        // Clean up render state
        RenderSystem.disableBlend();
        drawContext.getMatrices().pop();
    }

    private void setTextColor(String textColor) {
        try {
            this.titleTextColor = (int) Long.parseLong(textColor, 16);
        } catch (Exception e) {
            System.err.println("Text color " + textColor + " is not a valid RGB color. Defaulting to white...");
            this.titleTextColor = 0xFFFFFF;
        }
    }

    private void addRecentEntry(TerritoryProfile entry) {
        if (recentEntries.size() >= recentTerritoryCacheSize && !recentEntries.isEmpty()) {
            recentEntries.removeFirst();
        }
        if (recentTerritoryCacheSize > 0) {
            recentEntries.addLast(entry);
        }
    }

    private boolean matchesAnyRecentEntry(Predicate<TerritoryProfile> entryMatchPredicate) {
        return recentEntries.stream().anyMatch(entryMatchPredicate);
    }
}