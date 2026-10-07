package me.jamino.wynnWanderer.features;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TerritoryCacheTest {
    private static boolean contains(TerritoryCache cache, String territory) {
        return cache.matchesAnyEntry(territory::equals);
    }

    @Test
    void remembersRecentTerritories() {
        TerritoryCache cache = new TerritoryCache(3);

        cache.addEntry("Ragni");
        cache.addEntry("Detlas");

        assertEquals(2, cache.size());
        assertTrue(contains(cache, "Ragni"));
        assertTrue(contains(cache, "Detlas"));
        assertFalse(contains(cache, "Almuj"));
    }

    @Test
    void evictsTheOldestEntryWhenFull() {
        TerritoryCache cache = new TerritoryCache(3);

        cache.addEntry("Ragni");
        cache.addEntry("Detlas");
        cache.addEntry("Almuj");
        cache.addEntry("Nemract");

        assertEquals(3, cache.size());
        assertFalse(contains(cache, "Ragni"));
        assertTrue(contains(cache, "Detlas"));
        assertTrue(contains(cache, "Nemract"));
    }

    @Test
    void doesNotDuplicateTheLatestEntry() {
        TerritoryCache cache = new TerritoryCache(3);

        cache.addEntry("Ragni");
        cache.addEntry("Ragni");
        assertEquals(1, cache.size());

        // Going back and forth is two separate visits
        cache.addEntry("Detlas");
        cache.addEntry("Ragni");
        assertEquals(3, cache.size());
    }

    @Test
    void shrinkingDropsTheOldestEntries() {
        TerritoryCache cache = new TerritoryCache(3);
        cache.addEntry("Ragni");
        cache.addEntry("Detlas");
        cache.addEntry("Almuj");

        cache.setCacheSize(1);

        assertEquals(1, cache.getCacheSize());
        assertEquals(1, cache.size());
        assertTrue(contains(cache, "Almuj"));
    }

    @Test
    void zeroSizedCacheStoresNothing() {
        TerritoryCache cache = new TerritoryCache(0);

        cache.addEntry("Ragni");

        assertEquals(0, cache.size());
        assertFalse(contains(cache, "Ragni"));
    }

    @Test
    void clearRemovesEverything() {
        TerritoryCache cache = new TerritoryCache(3);
        cache.addEntry("Ragni");

        cache.clear();

        assertEquals(0, cache.size());
        assertFalse(contains(cache, "Ragni"));
    }
}
