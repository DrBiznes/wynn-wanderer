package me.jamino.wynnWanderer.features;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TitleColorTest {
    @Test
    void contrastingColorIsReadableOnTheColor() {
        assertEquals(TitleColor.BLACK, TitleColor.contrasting(TitleColor.WHITE));
        assertEquals(TitleColor.WHITE, TitleColor.contrasting(TitleColor.BLACK));
        // Yellow is bright, dark red is not
        assertEquals(TitleColor.BLACK, TitleColor.contrasting(0xE5E533));
        assertEquals(TitleColor.WHITE, TitleColor.contrasting(0x993333));
    }

    @Test
    void shadowIsADarkerShadeOfTheSameHue() {
        int shadow = TitleColor.shadowOf(0xFFCC00);
        // Darker, but not black, and still yellowish: red and green stay well above blue
        assertTrue(shadow != TitleColor.BLACK);
        assertTrue(((shadow >> 16) & 0xFF) < 0xFF && ((shadow >> 8) & 0xFF) < 0xCC);
        assertTrue(((shadow >> 16) & 0xFF) > 0 && ((shadow >> 8) & 0xFF) > 0);
        assertEquals(0, shadow & 0xFF);
        // No alpha bits are added
        assertEquals(0, shadow >>> 24);
        // Shades of grey stay grey
        int grey = TitleColor.shadowOf(0xFFFFFF);
        assertEquals((grey >> 16) & 0xFF, grey & 0xFF);
        assertTrue((grey & 0xFF) < 0xFF);
    }

    @Test
    void parsesHexColors() {
        assertEquals(0xFFCC00, TitleColor.parse("ffcc00", TitleColor.WHITE));
        assertEquals(0xFFCC00, TitleColor.parse("FFCC00", TitleColor.WHITE));
        assertEquals(0x336699, TitleColor.parse("#336699", TitleColor.WHITE));
        assertEquals(0x336699, TitleColor.parse(" 336699 ", TitleColor.WHITE));
        assertEquals(0x000000, TitleColor.parse("000000", TitleColor.WHITE));
    }

    @Test
    void invalidColorsUseTheFallback() {
        assertEquals(0x123456, TitleColor.parse(null, 0x123456));
        assertEquals(0x123456, TitleColor.parse("", 0x123456));
        assertEquals(0x123456, TitleColor.parse("not a color", 0x123456));
        assertEquals(0x123456, TitleColor.parse("fff", 0x123456));
        assertEquals(0x123456, TitleColor.parse("ffcc00ff", 0x123456));
        // Would overflow or be negative if parsed as a number without validation
        assertEquals(0x123456, TitleColor.parse("-fcc00", 0x123456));
    }

    @Test
    void recognizesHexColors() {
        assertTrue(TitleColor.isHex("a3c5e0"));
        assertFalse(TitleColor.isHex(null));
        assertFalse(TitleColor.isHex("#a3c5e0"));
        assertFalse(TitleColor.isHex("wynn_wanderer.territory.ragni.color"));
    }

    @Test
    void combinesAlphaAndColor() {
        assertEquals(0xFFFFCC00, TitleColor.withAlpha(0xFFCC00, 255));
        assertEquals(0x80FFCC00, TitleColor.withAlpha(0xFFCC00, 128));
        assertEquals(0x00FFCC00, TitleColor.withAlpha(0xFFCC00, 0));
        // Alpha is clamped and any alpha already in the color is replaced
        assertEquals(0xFFFFCC00, TitleColor.withAlpha(0xFFCC00, 999));
        assertEquals(0x00FFCC00, TitleColor.withAlpha(0xFFCC00, -5));
        assertEquals(0x10FFCC00, TitleColor.withAlpha(0xAAFFCC00, 0x10));
    }
}
