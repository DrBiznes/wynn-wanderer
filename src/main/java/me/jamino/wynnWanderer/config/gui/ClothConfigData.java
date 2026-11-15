package me.jamino.wynnWanderer.config.gui;

import me.jamino.wynnWanderer.config.WynnWandererConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

/**
 * The root of the config as Cloth Config sees it. Only loaded when Cloth Config is installed, which is why it is
 * kept apart from {@link WynnWandererConfig}. It holds the same option objects as the live config, so the screen
 * edits the settings the mod is using.
 * <p>
 * Cloth Config only shows the fields declared here, so this has to have the same fields as
 * {@link WynnWandererConfig}.
 */
@Config(name = "wynn-wanderer")
public class ClothConfigData implements ConfigData {
    @ConfigEntry.Gui.CollapsibleObject
    @ConfigEntry.Gui.Tooltip
    public WynnWandererConfig.TerritoryTitlesConfig territoryTitles;

    static ClothConfigData of(WynnWandererConfig config) {
        ClothConfigData data = new ClothConfigData();
        data.territoryTitles = config.territoryTitles;
        return data;
    }

    void copyTo(WynnWandererConfig config) {
        config.territoryTitles = territoryTitles;
    }
}
