package me.jamino.wynnWanderer.features;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import me.jamino.wynnWanderer.features.TitleText.Segment;
import org.junit.jupiter.api.Test;

class TitleTextTest {
    private static final int COLOR = 0x993333;
    private static final int TEXT_COLOR = 0xE5E533;
    private static final String PILL_FONT = "minecraft:banner/pill";

    @Test
    void plainTextIsASingleSegmentInTheTitleColor() {
        assertEquals(List.of(background("Entering Nivla Woods", null)), split("Entering Nivla Woods"));
    }

    @Test
    void switchesBetweenTheTitleAndTextColor() {
        assertEquals(
                List.of(background("(", null), foreground("A", null), background(")", null)),
                split("(§tA§r)"));
    }

    @Test
    void colorCodesAtTheEdgesDoNotAddEmptySegments() {
        assertEquals(List.of(foreground("A", null)), split("§r§tA§r"));
        assertEquals(List.of(), split(""));
    }

    @Test
    void switchesBetweenFonts() {
        assertEquals(
                List.of(background("A", PILL_FONT), background("B", "wynntils:five"), background("C", null)),
                split("§{minecraft:banner/pill}A§{ wynntils:five }B§{}C"));
    }

    @Test
    void theFontIsKeptWhenTheColorChanges() {
        assertEquals(
                List.of(background("A", PILL_FONT), foreground("B", PILL_FONT), background("C", PILL_FONT)),
                split("§{minecraft:banner/pill}A§tB§rC"));
    }

    @Test
    void anUnfinishedFontIsLeftInTheText() {
        assertEquals(List.of(background("A§{minecraft:default", null)), split("A§{minecraft:default"));
    }

    @Test
    void otherFormattingCodesAreLeftForMinecraft() {
        assertEquals(List.of(background("§lBold §cred", null)), split("§lBold §cred"));
        // A lone formatting character at the end is not a code
        assertEquals(List.of(background("A§", null)), split("A§"));
    }

    @Test
    void charactersOutsideOfTheBasicPlaneAreKeptWhole() {
        // The negative spaces of the Wynncraft space font
        String space = new String(Character.toChars(0xCFFFF));

        assertEquals(
                List.of(background("A" + space, null), foreground(space + "B", null)),
                split("A" + space + "§t" + space + "B"));
    }

    @Test
    void onlyTitlesWithTextInTheTextColorHaveABackground() {
        assertTrue(TitleText.hasBackground(split("(§tA")));
        assertFalse(TitleText.hasBackground(split("Entering Nivla Woods")));
        assertFalse(TitleText.hasBackground(split("§cRed§r title")));
    }

    private static List<Segment> split(String text) {
        return TitleText.split(text, COLOR, TEXT_COLOR);
    }

    private static Segment background(String text, String font) {
        return new Segment(text, COLOR, false, font);
    }

    private static Segment foreground(String text, String font) {
        return new Segment(text, TEXT_COLOR, true, font);
    }
}
