package me.jamino.wynnWanderer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import me.jamino.wynnWanderer.features.SignificantTerritoryManager;
import me.jamino.wynnWanderer.features.TitleText;
import org.junit.jupiter.api.Test;

/**
 * Lays out the city titles the way Minecraft does with the pill font of the Wynncraft resource pack
 * (minecraft:banner/pill), to check that the glyphs line up. How far each glyph moves the text along comes from
 * {@link #advance(int)}: {@link PillTitleTest} uses the advances the titles were made for, and
 * {@link me.jamino.wynnWanderer.pack.WynncraftPackTest} the ones of the live pack.
 * <p>
 * A pill is a background made of one glyph per letter, which has the letter cut out of it, followed
 * by the letters themselves.
 */
public abstract class AbstractPillTitleTest {
    public static final String PILL_FONT = "minecraft:banner/pill";
    public static final String SMALL_CAPS_FONT = "minecraft:language/five";

    protected static final int LEFT_EDGE = 0xE060;
    protected static final int SPACE_BACKGROUND = 0xE061;
    protected static final int RIGHT_EDGE = 0xE062;

    protected static final int FIRST_LETTER = 0xE000;
    protected static final int LAST_LETTER = 0xE019;
    protected static final int HYPHEN = 0xE071;
    // Background glyphs come this far after the letter that is cut out of them
    protected static final int LETTER_BACKGROUND_OFFSET = 0x30;
    protected static final int HYPHEN_BACKGROUND = 0xE081;

    // The space font of the pack has a space of every width around this code point
    protected static final int ZERO_WIDTH_SPACE = 0xD0000;
    protected static final int WIDEST_SPACE = 16384;

    protected final Map<String, String> lang = TestResources.readLang();

    /**
     * How many pixels the glyph moves the text along in the pill font: its width plus a pixel of spacing.
     */
    protected abstract int advance(int codePoint);

    /** Pixels the glyph covers. */
    private int width(int codePoint) {
        return advance(codePoint) - 1;
    }

    @Test
    void everyCityTitleSpellsItsName() {
        for (String territory : SignificantTerritoryManager.SIGNIFICANT_TERRITORIES) {
            assertEquals(territory.toUpperCase(Locale.ROOT), layout(title(territory)).letters(), territory);
        }
    }

    @Test
    void everyLetterIsDrawnInTheHoleOfItsBackground() {
        for (String territory : SignificantTerritoryManager.SIGNIFICANT_TERRITORIES) {
            Pill pill = layout(title(territory));

            assertEquals(pill.cells.size(), pill.letters.size(), "Background of " + territory);
            for (int i = 0; i < pill.cells.size(); i++) {
                Glyph cell = pill.cells.get(i);
                Glyph letter = pill.letters.get(i);

                assertEquals(backgroundOf(letter.codePoint), cell.codePoint, "Background " + i + " of " + territory);
                assertEquals(cell.x, letter.x, "Position of letter " + i + " of " + territory);
                assertEquals(width(cell.codePoint), advance(letter.codePoint),
                        "The hole of background " + i + " of " + territory + " is not as wide as its letter");
            }
        }
    }

    @Test
    void theBackgroundIsASingleShape() {
        for (String territory : SignificantTerritoryManager.SIGNIFICANT_TERRITORIES) {
            Pill pill = layout(title(territory));

            // Neither gaps, nor glyphs that overlap and would be darker while the title is fading
            int x = pill.leftEdge + width(LEFT_EDGE);
            for (Glyph cell : pill.cells) {
                assertEquals(x, cell.x, "Background of " + territory);
                x += width(cell.codePoint);
            }
            assertEquals(x, pill.rightEdge, "Right edge of " + territory);
        }
    }

    @Test
    void theWidthOfTheTextIsTheWidthOfThePill() {
        // Titles are centered on the width of their text, which ends with the letters and not the pill
        for (String territory : SignificantTerritoryManager.SIGNIFICANT_TERRITORIES) {
            Pill pill = layout(title(territory));

            assertEquals(0, pill.leftEdge, territory);
            assertEquals(pill.rightEdge + width(RIGHT_EDGE), pill.width, territory);
        }
    }

