package me.jamino.wynnWanderer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import me.jamino.wynnWanderer.config.WynnWandererConfig;
import me.jamino.wynnWanderer.features.SignificantTerritoryManager;
import me.jamino.wynnWanderer.features.TitleColor;
import me.jamino.wynnWanderer.features.TitleResolver;
import me.jamino.wynnWanderer.wynntils.TerritoryTitleOverlay;
import me.jamino.wynnWanderer.wynntils.WynnWandererFeature;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import org.junit.jupiter.api.Test;

class LangFileTest {
    private static final String CONFIG_PREFIX = "text.autoconfig.wynn-wanderer.";
    private static final String OPTION_PREFIX = CONFIG_PREFIX + "option.";

    private final Map<String, String> lang = TestResources.readLang();

    @Test
    void everySignificantTerritoryHasATitleSubtitleAndColor() {
        for (String territory : SignificantTerritoryManager.SIGNIFICANT_TERRITORIES) {
            String key = SignificantTerritoryManager.getTranslationKey(territory);

            assertFalse(lang.getOrDefault(key + ".title", "").isBlank(), "Missing title for " + territory);
            assertFalse(lang.getOrDefault(key + ".subtitle", "").isBlank(), "Missing subtitle for " + territory);
            assertTrue(TitleColor.isHex(lang.get(key + ".color")), "Missing or invalid color for " + territory);
        }
    }

    @Test
    void everyTerritoryTranslationBelongsToASignificantTerritory() {
        Set<String> known = new TreeSet<>(Set.of(
                TitleResolver.ENTERING_TITLE_KEY,
                TitleResolver.ENTERING_SUBTITLE_KEY,
                TitleResolver.SIGNIFICANT_TITLE_KEY,
                TitleResolver.SIGNIFICANT_SUBTITLE_KEY));
        for (String territory : SignificantTerritoryManager.SIGNIFICANT_TERRITORIES) {
            String key = SignificantTerritoryManager.getTranslationKey(territory);
            known.addAll(List.of(key + ".title", key + ".subtitle", key + ".color"));
        }

        Set<String> actual = new TreeSet<>();
        for (String key : lang.keySet()) {
            if (key.startsWith(TitleResolver.KEY_PREFIX)) {
                actual.add(key);
            }
        }

        assertEquals(known, actual);
    }

    @Test
    void genericTitlesHaveAPlaceholderForTheTerritoryName() {
        assertTrue(lang.get(TitleResolver.ENTERING_TITLE_KEY).contains("%s"));
        assertTrue(lang.get(TitleResolver.SIGNIFICANT_TITLE_KEY).contains("%s"));
    }

    /**
     * Wynntils derives these keys from the class names of the feature and overlay, and refuses to
     * start in a development environment if they are missing.
     */
    @Test
    void wynntilsFeatureAndOverlayAreTranslated() {
        String featureKey = "feature.wynntils." + lowerCamel(WynnWandererFeature.class, "Feature");
        String overlayKey = featureKey + ".overlay." + lowerCamel(TerritoryTitleOverlay.class, "Overlay");

        assertFalse(lang.getOrDefault(featureKey + ".name", "").isBlank(), featureKey + ".name");
        assertFalse(lang.getOrDefault(featureKey + ".description", "").isBlank(), featureKey + ".description");
        assertFalse(lang.getOrDefault(overlayKey + ".name", "").isBlank(), overlayKey + ".name");
    }

    @Test
    void everyConfigOptionHasANameAndItsTooltips() {
        assertTrue(lang.containsKey(CONFIG_PREFIX + "title"));

        List<String> expected = new ArrayList<>();
        collectOptionKeys(WynnWandererConfig.class, OPTION_PREFIX, expected);

        List<String> missing = new ArrayList<>(expected);
        missing.removeAll(lang.keySet());
        assertEquals(List.of(), missing, "Config options without a translation");
    }

    @Test
    void thereAreNoTranslationsForRemovedConfigOptions() {
        List<String> expected = new ArrayList<>();
        collectOptionKeys(WynnWandererConfig.class, OPTION_PREFIX, expected);

        Set<String> stale = new TreeSet<>();
        for (String key : lang.keySet()) {
            if (key.startsWith(OPTION_PREFIX) && !expected.contains(key)) {
                stale.add(key);
            }
        }

        assertEquals(Set.of(), stale, "Translations for config options that do not exist");
    }

    private static void collectOptionKeys(Class<?> configClass, String prefix, List<String> keys) {
        for (Field field : configClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;

            String key = prefix + field.getName();
            keys.add(key);

            ConfigEntry.Gui.Tooltip tooltip = field.getAnnotation(ConfigEntry.Gui.Tooltip.class);
            if (tooltip != null) {
                if (tooltip.count() == 1) {
                    keys.add(key + ".@Tooltip");
                } else {
                    for (int i = 0; i < tooltip.count(); i++) {
                        keys.add(key + ".@Tooltip[" + i + "]");
                    }
                }
            }

            if (field.isAnnotationPresent(ConfigEntry.Gui.CollapsibleObject.class)) {
                collectOptionKeys(field.getType(), key + ".", keys);
            }
        }
    }

    private static String lowerCamel(Class<?> clazz, String suffix) {
        String name = clazz.getSimpleName().replace(suffix, "");
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}
