package me.jamino.wynnWanderer.features;

import java.util.Optional;

/**
 * Decides when entering a territory should display a title. Territories are identified by their friendly name.
 */
public class TerritoryTracker {
    private final TerritoryCache territoryCache;
    private String lastTerritory = null;

    public TerritoryTracker(int cacheSize) {
        this.territoryCache = new TerritoryCache(cacheSize);
    }

    /**
     * Updates the tracker with the territory the player is currently in.
     *
     * @param territoryName Friendly name of the current territory, or null if the player is not in one
     * @param onCooldown Whether the title cooldown is active
     * @param showOnlySignificantTerritories Whether titles are limited to significant territories
     * @return The name of the territory to display a title for, if any
     */
    public Optional<String> update(String territoryName, boolean onCooldown, boolean showOnlySignificantTerritories) {
        // Player left a territory and is not in a new one, or the territory has no
        // friendly name, which is treated as leaving the territory for display purposes
        if (territoryName == null || territoryName.isEmpty()) {
            lastTerritory = null;
            return Optional.empty();
        }

        if (territoryName.equals(lastTerritory)) return Optional.empty();

        // The player entered a new territory
        lastTerritory = territoryName;

        // Skip if the territory is in the recent list and cooldown is active
        if (onCooldown && territoryCache.matchesAnyEntry(territoryName::equals)) {
            return Optional.empty();
        }

        // Skip non-significant territories if the showOnlySignificantTerritories option is enabled
        if (showOnlySignificantTerritories && !SignificantTerritoryManager.isSignificant(territoryName)) {
            return Optional.empty();
        }

        territoryCache.addEntry(territoryName);
        return Optional.of(territoryName);
    }

    /**
     * Forgets the current territory, so re-entering it is treated as a new entry.
     */
    public void reset() {
        lastTerritory = null;
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
