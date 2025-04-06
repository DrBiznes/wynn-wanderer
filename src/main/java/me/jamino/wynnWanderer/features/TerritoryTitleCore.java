package me.jamino.wynnWanderer.features;

import com.wynntils.core.components.Models;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
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

    // Title renderer for visualization
    public TerritoryRenderer territoryRenderer = new TerritoryRenderer();

    // Track recently visited territories
    public final LinkedList<TerritoryProfile> recentEntries = new LinkedList<>();

    // Configuration for recently visited territory cache
    public int recentTerritoryCacheSize = 3;

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
        this.isEnabled = enabled;
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
        territoryRenderer.centerText = centerText;
        this.recentTerritoryCacheSize = cacheSize;

        // Ensure the recent entries list respects the new cache size immediately
        while (recentEntries.size() > this.recentTerritoryCacheSize && !recentEntries.isEmpty()) {
            recentEntries.removeFirst();
        }
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
                if (territoryRenderer.cooldownTimer > 0 && matchesAnyRecentEntry(t -> t.equals(currentTerritory))) {
                    lastTerritoryProfile = currentTerritory;
                    return;
                }

                String territoryName = currentTerritory.getFriendlyName();
                // Only display if the name is not null or empty
                if (territoryName != null && !territoryName.isEmpty()) {
                    displayTerritoryTitle(territoryName, currentTerritory);

                    // Update last territory and add to recent entries
                    lastTerritoryProfile = currentTerritory;
                    addRecentEntry(currentTerritory);
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
            // Consider more specific error handling or logging if needed
            // e.printStackTrace(); // Optional: uncomment for detailed stack trace during debugging
        }
    }

    private void displayTerritoryTitle(String territoryName, TerritoryProfile territory) {
        // Check if this is a significant territory for special styling
        boolean isSignificantTerritory = SignificantTerritoryManager.SIGNIFICANT_TERRITORIES.contains(territoryName);
        Text title;
        Text subtitle = null;

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
            try {
                // Try to see if the client can render the color key
                Text colorText = Text.translatable(colorKey);
                String colorString = colorText.getString();
                // If we get something that looks like a color (hex digits), use it
                if (colorString.matches("[0-9A-Fa-f]{6}")) {
                    territoryRenderer.setColor(colorString);
                } else {
                    // Fallback to default
                    territoryRenderer.setColor(territoryRenderer.textColor);
                }
            } catch (Exception e) {
                // If there's any error, use the default color
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
        territoryRenderer.displayedSubTitle = subtitle;
        territoryRenderer.titleTimer = territoryRenderer.textFadeInTime + territoryRenderer.textDisplayTime + territoryRenderer.textFadeOutTime;
        territoryRenderer.cooldownTimer = territoryRenderer.textCooldownTime; // Start cooldown immediately upon display
    }

    private void addRecentEntry(TerritoryProfile entry) {
        // Avoid adding duplicates if it's already the last entry
        if (!recentEntries.isEmpty() && recentEntries.getLast().equals(entry)) {
            return;
        }

        // Remove oldest if cache size is exceeded
        while (recentEntries.size() >= recentTerritoryCacheSize && !recentEntries.isEmpty()) {
            recentEntries.removeFirst();
        }
        // Add new entry if cache size allows
        if (recentTerritoryCacheSize > 0) {
            recentEntries.addLast(entry);
        }
    }

    private boolean matchesAnyRecentEntry(Predicate<TerritoryProfile> entryMatchPredicate) {
        // Check if the predicate matches any entry in the current list
        return recentEntries.stream().anyMatch(entryMatchPredicate);
    }

    // For external access to clear timers if needed
    public void clearTimer() {
        territoryRenderer.clearTimer();
    }
}