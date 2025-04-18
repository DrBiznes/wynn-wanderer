package me.jamino.wynnWanderer.features;

import com.wynntils.core.components.Models;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class TerritoryTitleCore {
    private TerritoryProfile lastTerritoryProfile = null;
    private static final int CHECK_INTERVAL_TICKS = 10;
    private int tickCounter = 0;
    private boolean isEnabled = true;

    // Flag for only showing significant territories
    private boolean showOnlySignificantTerritories = true;

    // Title renderer for visualization
    public TerritoryRenderer territoryRenderer = new TerritoryRenderer();

    // Territory cache for tracking recently visited territories
    private TerritoryCache territoryCache;

    public TerritoryTitleCore() {
        // Initialize cache with default size
        territoryCache = new TerritoryCache(3);
    }

    public void initialize() {
        // Register tick event to periodically check for territory changes
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isEnabled) return;

            // Only run when on Wynncraft world
            try {
                // Ensure player and world state are available before checking
                if (client.player == null || !Models.WorldState.onWorld()) return;
            } catch (Exception e) {
                // Wynntils might not be loaded yet or world state check failed
                System.err.println("Error checking world state (Wynntils might be initializing): " + e.getMessage());
                return;
            }

            // Territory check timer
            tickCounter++;
            if (tickCounter >= CHECK_INTERVAL_TICKS) {
                tickCounter = 0;
                checkTerritory();
            }

            // Title animation timer
            territoryRenderer.tick();
        });

        // Register HUD render event
        HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
            // Pass the float delta time to renderTitle - using the tickDelta method
            territoryRenderer.renderTitle(drawContext, renderTickCounter.getTickDelta(false));
        });
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        if (!enabled) {
            lastTerritoryProfile = null;
            territoryRenderer.clearTimer();
        }
    }

    /**
     * Updates configuration settings for the title renderer
     */
    public void updateSettings(
            boolean enabled,
            boolean showOnlySignificantTerritories,
            int fadeInTime,
            int displayTime,
            int fadeOutTime,
            int cooldownTime,
            String textColor,
            double textSize,
            boolean renderShadow,
            int xOffset,
            int yOffset,
            int subtitleXOffset,
            int subtitleYOffset,
            double subtitleSize,
            boolean showSubtitles,
            boolean useEnhancedStyling,
            double titleSizeMultiplier,
            double subtitleSizeMultiplier,
            boolean useCustomColors,
            String defaultSignificantColor,
            boolean centerText,
            int cacheSize
    ) {
        this.isEnabled = enabled;
        this.showOnlySignificantTerritories = showOnlySignificantTerritories;
        territoryRenderer.enabled = enabled;
        territoryRenderer.textFadeInTime = fadeInTime;
        territoryRenderer.textDisplayTime = displayTime;
        territoryRenderer.textFadeOutTime = fadeOutTime;
        territoryRenderer.textCooldownTime = cooldownTime;
        territoryRenderer.textColor = textColor;
        territoryRenderer.setColor(textColor);
        territoryRenderer.textSize = textSize;
        territoryRenderer.renderShadow = renderShadow;
        territoryRenderer.textXOffset = xOffset;
        territoryRenderer.textYOffset = yOffset;
        territoryRenderer.subtitleXOffset = subtitleXOffset;
        territoryRenderer.subtitleYOffset = subtitleYOffset;
        territoryRenderer.subtitleSize = subtitleSize;
        territoryRenderer.showSubtitles = showSubtitles;
        territoryRenderer.useEnhancedStyling = useEnhancedStyling;
        territoryRenderer.titleSizeMultiplier = titleSizeMultiplier;
        territoryRenderer.subtitleSizeMultiplier = subtitleSizeMultiplier;
        territoryRenderer.useCustomColors = useCustomColors;
        territoryRenderer.defaultSignificantColor = defaultSignificantColor;
        territoryRenderer.centerText = centerText;

        // Update the cache size
        territoryCache.setCacheSize(cacheSize);
    }

    private void checkTerritory() {
        // Player null check already happened in tick event, but good practice to keep redundancy
        if (McUtils.player() == null) return;

        try {
            // Get player position using getPos() instead of position()
            Vec3d playerPos = McUtils.player().getPos();

            // Get territory at current position
            TerritoryProfile currentTerritory = Models.Territory.getTerritoryProfileForPosition(playerPos);

            // If player entered a new territory
            if (currentTerritory != null && !currentTerritory.equals(lastTerritoryProfile)) {
                // Skip if the territory is in the recent list and cooldown is active
                if (territoryRenderer.cooldownTimer > 0 && territoryCache.matchesAnyEntry(t -> t.equals(currentTerritory))) {
                    lastTerritoryProfile = currentTerritory;
                    return;
                }

                String territoryName = currentTerritory.getFriendlyName();
                // Only display if the name is not null or empty
                if (territoryName != null && !territoryName.isEmpty()) {
                    boolean isSignificantTerritory = SignificantTerritoryManager.SIGNIFICANT_TERRITORIES.contains(territoryName);

                    // Skip non-significant territories if the showOnlySignificantTerritories option is enabled
                    if (showOnlySignificantTerritories && !isSignificantTerritory) {
                        lastTerritoryProfile = currentTerritory;
                        return;
                    }

                    displayTerritoryTitle(territoryName, currentTerritory);

                    // Update last territory and add to recent entries
                    lastTerritoryProfile = currentTerritory;
                    territoryCache.addEntry(currentTerritory);
                } else {
                    // Territory exists but has no friendly name, treat as leaving territory for display purposes
                    lastTerritoryProfile = null;
                }

            } else if (currentTerritory == null && lastTerritoryProfile != null) {
                // Player left a territory and is not in a new one
                lastTerritoryProfile = null;
            }
        } catch (Exception e) {
            System.err.println("Error checking territory: " + e.getMessage());
        }
    }

    private void displayTerritoryTitle(String territoryName, TerritoryProfile territory) {
        // Check if this is a significant territory for special styling
        boolean isSignificantTerritory = SignificantTerritoryManager.SIGNIFICANT_TERRITORIES.contains(territoryName);
        Text title;
        Text subtitle = null;

        // Set the rendering mode for significant territories
        territoryRenderer.setSignificantTerritoryMode(isSignificantTerritory);

        if (isSignificantTerritory) {
            // Create territory-specific key for custom styling
            String sanitizedName = territoryName.toLowerCase().replace(" ", "_");

            // Create translation keys based on territory name
            String titleKey = "wynn_wanderer.territory." + sanitizedName + ".title";
            String subtitleKey = "wynn_wanderer.territory." + sanitizedName + ".subtitle";
            String colorKey = "wynn_wanderer.territory." + sanitizedName + ".color";

            // Create the title with the specific key
            title = Text.translatable(titleKey);

            // Create the subtitle with the specific key
            subtitle = Text.translatable(subtitleKey);

            // Try to get custom color from config or fallback
            if (territoryRenderer.useCustomColors) {
                try {
                    // Try to see if the client can render the color key
                    Text colorText = Text.translatable(colorKey);
                    String colorString = colorText.getString();
                    // If we get something that looks like a color (hex digits), use it
                    if (colorString.matches("[0-9A-Fa-f]{6}")) {
                        territoryRenderer.setColor(colorString);
                    } else if (territoryRenderer.useEnhancedStyling) {
                        // Fallback to default significant color
                        territoryRenderer.setColor(territoryRenderer.defaultSignificantColor);
                    } else {
                        // Fallback to regular color
                        territoryRenderer.setColor(territoryRenderer.textColor);
                    }
                } catch (Exception e) {
                    // If there's any error, use appropriate fallback
                    if (territoryRenderer.useEnhancedStyling) {
                        territoryRenderer.setColor(territoryRenderer.defaultSignificantColor);
                    } else {
                        territoryRenderer.setColor(territoryRenderer.textColor);
                    }
                }
            } else {
                // If not using custom colors, use regular color
                territoryRenderer.setColor(territoryRenderer.textColor);
            }

            // If we're unable to find translations, fall back to the significant format
            // We can check if the rendered text equals the raw key as a heuristic
            if (title.getString().equals(titleKey)) {
                title = Text.translatable("wynn_wanderer.territory.significant.title", territoryName);
            }

            if (subtitle.getString().equals(subtitleKey)) {
                subtitle = Text.translatable("wynn_wanderer.territory.significant.subtitle");
            }
        } else {
            // For regular territories, use the generic "Entering X" title
            title = Text.translatable("wynn_wanderer.territory.entering.title", territoryName);
            subtitle = Text.translatable("wynn_wanderer.territory.entering.subtitle");
            territoryRenderer.setColor(territoryRenderer.textColor); // Use default color
        }

        // Start displaying the title
        territoryRenderer.displayedTitle = title;
        // Only set subtitle if they're enabled
        territoryRenderer.displayedSubTitle = territoryRenderer.showSubtitles ? subtitle : null;
        territoryRenderer.titleTimer = territoryRenderer.textFadeInTime + territoryRenderer.textDisplayTime + territoryRenderer.textFadeOutTime;
        territoryRenderer.cooldownTimer = territoryRenderer.textCooldownTime; // Start cooldown immediately upon display
    }

    // For external access to clear timers if needed
    public void clearTimer() {
        territoryRenderer.clearTimer();
    }

    /**
     * Get the territory cache for testing or debugging purposes
     *
     * @return The territory cache
     */
    public TerritoryCache getTerritoryCache() {
        return territoryCache;
    }
}