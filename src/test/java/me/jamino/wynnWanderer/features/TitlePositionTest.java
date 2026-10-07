package me.jamino.wynnWanderer.features;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TitlePositionTest {
    private static final int SCREEN_WIDTH = 480;
    private static final int SCREEN_HEIGHT = 270;

    @Test
    void centeredTextIsCenteredOnTheMiddleOfTheScreen() {
        TitlePosition position = TitlePosition.of(SCREEN_WIDTH, SCREEN_HEIGHT, 0, 0, true, 100, 1f);

        assertEquals(new TitlePosition(190f, 135f), position);
    }

    @Test
    void centeredOffsetsAreRelativeToTheMiddleOfTheScreen() {
        TitlePosition position = TitlePosition.of(SCREEN_WIDTH, SCREEN_HEIGHT, 20, -80, true, 100, 1f);

        assertEquals(new TitlePosition(210f, 55f), position);
    }

    @Test
    void centeringAccountsForTheTextSize() {
        // 100 wide text drawn at double size is 200 wide
        TitlePosition position = TitlePosition.of(SCREEN_WIDTH, SCREEN_HEIGHT, 0, 0, true, 100, 2f);

        assertEquals(140f, position.x());
        // The text grows downwards from the offset
        assertEquals(135f, position.y());
    }

    @Test
    void uncenteredOffsetsAreRelativeToTheTopLeftCorner() {
        TitlePosition position = TitlePosition.of(SCREEN_WIDTH, SCREEN_HEIGHT, 12, 34, false, 100, 2f);

        assertEquals(new TitlePosition(12f, 34f), position);
    }

    @Test
    void oddScreenSizesKeepTheHalfPixel() {
        TitlePosition position = TitlePosition.of(427, 241, 0, 0, true, 0, 1f);

        assertEquals(new TitlePosition(213.5f, 120.5f), position);
    }
}
