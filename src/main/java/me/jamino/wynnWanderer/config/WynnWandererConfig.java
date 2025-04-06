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
        @ConfigEntry.Gui.Tooltip
        public boolean enabled = true;

        @ConfigEntry.Gui.Tooltip(count = 2)
        public boolean showOnlySignificantTerritories = true;  // New option, default to true

        @ConfigEntry.Gui.CollapsibleObject
        @ConfigEntry.Gui.Tooltip
        public AppearanceConfig appearance = new AppearanceConfig();

        @ConfigEntry.Gui.CollapsibleObject
        @ConfigEntry.Gui.Tooltip
        public PositioningConfig positioning = new PositioningConfig();

        @ConfigEntry.Gui.CollapsibleObject
        @ConfigEntry.Gui.Tooltip
        public AnimationConfig animation = new AnimationConfig();

        @ConfigEntry.Gui.CollapsibleObject
        @ConfigEntry.Gui.Tooltip
        public SignificantTerritoryConfig significantTerritories = new SignificantTerritoryConfig();

        public static class AppearanceConfig {
            @ConfigEntry.Gui.Tooltip
            public String textColor = "ffffff";

            @ConfigEntry.Gui.Tooltip
            public double textSize = 2.1;

            @ConfigEntry.Gui.Tooltip
            public boolean renderShadow = true;

            @ConfigEntry.Gui.Tooltip
            public double subtitleSize = 1.3;

            @ConfigEntry.Gui.Tooltip
            public boolean showSubtitles = true;  // New option to show/hide subtitles
        }

        public static class PositioningConfig {
            @ConfigEntry.Gui.Tooltip(count = 3)
            public int textYOffset = -300;  // Updated default to move title up more

            @ConfigEntry.Gui.Tooltip(count = 3)
            public int textXOffset = 0;

            @ConfigEntry.Gui.Tooltip(count = 3)
            public int subtitleYOffset = -240;  // Adjusted to be below the title

            @ConfigEntry.Gui.Tooltip(count = 3)
            public int subtitleXOffset = 0;

            @ConfigEntry.Gui.Tooltip(count = 3)
            public boolean centerText = true;
        }

        public static class AnimationConfig {
            @ConfigEntry.Gui.Tooltip(count = 2)
            public int textFadeInTime = 10;

            @ConfigEntry.Gui.Tooltip(count = 2)
            public int textDisplayTime = 50;

            @ConfigEntry.Gui.Tooltip(count = 2)
            public int textFadeOutTime = 10;

            @ConfigEntry.Gui.Tooltip(count = 2)
            public int textCooldownTime = 80;

            @ConfigEntry.Gui.Tooltip(count = 3)
            public int recentTerritoryCacheSize = 3;
        }

        public static class SignificantTerritoryConfig {
            @ConfigEntry.Gui.Tooltip
            public boolean useEnhancedStyling = true;

            @ConfigEntry.Gui.Tooltip
            public double titleSizeMultiplier = 1.2;

            @ConfigEntry.Gui.Tooltip
            public double subtitleSizeMultiplier = 1.1;

            @ConfigEntry.Gui.Tooltip(count = 2)
            public boolean useCustomColors = true;

            @ConfigEntry.Gui.Tooltip
            public String defaultColor = "ffcc00";
        }
    }
}