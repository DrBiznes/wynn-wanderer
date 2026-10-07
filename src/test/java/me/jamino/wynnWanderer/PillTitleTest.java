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
 * (minecraft:banner/pill), to check that the glyphs line up. The advances are the ones of the pack.
 * <p>
 * A pill is a background made of one glyph per letter, which has the letter cut out of it, followed
 * by the letters themselves.
 */
class PillTitleTest {
    private static final int LEFT_EDGE = 0xE060;
    private static final int SPACE_BACKGROUND = 0xE061;
    private static final int RIGHT_EDGE = 0xE062;
    private static final int LEFT_EDGE_WIDTH = 2;

    private static final int FIRST_LETTER = 0xE000;
    private static final int LAST_LETTER = 0xE019;
    // i is the only letter that is not 5 pixels wide
    private static final int NARROW_LETTER = 0xE008;
    private static final int HYPHEN = 0xE071;
    // Background glyphs come this far after the letter that is cut out of them
    private static final int LETTER_BACKGROUND_OFFSET = 0x30;
    private static final int HYPHEN_BACKGROUND = 0xE081;

    // The space font of the pack has a space of every width around this code point
    private static final int ZERO_WIDTH_SPACE = 0xD0000;
    private static final int WIDEST_SPACE = 16384;

    private final Map<String, String> lang = TestResources.readLang();

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
            }
        }
    }

    @Test
    void theBackgroundIsASingleShape() {
        for (String territory : SignificantTerritoryManager.SIGNIFICANT_TERRITORIES) {
            Pill pill = layout(title(territory));

            // Neither gaps, nor glyphs that overlap and would be darker while the title is fading
            int x = pill.leftEdge + LEFT_EDGE_WIDTH;
            for (Glyph cell : pill.cells) {
                assertEquals(x, cell.x, "Background of " + territory);
                x += backgroundWidth(cell.codePoint);
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
            assertEquals(pill.rightEdge + 1, pill.width, territory);
        }
    }

    @Test
    void cityTitlesAreTextOnABackgroundInThePillFont() {
        for (String territory : SignificantTerritoryManager.SIGNIFICANT_TERRITORIES) {
            List<TitleText.Segment> segments = TitleText.split(title(territory), 0, 0);

            assertTrue(TitleText.hasBackground(segments), territory);
            for (TitleText.Segment segment : segments) {
                assertEquals("minecraft:banner/pill", segment.font(), territory);
            }
        }
    }

    @Test
    void subtitlesAreReadableTextInTheSmallCapsFont() {
        for (String territory : SignificantTerritoryManager.SIGNIFICANT_TERRITORIES) {
            List<TitleText.Segment> segments = TitleText.split(lang.get(key(territory) + ".subtitle"), 0, 0);

            assertEquals(1, segments.size(), territory);
            assertEquals("minecraft:language/five", segments.get(0).font(), territory);
            assertTrue(segments.get(0).text().matches("[A-Za-z' -]+"), segments.get(0).text());
        }
    }

    private String title(String territory) {
        return lang.get(key(territory) + ".title");
    }

    private static String key(String territory) {
        return SignificantTerritoryManager.getTranslationKey(territory);
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

    private static Pill layout(String title) {
        Pill pill = new Pill();
        int x = 0;

        for (int i = 0; i < title.length(); ) {
            int c = title.codePointAt(i);
            i += Character.charCount(c);

            if (c == '§') {
                // Colors and fonts take no space
                i = title.charAt(i) == '{' ? title.indexOf('}', i) + 1 : i + 1;
            } else if (c == LEFT_EDGE) {
                pill.leftEdge = x;
                x += LEFT_EDGE_WIDTH + 1;
            } else if (c == RIGHT_EDGE) {
                pill.rightEdge = x;
                x += 2;
            } else if (Math.abs(c - ZERO_WIDTH_SPACE) <= WIDEST_SPACE) {
                x += c - ZERO_WIDTH_SPACE;
            } else if (isLetter(c)) {
                pill.letters.add(new Glyph(c, x));
                x += letterAdvance(c);
            } else if (c == SPACE_BACKGROUND || c == HYPHEN_BACKGROUND || isLetter(c - LETTER_BACKGROUND_OFFSET)) {
                pill.cells.add(new Glyph(c, x));
                x += backgroundWidth(c) + 1;
            } else {
                fail("Unexpected character U+" + Integer.toHexString(c) + " in " + title);
            }
        }

        pill.width = x;
        return pill;
    }

    private static boolean isLetter(int codePoint) {
        return (codePoint >= FIRST_LETTER && codePoint <= LAST_LETTER) || codePoint == HYPHEN || codePoint == ' ';
    }

    private static int letterAdvance(int letter) {
        return letter == NARROW_LETTER || letter == ' ' ? 4 : 6;
    }

    private static int backgroundOf(int letter) {
        if (letter == ' ') return SPACE_BACKGROUND;
        if (letter == HYPHEN) return HYPHEN_BACKGROUND;
        return letter + LETTER_BACKGROUND_OFFSET;
    }

    /**
     * Backgrounds are as wide as the advance of their letter, so that they connect to the next one.
     */
    private static int backgroundWidth(int background) {
        if (background == SPACE_BACKGROUND) return letterAdvance(' ');
        if (background == HYPHEN_BACKGROUND) return letterAdvance(HYPHEN);
        return letterAdvance(background - LETTER_BACKGROUND_OFFSET);
    }
}
