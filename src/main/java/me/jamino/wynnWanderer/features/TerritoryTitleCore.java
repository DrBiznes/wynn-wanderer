package me.jamino.wynnWanderer.features;

import com.wynntils.core.components.Models;
import com.wynntils.models.territories.profile.TerritoryProfile;
import java.util.List;
import me.jamino.wynnWanderer.WynnWanderer;
import me.jamino.wynnWanderer.config.WynnWandererConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
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
     *
     * @param background true if the title is text on top of a background, see {@link TitleText}
     */
    public record DisplayedTitle(
            Component title,
            Component subtitle,
            int color,
            int subtitleColor,
            boolean background,
            boolean significant) {}

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

        // The title can switch between the two colors of the territory, and between fonts
        String titleText =
                Component.translatable(resolved.titleKey(), territoryName).getString();
        List<TitleText.Segment> segments = TitleText.split(titleText, resolved.color(), resolved.textColor());
        Component title = toComponent(segments);

        // Only set subtitle if they are enabled and there is something to show
        Component subtitle = null;
        if (config.appearance.showSubtitles) {
            String subtitleText =
                    Component.translatable(resolved.subtitleKey(), territoryName).getString();
            subtitle = toComponent(TitleText.split(subtitleText, resolved.subtitleColor(), resolved.subtitleColor()));
            if (subtitle.getString().isBlank()) {
                subtitle = null;
            }
        }

        // Start displaying the title
        displayedTitle = new DisplayedTitle(
                title,
                subtitle,
                resolved.color(),
                resolved.subtitleColor(),
                TitleText.hasBackground(segments),
                resolved.significant());
        animation.start(
                config.animation.textFadeInTime,
                config.animation.textDisplayTime,
                config.animation.textFadeOutTime,
                config.animation.textCooldownTime);
    }

    private static Component toComponent(List<TitleText.Segment> segments) {
        MutableComponent component = Component.empty();
        for (TitleText.Segment segment : segments) {
            Style style = Style.EMPTY.withColor(segment.color());

            Identifier font = segment.font() == null ? null : Identifier.tryParse(segment.font());
            if (font != null) {
                style = style.withFont(new FontDescription.Resource(font));
            }
            // Only the background has a shadow, which would otherwise be drawn over it
            if (segment.foreground()) {
                style = style.withoutShadow();
            }

            component.append(Component.literal(segment.text()).withStyle(style));
        }
        return component;
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
