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
        WynnWandererConfig.TerritoryTitlesConfig.AppearanceConfig appearanceConfig = ttConfig.appearance;
        WynnWandererConfig.TerritoryTitlesConfig.PositioningConfig positioningConfig = ttConfig.positioning;
        WynnWandererConfig.TerritoryTitlesConfig.AnimationConfig animationConfig = ttConfig.animation;
        WynnWandererConfig.TerritoryTitlesConfig.SignificantTerritoryConfig stConfig = ttConfig.significantTerritories;

        territoryTitleCore.updateSettings(
                ttConfig.enabled,
                ttConfig.showOnlySignificantTerritories,
                animationConfig.textFadeInTime,
                animationConfig.textDisplayTime,
                animationConfig.textFadeOutTime,
                animationConfig.textCooldownTime,
                appearanceConfig.textColor,
                appearanceConfig.textSize,
                appearanceConfig.renderShadow,
                positioningConfig.textXOffset,
                positioningConfig.textYOffset,
                positioningConfig.subtitleXOffset,
                positioningConfig.subtitleYOffset,
                appearanceConfig.subtitleSize,
                appearanceConfig.showSubtitles, // Add the new parameter
                stConfig.useEnhancedStyling,
                stConfig.titleSizeMultiplier,
                stConfig.subtitleSizeMultiplier,
                stConfig.useCustomColors,
                stConfig.defaultColor,
                positioningConfig.centerText,
                animationConfig.recentTerritoryCacheSize
        );
    }

    public static TerritoryTitleCore getTerritoryTitleCore() {
        return territoryTitleCore;
    }

    public static WynnWandererConfig getConfig() {
        return config;
    }
}