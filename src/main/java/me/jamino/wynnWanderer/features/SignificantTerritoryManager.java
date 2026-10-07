package me.jamino.wynnWanderer.features;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class SignificantTerritoryManager {
    // List of significant cities for special styling
    public static final Set<String> SIGNIFICANT_TERRITORIES = new HashSet<>(Arrays.asList(
            "Llevigar", "Gelibord", "Olux", "Rodoroc", "Eltom", "Cinfras", "Ahmsord",
            "Kandon-Beda", "Thesead", "Corkus City", "Selchar", "Nemract", "Almuj",
            "Ragni", "Detlas", "Lutho", "Nesaak", "Troms", "Alekin",
            // Fruma
            "Fort Torann", "Espren", "Timasca", "Aldwell", "Hyloch"
    ));

    public static boolean isSignificant(String territoryName) {
        return SIGNIFICANT_TERRITORIES.contains(territoryName);
    }

    /**
     * @return The translation key prefix for the territory, e.g. "wynn_wanderer.territory.corkus_city"
     */
    public static String getTranslationKey(String territoryName) {
        return TitleResolver.KEY_PREFIX + territoryName.toLowerCase(Locale.ROOT).replace(" ", "_");
    }
}
