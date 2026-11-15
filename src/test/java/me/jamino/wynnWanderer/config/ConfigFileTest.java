package me.jamino.wynnWanderer.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigFileTest {
    /** What Cloth Config's TOML serializer wrote, so files from older versions of the mod still load. */
    private static final String CLOTH_CONFIG_FILE = """
            [territoryTitles]
            enabled = false
            showOnlySignificantTerritories = false

            [territoryTitles.appearance]
            textColor = "12abef"
            textSize = 3.5
            renderShadow = false
            subtitleSize = 1.0
            showSubtitles = true

            [territoryTitles.positioning]
            textYOffset = -120
            textXOffset = 4
            subtitleYOffset = -276
            subtitleXOffset = 0
            centerText = false

            [territoryTitles.animation]
            textFadeInTime = 10
            textDisplayTime = 50
            textFadeOutTime = 10
            textCooldownTime = 80
            recentTerritoryCacheSize = 7

            [territoryTitles.significantTerritories]
            useEnhancedStyling = true
            titleSizeMultiplier = 1.2
            subtitleSizeMultiplier = 1.1
            useCustomColors = true
            defaultColor = "ffcc00"
            """;

    @Test
    void readsFilesWrittenByClothConfig() {
        WynnWandererConfig config = new WynnWandererConfig();
        assertEquals(List.of(), ConfigFile.parse(CLOTH_CONFIG_FILE, config));

        WynnWandererConfig.TerritoryTitlesConfig titles = config.territoryTitles;
        assertFalse(titles.enabled);
        assertFalse(titles.showOnlySignificantTerritories);
        assertEquals("12abef", titles.appearance.textColor);
        assertEquals(3.5, titles.appearance.textSize);
        assertFalse(titles.appearance.renderShadow);
        assertEquals(-120, titles.positioning.textYOffset);
        assertEquals(4, titles.positioning.textXOffset);
        assertFalse(titles.positioning.centerText);
        assertEquals(7, titles.animation.recentTerritoryCacheSize);
    }

    @Test
    void writesTheSameLayoutAsClothConfig() {
        WynnWandererConfig config = new WynnWandererConfig();
        ConfigFile.parse(CLOTH_CONFIG_FILE, config);

        assertEquals(CLOTH_CONFIG_FILE, ConfigFile.format(config));
    }

    @Test
    void defaultsSurviveARoundTrip() {
        WynnWandererConfig read = new WynnWandererConfig();
        read.territoryTitles.appearance.textSize = 99;

        assertEquals(List.of(), ConfigFile.parse(ConfigFile.format(new WynnWandererConfig()), read));
        assertEquals(ConfigFile.format(new WynnWandererConfig()), ConfigFile.format(read));
    }

    @Test
    void missingOptionsKeepTheirDefaults() {
        WynnWandererConfig config = new WynnWandererConfig();
        String text = """
                [territoryTitles.appearance]
                textColor = "000000"
                """;

        assertEquals(List.of(), ConfigFile.parse(text, config));
        assertEquals("000000", config.territoryTitles.appearance.textColor);
        assertEquals(new WynnWandererConfig().territoryTitles.appearance.textSize,
                config.territoryTitles.appearance.textSize);
        assertTrue(config.territoryTitles.enabled);
    }

    @Test
    void handEditedFilesCanHaveCommentsAndOtherTomlSyntax() {
        WynnWandererConfig config = new WynnWandererConfig();
        String text = """
                # my settings
                [ territoryTitles.appearance ]   # the look
                textColor = 'ff00ff'  # literal string
                textSize = 4  # an integer is fine for a decimal option
                "renderShadow"=false
                [territoryTitles.positioning]
                textYOffset = -1_000
                """;

        assertEquals(List.of(), ConfigFile.parse(text, config));
        assertEquals("ff00ff", config.territoryTitles.appearance.textColor);
        assertEquals(4.0, config.territoryTitles.appearance.textSize);
        assertFalse(config.territoryTitles.appearance.renderShadow);
        assertEquals(-1000, config.territoryTitles.positioning.textYOffset);
    }

    @Test
    void badLinesAreReportedAndSkipped() {
        WynnWandererConfig config = new WynnWandererConfig();
        String text = """
                [territoryTitles]
                enabled = maybe
                notAnOption = 1
                [territoryTitles.nope]
                textSize = 9
                [territoryTitles.appearance]
                textSize = big
                textColor = "a#b"
                renderShadow = false
                """;

        List<String> problems = ConfigFile.parse(text, config);

        assertEquals(4, problems.size(), problems.toString());
        assertTrue(problems.get(0).startsWith("line 2:"), problems.get(0));
        assertTrue(config.territoryTitles.enabled);
        assertEquals(new WynnWandererConfig().territoryTitles.appearance.textSize,
                config.territoryTitles.appearance.textSize);
        // A # inside a string is not a comment, and lines after a bad one are still read
        assertEquals("a#b", config.territoryTitles.appearance.textColor);
        assertFalse(config.territoryTitles.appearance.renderShadow);
    }

    @Test
    void stringsAreEscaped() {
        WynnWandererConfig config = new WynnWandererConfig();
        config.territoryTitles.appearance.textColor = "quote \" slash \\ tab \t é";

        WynnWandererConfig read = new WynnWandererConfig();
        assertEquals(List.of(), ConfigFile.parse(ConfigFile.format(config), read));
        assertEquals(config.territoryTitles.appearance.textColor, read.territoryTitles.appearance.textColor);
    }

    @Test
    void writesAndReadsFiles(@TempDir Path dir) throws IOException {
        Path path = dir.resolve("config").resolve("wynn-wanderer.toml");
        WynnWandererConfig config = new WynnWandererConfig();
        config.territoryTitles.animation.textCooldownTime = 5;

        ConfigFile.write(path, config);
        ConfigFile.write(path, config);

        WynnWandererConfig read = new WynnWandererConfig();
        assertEquals(List.of(), ConfigFile.read(path, read));
        assertEquals(5, read.territoryTitles.animation.textCooldownTime);
        try (var files = Files.list(path.getParent())) {
            assertEquals(List.of(path), files.toList(), "The temporary file was left behind");
        }
    }
}
