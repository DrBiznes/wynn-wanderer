package me.jamino.wynnWanderer;

import me.jamino.wynnWanderer.config.WynnWandererConfig;
import me.jamino.wynnWanderer.features.TerritoryTitleCore;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WynnWanderer implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("wynn-wanderer");
    private static TerritoryTitleCore territoryTitleCore;
    private static WynnWandererConfig config;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing WynnWanderer client");

        // Initialize config
        AutoConfig.register(WynnWandererConfig.class, Toml4jConfigSerializer::new);
        config = AutoConfig.getConfigHolder(WynnWandererConfig.class).getConfig();

        // Register config save listener
        AutoConfig.getConfigHolder(WynnWandererConfig.class).registerSaveListener((configHolder, newConfig) -> {
            config = newConfig;
            applyConfigToTerritoryTitleCore();
            return ActionResult.SUCCESS;
        });

        // Initialize the territory title core
        territoryTitleCore = new TerritoryTitleCore();
        applyConfigToTerritoryTitleCore();
        territoryTitleCore.initialize();

        LOGGER.info("WynnWanderer client initialized");
    }

    private void applyConfigToTerritoryTitleCore() {
        WynnWandererConfig.TerritoryTitlesConfig ttConfig = config.territoryTitles;

        territoryTitleCore.updateSettings(
                ttConfig.enabled,
                ttConfig.textFadeInTime,
                ttConfig.textDisplayTime,
                ttConfig.textFadeOutTime,
                ttConfig.textCooldownTime,
                ttConfig.textColor,
                ttConfig.textSize,
                ttConfig.renderShadow,
                ttConfig.textXOffset,
                ttConfig.textYOffset,
                ttConfig.subtitleXOffset,
                ttConfig.subtitleYOffset,
                ttConfig.subtitleSize,
                ttConfig.centerText,
                ttConfig.recentTerritoryCacheSize
        );
    }

    public static TerritoryTitleCore getTerritoryTitleCore() {
        return territoryTitleCore;
    }

    public static WynnWandererConfig getConfig() {
        return config;
    }
}