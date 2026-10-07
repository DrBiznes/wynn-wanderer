package me.jamino.wynnWanderer.features;

import java.util.function.UnaryOperator;
import me.jamino.wynnWanderer.config.WynnWandererConfig;

/**
 * Picks the translation keys and color used to display the title of a territory.
 */
public final class TitleResolver {
    public static final String KEY_PREFIX = "wynn_wanderer.territory.";
    public static final String ENTERING_TITLE_KEY = KEY_PREFIX + "entering.title";
    public static final String ENTERING_SUBTITLE_KEY = KEY_PREFIX + "entering.subtitle";
    public static final String SIGNIFICANT_TITLE_KEY = KEY_PREFIX + "significant.title";
    public static final String SIGNIFICANT_SUBTITLE_KEY = KEY_PREFIX + "significant.subtitle";

    private TitleResolver() {}

    /**
     * The translation keys (formatted with the territory name) and RGB colors of a title.
     *
     * @param color Color of the title
     * @param textColor Second color of the title, see {@link TitleText}
     * @param subtitleColor Color of the subtitle
     */
    public record ResolvedTitle(
            String territoryName,
            String titleKey,
            String subtitleKey,
            int color,
            int textColor,
            int subtitleColor,
            boolean significant) {}

    /**
     * @param territoryName Friendly name of the territory
     * @param config The territory title settings
     * @param translations Looks up a translation key, returning null if there is no translation for it.
     *                     Resource packs can override these.
     */
    public static ResolvedTitle resolve(
            String territoryName, WynnWandererConfig.TerritoryTitlesConfig config, UnaryOperator<String> translations) {
        int textColor = TitleColor.parse(config.appearance.textColor, TitleColor.WHITE);

        if (!SignificantTerritoryManager.isSignificant(territoryName)) {
            // For regular territories, use the generic "Entering X" title
            return new ResolvedTitle(
                    territoryName,
                    ENTERING_TITLE_KEY,
                    ENTERING_SUBTITLE_KEY,
                    textColor,
                    TitleColor.contrasting(textColor),
                    TitleColor.WHITE,
                    false);
        }

        // Significant territories have territory-specific keys for custom styling
        WynnWandererConfig.TerritoryTitlesConfig.SignificantTerritoryConfig significantConfig =
                config.significantTerritories;
        String territoryKey = SignificantTerritoryManager.getTranslationKey(territoryName);
        String titleKey = territoryKey + ".title";
        String subtitleKey = territoryKey + ".subtitle";

        int color = textColor;
        // Without colors of their own, the second color has to stay readable on the first one
        String customTextColor = null;
        String customSubtitleColor = null;
        if (significantConfig.useCustomColors) {
            customTextColor = translations.apply(territoryKey + ".text_color");
            customSubtitleColor = translations.apply(territoryKey + ".subtitle_color");

            String customColor = translations.apply(territoryKey + ".color");
            if (TitleColor.isHex(customColor)) {
                color = TitleColor.parse(customColor, textColor);
            } else if (significantConfig.useEnhancedStyling) {
                // Fallback to default significant color
                color = TitleColor.parse(significantConfig.defaultColor, textColor);
            }
        }

        // If we are unable to find translations, fall back to the significant format
        if (translations.apply(titleKey) == null) {
            titleKey = SIGNIFICANT_TITLE_KEY;
        }
        if (translations.apply(subtitleKey) == null) {
            subtitleKey = SIGNIFICANT_SUBTITLE_KEY;
        }

        return new ResolvedTitle(
                territoryName,
                titleKey,
                subtitleKey,
                color,
                TitleColor.parse(customTextColor, TitleColor.contrasting(color)),
                TitleColor.parse(customSubtitleColor, TitleColor.WHITE),
                true);
    }
}
