package me.jamino.wynnWanderer.features;

/**
 * Helpers for the RGB hex colors used in the config and language files.
 */
public final class TitleColor {
    public static final int WHITE = 0xFFFFFF;
    public static final int BLACK = 0x000000;
    // How much of the brightness of a color its shadow keeps
    private static final float SHADOW_BRIGHTNESS = 0.3f;

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
     * @return Black or white, whichever is easier to read on top of the color
     */
    public static int contrasting(int rgb) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        // Perceived brightness
        return (red * 299 + green * 587 + blue * 114) / 1000 >= 128 ? BLACK : WHITE;
    }

    /**
     * @return A darker shade of the color with the same hue, for the shadow of text in that color
     */
    public static int shadowOf(int rgb) {
        float[] hsb = java.awt.Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, null);
        return java.awt.Color.HSBtoRGB(hsb[0], hsb[1], hsb[2] * SHADOW_BRIGHTNESS) & 0xFFFFFF;
    }

    /**
     * Combines an RGB color with an alpha value (0-255) into an ARGB color.
     */
    public static int withAlpha(int rgb, int alpha) {
        return (Math.clamp(alpha, 0, 255) << 24) | (rgb & 0xFFFFFF);
    }
}
