package me.jamino.wynnWanderer;

import me.jamino.wynnWanderer.features.TerritoryTitleCore;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WynnWanderer implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("wynn-wanderer");
    private static TerritoryTitleCore territoryTitleCore;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing WynnWanderer client");

        // Initialize the territory title core
        territoryTitleCore = new TerritoryTitleCore();
        territoryTitleCore.initialize();

        LOGGER.info("WynnWanderer client initialized");
    }

    public static TerritoryTitleCore getTerritoryTitleCore() {
        return territoryTitleCore;
    }
}