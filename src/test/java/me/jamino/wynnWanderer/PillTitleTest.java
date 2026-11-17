package me.jamino.wynnWanderer;

/**
 * Checks the city titles against the advances of the pill font they were made for, so this runs without the
 * Wynncraft resource pack. {@link me.jamino.wynnWanderer.pack.WynncraftPackTest} checks that the live pack still
 * has these advances.
 */
public class PillTitleTest extends AbstractPillTitleTest {
    // i is the only letter that is not 5 pixels wide
    private static final int NARROW_LETTER = 0xE008;

    @Override
    protected int advance(int codePoint) {
        return expectedAdvance(codePoint);
    }

    /**
     * The advances of the glyphs of the pill font that the titles use.
     */
    public static int expectedAdvance(int codePoint) {
        if (isSpace(codePoint)) return codePoint - ZERO_WIDTH_SPACE;
        if (codePoint == LEFT_EDGE) return 3;
        if (codePoint == RIGHT_EDGE) return 2;
        if (isLetter(codePoint)) return letterAdvance(codePoint);
        // Backgrounds are a pixel wider than the advance of their letter, so that they connect to the next one
        // after the space of -1 that follows each of them
        if (codePoint == SPACE_BACKGROUND) return letterAdvance(' ') + 1;
        if (codePoint == HYPHEN_BACKGROUND) return letterAdvance(HYPHEN) + 1;
        if (isBackground(codePoint)) return letterAdvance(codePoint - LETTER_BACKGROUND_OFFSET) + 1;
        throw new IllegalArgumentException("Not a glyph of the city titles: U+" + Integer.toHexString(codePoint));
    }

    private static int letterAdvance(int letter) {
        return letter == NARROW_LETTER || letter == ' ' ? 4 : 6;
    }
}
