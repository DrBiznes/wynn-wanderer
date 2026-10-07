package me.jamino.wynnWanderer.features;

/**
 * Helpers for the RGB hex colors used in the config and language files.
 */
public final class TitleColor {
    public static final int WHITE = 0xFFFFFF;

    private TitleColor() {}

    /**
     * @return true if the string is a 6 digit RGB hex color, e.g. "ffcc00"
     */
    public static boolean isHex(String color) {
        return color != null && color.matches("[0-9A-Fa-f]{6}");
    }

    /**
     * Parses an RGB hex color, with or without a leading '#'.
     *
     * @param color Hexadecimal string representation of the color
     * @param fallback Color to use when the string is not a valid hex color
     */
    public static int parse(String color, int fallback) {
        if (color == null) return fallback;

        String hex = color.trim();
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        return isHex(hex) ? Integer.parseInt(hex, 16) : fallback;
    }

    /**
     * Combines an RGB color with an alpha value (0-255) into an ARGB color.
     */
    public static int withAlpha(int rgb, int alpha) {
        return (Math.clamp(alpha, 0, 255) << 24) | (rgb & 0xFFFFFF);
    }
}
