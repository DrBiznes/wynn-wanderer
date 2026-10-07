package me.jamino.wynnWanderer.features;

import com.wynntils.core.components.Models;
import com.wynntils.models.territories.profile.TerritoryProfile;
import me.jamino.wynnWanderer.WynnWanderer;
import me.jamino.wynnWanderer.config.WynnWandererConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class TerritoryTitleCore {
    private static final int CHECK_INTERVAL_TICKS = 10;
    private int tickCounter = 0;

    // Timers for the title currently being displayed
    private final TitleAnimation animation = new TitleAnimation();

    // Tracks the current and recently visited territories
    private final TerritoryTracker territoryTracker = new TerritoryTracker(3);

    // Title renderer for visualization
    private final TerritoryRenderer territoryRenderer = new TerritoryRenderer(this);

    // The title being displayed
    private DisplayedTitle displayedTitle = null;

    /**
     * A title that is ready to be rendered. The subtitle is null if there is none to show.
     */
    public record DisplayedTitle(Component title, Component subtitle, int color, boolean significant) {}

    public void initialize() {
        // Register tick event to periodically check for territory changes
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);

        // Register HUD element to render the title
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("wynn-wanderer", "territory_title"), territoryRenderer::renderTitle);
    }

    private void tick(Minecraft client) {
        WynnWandererConfig.TerritoryTitlesConfig config = WynnWanderer.getConfig().territoryTitles;
        if (!config.enabled) {
            if (displayedTitle != null) {
                clearTimer();
            }
            return;
        }

        // Only run when on Wynncraft world
        try {
            // Ensure player and world state are available before checking
            if (client.player == null || !Models.WorldState.onWorld()) return;
        } catch (Exception e) {
            // Wynntils might not be loaded yet or world state check failed
            WynnWanderer.LOGGER.debug("Error checking world state (Wynntils might be initializing)", e);
            return;
        }

        // Territory check timer
        tickCounter++;
        if (tickCounter >= CHECK_INTERVAL_TICKS) {
            tickCounter = 0;
            checkTerritory(client, config);
        }

        // Title animation timer
        if (animation.tick()) {
            displayedTitle = null;
        }
    }

    private void checkTerritory(Minecraft client, WynnWandererConfig.TerritoryTitlesConfig config) {
        try {
            territoryTracker.getTerritoryCache().setCacheSize(config.animation.recentTerritoryCacheSize);

            // Get territory at current position
            TerritoryProfile currentTerritory =
                    Models.Territory.getTerritoryProfileForPosition(client.player.position());
            String territoryName = currentTerritory == null ? null : currentTerritory.getFriendlyName();

            territoryTracker
                    .update(territoryName, animation.isOnCooldown(), config.showOnlySignificantTerritories)
                    .ifPresent(this::displayTerritoryTitle);
        } catch (Exception e) {
            WynnWanderer.LOGGER.error("Error checking territory", e);
        }
    }

    /**
     * Displays the title of a territory, as if the player had just entered it.
     *
     * @param territoryName Friendly name of the territory
     */
    public void displayTerritoryTitle(String territoryName) {
        WynnWandererConfig.TerritoryTitlesConfig config = WynnWanderer.getConfig().territoryTitles;
        Language language = Language.getInstance();
        TitleResolver.ResolvedTitle resolved =
                TitleResolver.resolve(territoryName, config, key -> language.has(key) ? language.getOrDefault(key) : null);

        Component title = Component.translatable(resolved.titleKey(), territoryName);

        // Only set subtitle if they are enabled and there is something to show
        Component subtitle = null;
        if (config.appearance.showSubtitles) {
            subtitle = Component.translatable(resolved.subtitleKey(), territoryName);
            if (subtitle.getString().isBlank()) {
                subtitle = null;
            }
        }

        // Start displaying the title
        displayedTitle = new DisplayedTitle(title, subtitle, resolved.color(), resolved.significant());
        animation.start(
                config.animation.textFadeInTime,
                config.animation.textDisplayTime,
                config.animation.textFadeOutTime,
                config.animation.textCooldownTime);
    }

    /**
     * @return The title being displayed, or null if there is none
     */
    public DisplayedTitle getDisplayedTitle() {
        return displayedTitle;
    }

    public TerritoryRenderer getTerritoryRenderer() {
        return territoryRenderer;
    }

    public TitleAnimation getAnimation() {
        return animation;
    }

    // For external access to clear timers if needed
    public void clearTimer() {
        displayedTitle = null;
        animation.stop();
        territoryTracker.reset();
    }

    /**
     * Get the territory cache for testing or debugging purposes
     *
     * @return The territory cache
     */
    public TerritoryCache getTerritoryCache() {
        return territoryTracker.getTerritoryCache();
    }
}
