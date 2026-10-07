package me.jamino.wynnWanderer.features;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits the text of a title into its differently styled parts.
 * <p>
 * Titles and subtitles in the language files can switch between the two colors of a territory and
 * between fonts, which is how the Wynncraft pill glyphs get a background of one color and letters
 * of another:
 * <ul>
 *   <li>{@code §r} switches to the title color (the {@code .color} key)</li>
 *   <li>{@code §t} switches to the text color (the {@code .text_color} key), for text that is drawn
 *       on top of a background</li>
 *   <li>{@code §{minecraft:banner/pill}} switches to that font, and {@code §{}} back to the default</li>
 * </ul>
 * All other formatting codes are left in the text for Minecraft to handle. As the font is part of
 * the text, a resource pack that replaces a title does not have to know which font it was in.
 */
public final class TitleText {
    private static final char FORMAT_CHAR = '§';
    private static final char TITLE_COLOR_CODE = 'r';
    private static final char TEXT_COLOR_CODE = 't';
    private static final char FONT_START = '{';
    private static final char FONT_END = '}';

    private TitleText() {}

    /**
     * A run of text that is drawn in a single RGB color and font.
     *
     * @param foreground true for text in the text color, which is drawn on top of a background
     * @param font Id of the font, or null for the default font
     */
    public record Segment(String text, int color, boolean foreground, String font) {}

    /**
     * @param text The translated title
     * @param color RGB color the title starts in, and returns to with {@code §r}
     * @param textColor RGB color used after {@code §t}
     */
    public static List<Segment> split(String text, int color, int textColor) {
        List<Segment> segments = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean foreground = false;
        String font = null;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == FORMAT_CHAR && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));
                int fontEnd = code == FONT_START ? text.indexOf(FONT_END, i + 2) : -1;

                if (code == TITLE_COLOR_CODE || code == TEXT_COLOR_CODE || fontEnd >= 0) {
                    // Start a new segment, which also resets any other formatting
                    if (!current.isEmpty()) {
                        segments.add(new Segment(current.toString(), foreground ? textColor : color, foreground, font));
                        current.setLength(0);
                    }

                    if (fontEnd >= 0) {
                        String id = text.substring(i + 2, fontEnd).trim();
                        font = id.isEmpty() ? null : id;
                        i = fontEnd;
                    } else {
                        foreground = code == TEXT_COLOR_CODE;
                        i++;
                    }
                    continue;
                }
            }
            current.append(c);
        }

        if (!current.isEmpty()) {
            segments.add(new Segment(current.toString(), foreground ? textColor : color, foreground, font));
        }
        return segments;
    }

    /**
     * @return true if the title is made of a background with text on top of it, like the pills. Only
     *         the background of these gets a shadow.
     */
    public static boolean hasBackground(List<Segment> segments) {
        return segments.stream().anyMatch(Segment::foreground);
    }
}
