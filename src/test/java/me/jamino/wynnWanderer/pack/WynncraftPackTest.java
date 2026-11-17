package me.jamino.wynnWanderer.pack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import me.jamino.wynnWanderer.AbstractPillTitleTest;
import me.jamino.wynnWanderer.PillTitleTest;
import me.jamino.wynnWanderer.features.SignificantTerritoryManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Checks the mod against the Wynncraft resource pack that players get right now, to catch updates of the pack
 * that break the city titles. Runs the {@link AbstractPillTitleTest pill layout checks} with the glyphs of the
 * pack, and checks that the fonts and glyphs the mod uses are still there.
 * <p>
 * Skipped when there is no pack, see {@link WynncraftPack} for where it comes from. Run with
 * {@code gradlew testWynncraftPack}.
 */
@Tag("wynncraft-pack")
class WynncraftPackTest extends AbstractPillTitleTest {
    private static WynncraftPack pack;
    private static PackFont pill;
    private static PackFont smallCaps;

    @BeforeAll
    static void loadPack() {
        pack = WynncraftPack.get();
        pill = pack.font(PILL_FONT);
        smallCaps = pack.font(SMALL_CAPS_FONT);
    }

    @Override
    protected int advance(int codePoint) {
        assertNotNull(pill, "The Wynncraft resource pack has no " + PILL_FONT + " font");
        return pill.advance(codePoint).orElseThrow(() -> new AssertionError(
                "The " + PILL_FONT + " font of the Wynncraft resource pack has no glyph for U+" + Integer.toHexString(codePoint)));
    }

    @Test
    void minecraftLoadsThePack() {
        assertTrue(WynncraftPack.supports(pack.metadata().getAsJsonObject("pack"), WynncraftPack.PACK_FORMAT),
                "The Wynncraft resource pack does not support pack format " + WynncraftPack.PACK_FORMAT
                        + " anymore, which the Minecraft version of the mod uses. pack.mcmeta: " + pack.metadata());
    }

    @Test
    void thePackHasTheFontsOfTheTitles() {
        assertNotNull(pill, "The Wynncraft resource pack has no " + PILL_FONT + " font, city titles would be unreadable");
        assertNotNull(smallCaps, "The Wynncraft resource pack has no " + SMALL_CAPS_FONT + " font, subtitles would be unreadable");
    }

    @Test
    void theGlyphsOfTheCityTitlesHaveNotChanged() {
        // Lists every glyph that changed at once, the layout checks only tell which title broke first
        Map<String, String> changed = new TreeMap<>();
        for (String territory : SignificantTerritoryManager.SIGNIFICANT_TERRITORIES) {
            for (int c : drawnCharacters(title(territory))) {
                String glyph = "U+" + Integer.toHexString(c).toUpperCase();
                if (pill == null || !pill.has(c)) {
                    changed.put(glyph, "missing (used by " + territory + ")");
                    continue;
                }
                int expected = PillTitleTest.expectedAdvance(c);
                int actual = pill.advance(c).getAsInt();
                if (expected != actual) {
                    changed.put(glyph, "advance " + expected + " -> " + actual + " (used by " + territory + ")");
                }
            }
        }
        assertEquals(Map.of(), changed, "Glyphs of the pill font that changed in the Wynncraft resource pack " + pack.sha1());
    }

    @Test
    void theSmallCapsFontHasEveryLetterOfTheSubtitles() {
        assertNotNull(smallCaps, "The Wynncraft resource pack has no " + SMALL_CAPS_FONT + " font");
        List<String> missing = new ArrayList<>();
        for (String territory : SignificantTerritoryManager.SIGNIFICANT_TERRITORIES) {
            for (int c : drawnCharacters(subtitle(territory))) {
                if (!smallCaps.has(c)) {
                    missing.add("'" + Character.toString(c) + "' in the subtitle of " + territory);
                }
            }
        }
        assertEquals(List.of(), missing, "Characters missing from " + SMALL_CAPS_FONT);
    }
}
