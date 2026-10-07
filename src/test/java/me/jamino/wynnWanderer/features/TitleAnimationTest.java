package me.jamino.wynnWanderer.features;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TitleAnimationTest {
    private static TitleAnimation started(int fadeIn, int display, int fadeOut, int cooldown) {
        TitleAnimation animation = new TitleAnimation();
        animation.start(fadeIn, display, fadeOut, cooldown);
        return animation;
    }

    private static void tick(TitleAnimation animation, int ticks) {
        for (int i = 0; i < ticks; i++) {
            animation.tick();
        }
    }

    @Test
    void isInactiveUntilStarted() {
        TitleAnimation animation = new TitleAnimation();

        assertFalse(animation.isActive());
        assertFalse(animation.isOnCooldown());
        assertEquals(0, animation.getOpacity(0f));
        assertFalse(animation.tick());
    }

    @Test
    void fadesInHoldsAndFadesOut() {
        TitleAnimation animation = started(10, 50, 10, 80);

        // Fade-in
        assertEquals(0, animation.getOpacity(0f));
        tick(animation, 5);
        assertEquals(127, animation.getOpacity(0f));
        tick(animation, 5);

        // Display
        assertEquals(255, animation.getOpacity(0f));
        tick(animation, 25);
        assertEquals(255, animation.getOpacity(0f));
        tick(animation, 25);

        // Fade-out
        assertEquals(255, animation.getOpacity(0f));
        tick(animation, 5);
        assertEquals(127, animation.getOpacity(0f));
        tick(animation, 4);
        assertEquals(25, animation.getOpacity(0f));
        assertTrue(animation.isActive());
    }

    @Test
    void opacityIsInterpolatedBetweenTicks() {
        TitleAnimation animation = started(10, 50, 10, 80);

        int previous = animation.getOpacity(0f);
        for (float partialTicks = 0.25f; partialTicks <= 1f; partialTicks += 0.25f) {
            int opacity = animation.getOpacity(partialTicks);
            assertTrue(opacity > previous, "opacity should increase during the fade-in");
            previous = opacity;
        }

        // The end of one tick lines up with the start of the next
        int endOfTick = animation.getOpacity(1f);
        animation.tick();
        assertEquals(endOfTick, animation.getOpacity(0f));
    }

    @Test
    void reportsFinishingExactlyOnce() {
        TitleAnimation animation = started(2, 3, 2, 0);

        for (int i = 0; i < 6; i++) {
            assertFalse(animation.tick(), "should still be displaying on tick " + i);
            assertTrue(animation.isActive());
        }

        assertTrue(animation.tick());
        assertFalse(animation.isActive());
        assertEquals(0, animation.getOpacity(0f));
        assertFalse(animation.tick());
    }

    @Test
    void cooldownOutlivesTheTitleAndSurvivesStop() {
        TitleAnimation animation = started(1, 1, 1, 10);

        tick(animation, 3);
        assertFalse(animation.isActive());
        assertTrue(animation.isOnCooldown());

        tick(animation, 7);
        assertFalse(animation.isOnCooldown());

        animation.start(10, 10, 10, 10);
        animation.stop();
        assertFalse(animation.isActive());
        assertTrue(animation.isOnCooldown());
    }

    @Test
    void zeroLengthFadesDoNotDivideByZero() {
        TitleAnimation animation = started(0, 5, 0, 0);

        // Instant fade-in
        assertEquals(255, animation.getOpacity(0f));
        assertEquals(255, animation.getOpacity(0.5f));
        tick(animation, 4);
        assertEquals(255, animation.getOpacity(0f));
        assertTrue(animation.tick());

        TitleAnimation instant = started(0, 0, 0, 0);
        assertFalse(instant.isActive());
        assertEquals(0, instant.getOpacity(0f));
    }

    @Test
    void negativeTimesAreTreatedAsZero() {
        TitleAnimation animation = started(-5, 4, -5, -5);

        assertTrue(animation.isActive());
        assertFalse(animation.isOnCooldown());
        assertEquals(255, animation.getOpacity(0f));
        tick(animation, 4);
        assertFalse(animation.isActive());
    }

    @Test
    void nearlyTransparentTitlesAreNotVisible() {
        TitleAnimation animation = started(100, 10, 100, 0);

        assertFalse(animation.isVisible(0f));
        tick(animation, 3);
        assertEquals(7, animation.getOpacity(0f));
        assertFalse(animation.isVisible(0f));
        animation.tick();
        assertTrue(animation.isVisible(0f));
    }
}
