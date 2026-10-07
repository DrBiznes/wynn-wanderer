package me.jamino.wynnWanderer;

import me.jamino.wynnWanderer.config.WynnWandererConfig;
import me.jamino.wynnWanderer.features.TerritoryTitleCore;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.world.InteractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WynnWanderer implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("wynn-wanderer");
    private static TerritoryTitleCore territoryTitleCore;
    private static WynnWandererConfig config = new WynnWandererConfig();

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing WynnWanderer client");

        // Initialize config
        AutoConfig.register(WynnWandererConfig.class, Toml4jConfigSerializer::new);
        config = AutoConfig.getConfigHolder(WynnWandererConfig.class).getConfig();

        // Register config save listener
        AutoConfig.getConfigHolder(WynnWandererConfig.class).registerSaveListener((configHolder, newConfig) -> {
            config = newConfig;
            return InteractionResult.SUCCESS;
        });

        // Initialize the territory title core. The title itself is rendered by the
        // overlay that FeatureManagerMixin registers with Wynntils.
        territoryTitleCore = new TerritoryTitleCore();
        territoryTitleCore.initialize();

        LOGGER.info("WynnWanderer client initialized");
    }

    public static TerritoryTitleCore getTerritoryTitleCore() {
        return territoryTitleCore;
    }

    public static WynnWandererConfig getConfig() {
        return config;
    }
}
