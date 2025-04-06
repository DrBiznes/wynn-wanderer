package me.jamino.wynnWanderer.features;

import com.wynntils.core.components.Models;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class TerritoryTitleCore {
    private TerritoryProfile lastTerritoryProfile = null;
    private static final int CHECK_INTERVAL_TICKS = 10;
    private int tickCounter = 0;
    private boolean isEnabled = true;

    public void initialize() {
        // Register tick event to periodically check for territory changes
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isEnabled) return;

            // Only run when on Wynncraft world
            try {
                if (!Models.WorldState.onWorld()) return;
            } catch (Exception e) {
                // Wynntils might not be loaded yet
                return;
            }

            tickCounter++;
            if (tickCounter >= CHECK_INTERVAL_TICKS) {
                tickCounter = 0;
                checkTerritory();
            }
        });
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        if (!enabled) {
            lastTerritoryProfile = null;
        }
    }

    private void checkTerritory() {
        if (McUtils.player() == null) return;

        try {
            // Get player position using getPos() instead of position()
            Vec3d playerPos = McUtils.player().getPos();

            // Convert to Position type if needed by creating a simple Position object
            // If getTerritoryProfileForPosition expects a specific Position type,
            // we need to create a compatible object
            TerritoryProfile currentTerritory = Models.Territory.getTerritoryProfileForPosition(playerPos);

            // If player entered a new territory
            if (currentTerritory != null && !currentTerritory.equals(lastTerritoryProfile)) {
                String territoryName = currentTerritory.getFriendlyName();
                displayTerritoryTitle(territoryName);

                // Update last territory
                lastTerritoryProfile = currentTerritory;
            } else if (currentTerritory == null && lastTerritoryProfile != null) {
                // Player left a territory and is not in a new one
                lastTerritoryProfile = null;
            }
        } catch (Exception e) {
            System.err.println("Error checking territory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayTerritoryTitle(String territoryName) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        try {
            // Create the title and subtitle components with translation keys
            Text title = Text.translatable("wynn_wanderer.territory.entering.title", territoryName);
            Text subtitle = Text.translatable("wynn_wanderer.territory.entering.subtitle");

            // Display the title on screen
            mc.inGameHud.setTitle(title);
            mc.inGameHud.setSubtitle(subtitle);
            mc.inGameHud.setTitleTicks(10, 70, 20); // fadeIn, stay, fadeOut
        } catch (Exception e) {
            System.err.println("Error displaying title: " + e.getMessage());
            e.printStackTrace();
        }
    }
}