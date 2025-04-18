package me.jamino.wynnWanderer.features;

import com.wynntils.models.territories.profile.TerritoryProfile;

import java.util.LinkedList;
import java.util.function.Predicate;

/**
 * Manages a cache of recently visited territories to prevent
 * repeated title displays when crossing the same territories repeatedly.
 */
public class TerritoryCache {
    private final LinkedList<TerritoryProfile> recentEntries = new LinkedList<>();
    private int cacheSize = 3;

    /**
     * Constructs a territory cache with the specified size.
     *
     * @param cacheSize Maximum number of territories to cache
     */
    public TerritoryCache(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    /**
     * Adds a territory to the cache of recently visited territories.
     *
     * @param entry The territory profile to add
     */
    public void addEntry(TerritoryProfile entry) {
        // Avoid adding duplicates if it's already the last entry
        if (!recentEntries.isEmpty() && recentEntries.getLast().equals(entry)) {
            return;
        }

        // Remove oldest if cache size is exceeded
        while (recentEntries.size() >= cacheSize && !recentEntries.isEmpty()) {
            recentEntries.removeFirst();
        }

        // Add new entry if cache size allows
        if (cacheSize > 0) {
            recentEntries.addLast(entry);
        }
    }

    /**
     * Checks if any entry in the cache matches the given predicate.
     *
     * @param entryMatchPredicate Predicate to test entries against
     * @return true if any entry matches the predicate, false otherwise
     */
    public boolean matchesAnyEntry(Predicate<TerritoryProfile> entryMatchPredicate) {
        // Check if the predicate matches any entry in the current list
        return recentEntries.stream().anyMatch(entryMatchPredicate);
    }

    /**
     * Changes the maximum size of the cache.
     *
     * @param newSize The new maximum cache size
     */
    public void setCacheSize(int newSize) {
        this.cacheSize = newSize;

        // Ensure the recent entries list respects the new cache size immediately
        while (recentEntries.size() > this.cacheSize && !recentEntries.isEmpty()) {
            recentEntries.removeFirst();
        }
    }

    /**
     * Clears all entries from the cache.
     */
    public void clear() {
        recentEntries.clear();
    }

    /**
     * Gets the current number of entries in the cache.
     *
     * @return The number of cached entries
     */
    public int size() {
        return recentEntries.size();
    }

    /**
     * Gets the maximum size of the cache.
     *
     * @return The maximum cache size
     */
    public int getCacheSize() {
        return cacheSize;
    }
}