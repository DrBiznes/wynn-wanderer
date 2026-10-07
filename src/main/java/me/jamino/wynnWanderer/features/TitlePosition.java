package me.jamino.wynnWanderer.features;

/**
 * Where a line of text is drawn on screen, in scaled GUI pixels.
 */
public record TitlePosition(float x, float y) {
    // Height of the backgrounds in the banner fonts of Wynncraft, in pixels of the font
    public static final int BACKGROUND_HEIGHT = 7;

    /**
     * Calculates the position of the top left corner of a line of text.
     *
     * @param screenWidth Scaled width of the screen
     * @param screenHeight Scaled height of the screen
     * @param xOffset Horizontal offset from the config
     * @param yOffset Vertical offset from the config
     * @param centerText If true, the offsets are relative to the center of the screen and the text is
     *                   centered horizontally on that point. Otherwise they are relative to the top left corner.
     * @param textWidth Width of the text before scaling
     * @param size Scale factor of the text
     */
    public static TitlePosition of(
            int screenWidth,
            int screenHeight,
            int xOffset,
            int yOffset,
            boolean centerText,
            int textWidth,
            float size) {
        if (!centerText) {
            // Use direct offsets
            return new TitlePosition(xOffset, yOffset);
        }

        // Move to center of screen, then apply offsets
        float x = screenWidth / 2f + xOffset - (textWidth / 2) * size;
        float y = screenHeight / 2f + yOffset;
        return new TitlePosition(x, y);
    }

    /**
     * The right and bottom edge of the background of a title at this position, rounded down to
     * whole GUI pixels. Only the shadow of the background reaches past them.
     *
     * @param textWidth Width of the text before scaling
     * @param size Scale factor of the text
     */
    public BackgroundEdges backgroundEdges(int textWidth, float size) {
        return new BackgroundEdges(
                (int) Math.floor(x + textWidth * size), (int) Math.floor(y + BACKGROUND_HEIGHT * size));
    }

    public record BackgroundEdges(int right, int bottom) {}
}
