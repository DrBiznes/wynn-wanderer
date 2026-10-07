package me.jamino.wynnWanderer.features;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class TerritoryTrackerTest {
    private static final boolean ON_COOLDOWN = true;
    private static final boolean NO_COOLDOWN = false;
    private static final boolean ONLY_SIGNIFICANT = true;
    private static final boolean ALL_TERRITORIES = false;

    @Test
    void showsATitleWhenEnteringATerritory() {
        TerritoryTracker tracker = new TerritoryTracker(3);

        assertEquals(Optional.of("Detlas"), tracker.update("Detlas", NO_COOLDOWN, ONLY_SIGNIFICANT));
    }

    @Test
    void doesNotRepeatTheTitleWhileStayingInATerritory() {
        TerritoryTracker tracker = new TerritoryTracker(3);

        tracker.update("Detlas", NO_COOLDOWN, ONLY_SIGNIFICANT);

        assertEquals(Optional.empty(), tracker.update("Detlas", ON_COOLDOWN, ONLY_SIGNIFICANT));
        assertEquals(Optional.empty(), tracker.update("Detlas", NO_COOLDOWN, ONLY_SIGNIFICANT));
    }

    @Test
    void noTitleOutsideOfTerritories() {
        TerritoryTracker tracker = new TerritoryTracker(3);

        assertEquals(Optional.empty(), tracker.update(null, NO_COOLDOWN, ALL_TERRITORIES));
        assertEquals(Optional.empty(), tracker.update("", NO_COOLDOWN, ALL_TERRITORIES));
    }

    @Test
    void insignificantTerritoriesAreOnlyShownWhenEnabled() {
        TerritoryTracker tracker = new TerritoryTracker(3);

        assertEquals(Optional.empty(), tracker.update("Detlas Suburbs", NO_COOLDOWN, ONLY_SIGNIFICANT));
        // Changing the option while standing in the territory does not pop up a title
        assertEquals(Optional.empty(), tracker.update("Detlas Suburbs", NO_COOLDOWN, ALL_TERRITORIES));

        assertEquals(Optional.of("Nivla Woods"), tracker.update("Nivla Woods", NO_COOLDOWN, ALL_TERRITORIES));
    }

    @Test
    void reenteringARecentTerritoryDuringCooldownIsSkipped() {
        TerritoryTracker tracker = new TerritoryTracker(3);

        tracker.update("Detlas", NO_COOLDOWN, ONLY_SIGNIFICANT);
        tracker.update(null, ON_COOLDOWN, ONLY_SIGNIFICANT);

        assertEquals(Optional.empty(), tracker.update("Detlas", ON_COOLDOWN, ONLY_SIGNIFICANT));
    }

    @Test
    void reenteringAfterTheCooldownShowsTheTitleAgain() {
        TerritoryTracker tracker = new TerritoryTracker(3);

        tracker.update("Detlas", NO_COOLDOWN, ONLY_SIGNIFICANT);
        tracker.update(null, ON_COOLDOWN, ONLY_SIGNIFICANT);

        assertEquals(Optional.of("Detlas"), tracker.update("Detlas", NO_COOLDOWN, ONLY_SIGNIFICANT));
    }

    @Test
    void cooldownDoesNotBlockTerritoriesThatWereNotVisitedRecently() {
        TerritoryTracker tracker = new TerritoryTracker(3);

        tracker.update("Detlas", NO_COOLDOWN, ONLY_SIGNIFICANT);

        assertEquals(Optional.of("Almuj"), tracker.update("Almuj", ON_COOLDOWN, ONLY_SIGNIFICANT));
    }

    @Test
    void territoriesThatFellOutOfTheCacheAreShownDuringCooldown() {
        TerritoryTracker tracker = new TerritoryTracker(1);

        tracker.update("Detlas", NO_COOLDOWN, ONLY_SIGNIFICANT);
        tracker.update("Almuj", ON_COOLDOWN, ONLY_SIGNIFICANT);

        assertEquals(Optional.of("Detlas"), tracker.update("Detlas", ON_COOLDOWN, ONLY_SIGNIFICANT));
    }

    @Test
    void skippedTerritoriesAreNotCached() {
        TerritoryTracker tracker = new TerritoryTracker(3);

        tracker.update("Detlas Suburbs", NO_COOLDOWN, ONLY_SIGNIFICANT);

        assertEquals(0, tracker.getTerritoryCache().size());
    }

    @Test
    void resetTreatsTheCurrentTerritoryAsNewlyEntered() {
        TerritoryTracker tracker = new TerritoryTracker(3);

        tracker.update("Detlas", NO_COOLDOWN, ONLY_SIGNIFICANT);
        tracker.reset();

        assertEquals(Optional.of("Detlas"), tracker.update("Detlas", NO_COOLDOWN, ONLY_SIGNIFICANT));
    }
}
