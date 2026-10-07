package me.jamino.wynnWanderer.features;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import me.jamino.wynnWanderer.TestResources;
import me.jamino.wynnWanderer.config.WynnWandererConfig;
import me.jamino.wynnWanderer.features.TitleResolver.ResolvedTitle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TitleResolverTest {
    private final Map<String, String> lang = TestResources.readLang();
    private WynnWandererConfig.TerritoryTitlesConfig config;

    @BeforeEach
    void setUp() {
        config = new WynnWandererConfig().territoryTitles;
    }

    @Test
    void significantTerritoriesUseTheirOwnTitleAndColor() {
        ResolvedTitle title = TitleResolver.resolve("Detlas", config, lang::get);

        assertTrue(title.significant());
        assertEquals("Detlas", title.territoryName());
        assertEquals("wynn_wanderer.territory.detlas.title", title.titleKey());
        assertEquals("wynn_wanderer.territory.detlas.subtitle", title.subtitleKey());
        assertEquals(0x669933, title.color());
    }

    @Test
    void territoryNamesWithSpacesMapToUnderscoredKeys() {
        ResolvedTitle title = TitleResolver.resolve("Corkus City", config, lang::get);

        assertEquals("wynn_wanderer.territory.corkus_city.title", title.titleKey());
        assertEquals(0xC7B350, title.color());
    }

    @Test
    void regularTerritoriesUseTheEnteringTitleAndTextColor() {
        config.appearance.textColor = "abcdef";

        ResolvedTitle title = TitleResolver.resolve("Nivla Woods", config, lang::get);

        assertFalse(title.significant());
        assertEquals("Nivla Woods", title.territoryName());
        assertEquals(TitleResolver.ENTERING_TITLE_KEY, title.titleKey());
        assertEquals(TitleResolver.ENTERING_SUBTITLE_KEY, title.subtitleKey());
        assertEquals(0xABCDEF, title.color());
    }

    @Test
    void customColorsCanBeDisabled() {
        config.appearance.textColor = "abcdef";
        config.significantTerritories.useCustomColors = false;

        assertEquals(0xABCDEF, TitleResolver.resolve("Detlas", config, lang::get).color());
    }

    @Test
    void missingTranslationsFallBackToTheSignificantFormat() {
        ResolvedTitle title = TitleResolver.resolve("Detlas", config, key -> null);

        assertTrue(title.significant());
        assertEquals(TitleResolver.SIGNIFICANT_TITLE_KEY, title.titleKey());
        assertEquals(TitleResolver.SIGNIFICANT_SUBTITLE_KEY, title.subtitleKey());
        // Default significant color
        assertEquals(0xFFCC00, title.color());
    }

    @Test
    void invalidCustomColorFallsBackToTheDefaultSignificantColor() {
        Map<String, String> brokenPack = new HashMap<>(lang);
        brokenPack.put("wynn_wanderer.territory.detlas.color", "green");
        config.significantTerritories.defaultColor = "112233";

        assertEquals(0x112233, TitleResolver.resolve("Detlas", config, brokenPack::get).color());
    }

    @Test
    void withoutEnhancedStylingTheFallbackIsTheTextColor() {
        config.appearance.textColor = "abcdef";
        config.significantTerritories.useEnhancedStyling = false;

        assertEquals(0xABCDEF, TitleResolver.resolve("Detlas", config, key -> null).color());
    }

    @Test
    void invalidConfigColorsFallBackToWhite() {
        config.appearance.textColor = "white";
        config.significantTerritories.defaultColor = "gold";

        assertEquals(TitleColor.WHITE, TitleResolver.resolve("Nivla Woods", config, lang::get).color());
        assertEquals(TitleColor.WHITE, TitleResolver.resolve("Detlas", config, key -> null).color());
    }

    @Test
    void resourcePacksCanOverrideTitles() {
        Map<String, String> pack = new HashMap<>(lang);
        pack.put("wynn_wanderer.territory.ragni.color", "010203");

        ResolvedTitle title = TitleResolver.resolve("Ragni", config, pack::get);

        assertEquals("wynn_wanderer.territory.ragni.title", title.titleKey());
        assertEquals(0x010203, title.color());
    }
}
