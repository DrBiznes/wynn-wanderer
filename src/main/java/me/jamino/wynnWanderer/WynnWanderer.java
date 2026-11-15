package me.jamino.wynnWanderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import me.jamino.wynnWanderer.config.ConfigFile;
import me.jamino.wynnWanderer.config.WynnWandererConfig;
import me.jamino.wynnWanderer.features.TerritoryTitleCore;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WynnWanderer implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("wynn-wanderer");
    private static TerritoryTitleCore territoryTitleCore;
    private static WynnWandererConfig config = new WynnWandererConfig();

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing WynnWanderer client");

        loadConfig();

        // Initialize the territory title core
        territoryTitleCore = new TerritoryTitleCore();
        territoryTitleCore.initialize();

        LOGGER.info("WynnWanderer client initialized");
    }

    /** Cloth Config is optional, it is only needed for the config screen. */
    public static boolean isClothConfigLoaded() {
        return FabricLoader.getInstance().isModLoaded("cloth-config");
    }

    public static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("wynn-wanderer.toml");
    }

    /** Reads the config file, creating it with the defaults if it does not exist yet. */
    public static void loadConfig() {
        Path path = getConfigPath();
        config = new WynnWandererConfig();

        if (!Files.exists(path)) {
            saveConfig();
            return;
        }

        try {
            List<String> problems = ConfigFile.read(path, config);
            if (problems.isEmpty()) {
                // Write it back so options added in newer versions show up in the file
                saveConfig();
            } else {
                // Leave the file alone so the broken lines can be fixed by hand
                problems.forEach(problem -> LOGGER.warn("Ignoring {} in {}", problem, path));
            }
        } catch (IOException e) {
            LOGGER.error("Could not read {}, using the default settings", path, e);
        }
    }

    public static void saveConfig() {
        try {
            ConfigFile.write(getConfigPath(), config);
        } catch (IOException e) {
            LOGGER.error("Could not save {}", getConfigPath(), e);
        }
    }

    public static TerritoryTitleCore getTerritoryTitleCore() {
        return territoryTitleCore;
    }

    public static WynnWandererConfig getConfig() {
        return config;
    }
}
