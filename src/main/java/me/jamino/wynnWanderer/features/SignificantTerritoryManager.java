package me.jamino.wynnWanderer.features;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SignificantTerritoryManager {
    // List of significant cities for special styling
    public static final Set<String> SIGNIFICANT_TERRITORIES = new HashSet<>(Arrays.asList(
            "Llevigar", "Gelibord", "Olux", "Rodoroc", "Thanos", "Eltom", "Cinfras", "Ahmsord",
            "Kandon-Beda", "Thesead", "Corkus City", "Selchar", "Nemract", "Almuj",
            "Ragni", "Detlas", "Lutho", "Nesaak", "Troms", "Alekin",
            // Towns
            "Bucie", "Bremminglar", "Rymek", "Ternaves", "Lusuco", "Maltic",
            // Fruma
            "Fort Torann", "Espren", "Timasca", "Aldwell", "Hyloch"
    ));

    /**
     * Territories that stand for a city whose name they don't have, e.g. there is no Bucie territory, only the
     * entrance to it.
     */
    public static final Map<String, String> CITY_OF_TERRITORY = Map.of(
            "Entrance to Bucie", "Bucie"
    );

    /**
     * @return The city the territory stands for, or the territory itself
     */
    public static String cityOf(String territoryName) {
        return CITY_OF_TERRITORY.getOrDefault(territoryName, territoryName);
    }

    public static boolean isSignificant(String territoryName) {
        return SIGNIFICANT_TERRITORIES.contains(cityOf(territoryName));
    }

    /**
     * @return The translation key prefix for the territory, e.g. "wynn_wanderer.territory.corkus_city"
     */
    public static String getTranslationKey(String territoryName) {
        return TitleResolver.KEY_PREFIX + cityOf(territoryName).toLowerCase(Locale.ROOT).replace(" ", "_");
    }
}