    @Test
    void cityTitlesAreTextOnABackgroundInThePillFont() {
        for (String territory : SignificantTerritoryManager.SIGNIFICANT_TERRITORIES) {
            List<TitleText.Segment> segments = TitleText.split(title(territory), 0, 0);

            assertTrue(TitleText.hasBackground(segments), territory);
            for (TitleText.Segment segment : segments) {
                assertEquals(PILL_FONT, segment.font(), territory);
            }
        }
    }

    @Test
    void subtitlesAreReadableTextInTheSmallCapsFont() {
        for (String territory : SignificantTerritoryManager.SIGNIFICANT_TERRITORIES) {
            List<TitleText.Segment> segments = TitleText.split(subtitle(territory), 0, 0);

            assertEquals(1, segments.size(), territory);
            assertEquals(SMALL_CAPS_FONT, segments.get(0).font(), territory);
            assertTrue(segments.get(0).text().matches("[A-Za-z' -]+"), segments.get(0).text());
        }
    }

    protected String title(String territory) {
        return lang.get(key(territory) + ".title");
    }

    protected String subtitle(String territory) {
        return lang.get(key(territory) + ".subtitle");
    }

    private static String key(String territory) {
        return SignificantTerritoryManager.getTranslationKey(territory);
    }

    /**
     * The characters of the text that are drawn, without the color and font codes.
     */
    protected static List<Integer> drawnCharacters(String text) {
        List<Integer> characters = new ArrayList<>();
        for (int i = 0; i < text.length(); ) {
            int c = text.codePointAt(i);
            i += Character.charCount(c);

            if (c == '§') {
                // Colors and fonts take no space
                i = text.charAt(i) == '{' ? text.indexOf('}', i) + 1 : i + 1;
            } else {
                characters.add(c);
            }
        }
        return characters;
    }

    protected static boolean isSpace(int codePoint) {
        return Math.abs(codePoint - ZERO_WIDTH_SPACE) <= WIDEST_SPACE;
    }

    private record Glyph(int codePoint, int x) {}

    private static final class Pill {
        private final List<Glyph> cells = new ArrayList<>();
        private final List<Glyph> letters = new ArrayList<>();
        private int leftEdge = -1;
        private int rightEdge = -1;
        private int width;

        private String letters() {
            StringBuilder name = new StringBuilder();
            for (Glyph letter : letters) {
                if (letter.codePoint == ' ') {
                    name.append(' ');
                } else if (letter.codePoint == HYPHEN) {
                    name.append('-');
                } else {
                    name.append((char) ('A' + letter.codePoint - FIRST_LETTER));
                }
            }
            return name.toString();
        }
    }

    private Pill layout(String title) {
        Pill pill = new Pill();
        int x = 0;

        for (int c : drawnCharacters(title)) {
            if (c == LEFT_EDGE) {
                pill.leftEdge = x;
            } else if (c == RIGHT_EDGE) {
                pill.rightEdge = x;
            } else if (isSpace(c)) {
                // Moves the text without drawing anything
            } else if (isLetter(c)) {
                pill.letters.add(new Glyph(c, x));
            } else if (isBackground(c)) {
                pill.cells.add(new Glyph(c, x));
            } else {
                fail("Unexpected character U+" + Integer.toHexString(c) + " in " + title);
            }
            x += advance(c);
        }

        pill.width = x;
        return pill;
    }

    protected static boolean isLetter(int codePoint) {
        return (codePoint >= FIRST_LETTER && codePoint <= LAST_LETTER) || codePoint == HYPHEN || codePoint == ' ';
    }

    protected static boolean isBackground(int codePoint) {
        return codePoint == SPACE_BACKGROUND || codePoint == HYPHEN_BACKGROUND
                || (codePoint >= FIRST_LETTER + LETTER_BACKGROUND_OFFSET && codePoint <= LAST_LETTER + LETTER_BACKGROUND_OFFSET);
    }

    private static int backgroundOf(int letter) {
        if (letter == ' ') return SPACE_BACKGROUND;
        if (letter == HYPHEN) return HYPHEN_BACKGROUND;
        return letter + LETTER_BACKGROUND_OFFSET;
    }
}
