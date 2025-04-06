package me.jamino.wynnWanderer.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "wynn-wanderer")
public class WynnWandererConfig implements ConfigData {
    @ConfigEntry.Gui.CollapsibleObject
    @ConfigEntry.Gui.Tooltip
    public TerritoryTitlesConfig territoryTitles = new TerritoryTitlesConfig();

    public static class TerritoryTitlesConfig {
        public boolean enabled = true;

        @ConfigEntry.Gui.Tooltip(count = 2)
        public int textFadeInTime = 10;

        @ConfigEntry.Gui.Tooltip(count = 2)
        public int textDisplayTime = 50;

        @ConfigEntry.Gui.Tooltip(count = 2)
        public int textFadeOutTime = 10;

        @ConfigEntry.Gui.Tooltip(count = 2)
        public int textCooldownTime = 80;

        @ConfigEntry.Gui.Tooltip
        public String textColor = "ffffff";

        @ConfigEntry.Gui.Tooltip
        public double textSize = 2.1;

        @ConfigEntry.Gui.Tooltip
        public boolean renderShadow = true;

        @ConfigEntry.Gui.Tooltip(count = 3)
        public int textYOffset = -40; // Updated default to move title up a bit

        @ConfigEntry.Gui.Tooltip(count = 3)
        public int textXOffset = 0;

        // Subtitle positioning settings
        @ConfigEntry.Gui.Tooltip(count = 3)
        public int subtitleYOffset = -20; // Default position for subtitle

        @ConfigEntry.Gui.Tooltip(count = 3)
        public int subtitleXOffset = 0;

        @ConfigEntry.Gui.Tooltip
        public double subtitleSize = 1.3; // Smaller scale for subtitle

        // Significant territory settings
        @ConfigEntry.Gui.CollapsibleObject
        @ConfigEntry.Gui.Tooltip
        public SignificantTerritoryConfig significantTerritories = new SignificantTerritoryConfig();

        @ConfigEntry.Gui.Tooltip(count = 3)
        public boolean centerText = true;

        @ConfigEntry.Gui.Tooltip(count = 3)
        public int recentTerritoryCacheSize = 3;

        public static class SignificantTerritoryConfig {
            @ConfigEntry.Gui.Tooltip
            public boolean useEnhancedStyling = true;

            @ConfigEntry.Gui.Tooltip
            public double titleSizeMultiplier = 1.2; // Makes significant territory titles slightly larger

            @ConfigEntry.Gui.Tooltip
            public double subtitleSizeMultiplier = 1.1; // Makes significant territory subtitles slightly larger

            @ConfigEntry.Gui.Tooltip(count = 2)
            public boolean useCustomColors = true; // Whether to use colors defined in lang files

            @ConfigEntry.Gui.Tooltip
            public String defaultColor = "ffcc00"; // Default gold color for significant territories
        }
    }
}