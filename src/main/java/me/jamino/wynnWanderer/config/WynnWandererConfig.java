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

        // New subtitle positioning settings
        @ConfigEntry.Gui.Tooltip(count = 3)
        public int subtitleYOffset = -20; // Default position for subtitle

        @ConfigEntry.Gui.Tooltip(count = 3)
        public int subtitleXOffset = 0;

        @ConfigEntry.Gui.Tooltip
        public double subtitleSize = 1.3; // Smaller scale for subtitle

        @ConfigEntry.Gui.Tooltip(count = 3)
        public boolean centerText = true;

        @ConfigEntry.Gui.Tooltip(count = 3)
        public int recentTerritoryCacheSize = 3;
    }
}