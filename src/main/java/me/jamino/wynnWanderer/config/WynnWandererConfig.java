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
        public int textYOffset = -33;

        @ConfigEntry.Gui.Tooltip(count = 3)
        public int textXOffset = 0;

        @ConfigEntry.Gui.Tooltip(count = 3)
        public boolean centerText = true;

        @ConfigEntry.Gui.Tooltip(count = 3)
        public int recentTerritoryCacheSize = 3;
    }
}