package me.jamino.wynnWanderer.features;

import com.wynntils.core.components.Models;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.utils.mc.McUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
// Import RenderTickCounter if needed, though it's implicitly used in the lambda
// import net.minecraft.client.render.RenderTickCounter;

import java.util.LinkedList;
import java.util.function.Predicate;

public class TerritoryTitleCore {
    private TerritoryProfile lastTerritoryProfile = null;
    private static final int CHECK_INTERVAL_TICKS = 10;
    private int tickCounter = 0;
    private boolean isEnabled = true;

    // Configuration properties
    public boolean enabled = true;
    public int textFadeInTime = 10;
    public int textDisplayTime = 50;
    public int textFadeOutTime = 10;
    public int textCooldownTime = 80;
    public String textColor = "ffffff";
    public double textSize = 2.1;
    public boolean renderShadow = true;
    public int textYOffset = -33;
    public int textXOffset = 0;
    public boolean centerText = true;
    public int recentTerritoryCacheSize = 3;

    // Title renderer state
    private final LinkedList<TerritoryProfile> recentEntries = new LinkedList<>();
    private Text displayedTitle = null;
    private Text displayedSubTitle = null;
    private int titleTimer = 0;
    private int cooldownTimer = 0;
    private int titleTextColor = 0xFFFFFF;

    public void initialize() {
        // Register tick event to periodically check for territory changes
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isEnabled) return;

            // Only run when on Wynncraft world
            try {
                // Ensure player and world state are available before checking
                if (client.player == null || !Models.WorldState.onWorld()) return;
            } catch (Exception e) {
                // Wynntils might not be loaded yet or world state check failed
                System.err.println("Error checking world state (Wynntils might be initializing): " + e.getMessage());
                return;
            }

            // Territory check timer
            tickCounter++;
            if (tickCounter >= CHECK_INTERVAL_TICKS) {
                tickCounter = 0;
                checkTerritory();
            }

            // Title animation timer
            tickTitle();
        });

        // Register HUD render event
        HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> { // Changed variable name for clarity
            // Pass the float delta time to renderTitle
            renderTitle(drawContext, renderTickCounter.getTickDelta(false)); // <<< FIX 1 APPLIED HERE
        });
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        if (!enabled) {
            lastTerritoryProfile = null;
            clearTitle();
        }
    }

    /**
     * Updates configuration settings for the title renderer
     */
    public void updateSettings(
            boolean enabled,
            int fadeInTime,
            int displayTime,
            int fadeOutTime,
            int cooldownTime,
            String textColor,
            double textSize,
            boolean renderShadow,
            int xOffset,
            int yOffset,
            boolean centerText,
            int cacheSize
    ) {
        this.enabled = enabled;
        this.textFadeInTime = fadeInTime;
        this.textDisplayTime = displayTime;
        this.textFadeOutTime = fadeOutTime;
        this.textCooldownTime = cooldownTime;
        this.textColor = textColor;
        setTextColor(textColor);
        this.textSize = textSize;
        this.renderShadow = renderShadow;
        this.textXOffset = xOffset;
        this.textYOffset = yOffset;
        this.centerText = centerText;
        this.recentTerritoryCacheSize = cacheSize;

        // Ensure the recent entries list respects the new cache size immediately
        while (recentEntries.size() > this.recentTerritoryCacheSize && !recentEntries.isEmpty()) {
            recentEntries.removeFirst();
        }
    }

    private void checkTerritory() {
        // Player null check already happened in tick event, but good practice to keep redundancy
        if (McUtils.player() == null) return;

        try {
            // Get player position using getPos() instead of position()
            Vec3d playerPos = McUtils.player().getPos();

            // Get territory at current position
            TerritoryProfile currentTerritory = Models.Territory.getTerritoryProfileForPosition(playerPos);

            // If player entered a new territory
            if (currentTerritory != null && !currentTerritory.equals(lastTerritoryProfile)) {
                // Skip if the territory is in the recent list and cooldown is active
                if (cooldownTimer > 0 && matchesAnyRecentEntry(t -> t.equals(currentTerritory))) {
                    lastTerritoryProfile = currentTerritory;
                    return;
                }

                String territoryName = currentTerritory.getFriendlyName();
                // Only display if the name is not null or empty
                if (territoryName != null && !territoryName.isEmpty()) {
                    displayTerritoryTitle(territoryName);

                    // Update last territory and add to recent entries
                    lastTerritoryProfile = currentTerritory;
                    addRecentEntry(currentTerritory);
                } else {
                    // Territory exists but has no friendly name, treat as leaving territory for display purposes
                    lastTerritoryProfile = null;
                }

            } else if (currentTerritory == null && lastTerritoryProfile != null) {
                // Player left a territory and is not in a new one
                lastTerritoryProfile = null;
            }
        } catch (Exception e) {
            System.err.println("Error checking territory: " + e.getMessage());
            // Consider more specific error handling or logging if needed
            // e.printStackTrace(); // Optional: uncomment for detailed stack trace during debugging
        }
    }

    private void displayTerritoryTitle(String territoryName) {
        // Create the title and subtitle components with translation keys
        Text title = Text.translatable("wynn_wanderer.territory.entering.title", territoryName);
        Text subtitle = Text.translatable("wynn_wanderer.territory.entering.subtitle");

        // Start displaying the title
        displayedTitle = title;
        displayedSubTitle = subtitle;
        titleTimer = textFadeInTime + textDisplayTime + textFadeOutTime;
        cooldownTimer = textCooldownTime; // Start cooldown immediately upon display
    }

    private void tickTitle() {
        // Tick down cooldown regardless of title display
        if (cooldownTimer > 0) {
            --cooldownTimer;
        }

        // Tick down title timer only if a title is active
        if (titleTimer > 0) {
            --titleTimer;
            if (titleTimer <= 0) {
                clearTitle();
                // Cooldown continues independently
            }
        }
    }

    private void clearTitle() {
        // Only clear display variables, timer is handled in tickTitle
        displayedTitle = null;
        displayedSubTitle = null;
        // Do not reset titleTimer here, tickTitle handles reaching 0
    }

    private void renderTitle(DrawContext drawContext, float partialTicks) {
        if (!isEnabled || displayedTitle == null || titleTimer <= 0) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        // Check if debug screen is visible
        if (mc.getDebugHud().shouldShowDebugHud()) return; // <<< FIX 2 APPLIED HERE

        // Calculate fade opacity
        float age = (float) titleTimer - partialTicks; // Calculate remaining time precisely
        int opacity = 255; // Default to fully opaque (during display phase)

        // Fade-in phase
        if (age > textFadeOutTime + textDisplayTime) {
            // Calculate progress through fade-in phase
            // Total duration of fade-in = textFadeInTime
            // Time elapsed in fade-in = (Total Title Time) - age - (Display Time) - (Fade Out Time)
            // Simplified: Time into fade-in = textFadeInTime - (age - (textDisplayTime + textFadeOutTime))
            float fadeInProgress = (float)textFadeInTime - (age - (textDisplayTime + textFadeOutTime));
            if (textFadeInTime > 0) { // Avoid division by zero
                opacity = (int) (MathHelper.clamp(fadeInProgress, 0, textFadeInTime) * 255.0F / (float) textFadeInTime);
            } else {
                opacity = 255; // Instant fade-in if time is 0
            }
        }
        // Fade-out phase
        else if (age <= textFadeOutTime) {
            // Calculate progress through fade-out phase (age is time remaining in fade-out)
            if (textFadeOutTime > 0) { // Avoid division by zero
                opacity = (int) (MathHelper.clamp(age, 0, textFadeOutTime) * 255.0F / (float) textFadeOutTime);
            } else {
                opacity = 0; // Instant fade-out if time is 0
            }
        }
        // Display phase: opacity remains 255

        opacity = MathHelper.clamp(opacity, 0, 255);
        // Don't render if almost fully transparent
        if (opacity < 8) return;

        // Set up rendering state
        drawContext.getMatrices().push();

        // Center the title if enabled
        if (centerText) {
            // Translate to the center of the screen, then apply offsets
            drawContext.getMatrices().translate(
                    mc.getWindow().getScaledWidth() / 2.0 + textXOffset, // Apply X offset relative to center
                    mc.getWindow().getScaledHeight() / 2.0 + textYOffset, // Apply Y offset relative to center
                    0);
        } else {
            // Translate based on offsets from top-left
            drawContext.getMatrices().translate(textXOffset, textYOffset, 0);
        }

        RenderSystem.enableBlend();
        // Default blend function is usually SRC_ALPHA, ONE_MINUS_SRC_ALPHA
        RenderSystem.defaultBlendFunc();

        // --- Title Rendering ---
        drawContext.getMatrices().push();
        drawContext.getMatrices().scale((float)textSize, (float)textSize, (float)textSize);

        int alpha = opacity << 24; // Apply alpha (no bitwise AND needed here)
        TextRenderer fontRenderer = mc.textRenderer;
        int titleWidth = fontRenderer.getWidth(displayedTitle);

        // Calculate x position based on centering
        int titleXPos = centerText ? -(titleWidth / 2) : 0; // If centered, offset by half width; otherwise, start at 0 (relative to translation)
        // Y position is implicitly 0 relative to the translation applied earlier

        // Draw title
        drawContext.drawText(
                fontRenderer,
                displayedTitle,
                titleXPos, // X position relative to translation/scaling
                0,         // Y position relative to translation/scaling
                titleTextColor | alpha, // Combine color and alpha
                renderShadow);

        drawContext.getMatrices().pop(); // Pop title scaling

        // --- Subtitle Rendering (if exists) ---
        if (displayedSubTitle != null) {
            drawContext.getMatrices().push();
            // Subtitle is typically smaller, scale it relative to the main translation
            float subtitleScale = 1.0F; // Adjust as needed, maybe make configurable?
            drawContext.getMatrices().scale(subtitleScale, subtitleScale, subtitleScale);

            int subtitleWidth = fontRenderer.getWidth(displayedSubTitle);
            // Calculate subtitle position relative to the main translation
            int subtitleXPos = centerText ? -(subtitleWidth / 2) : 0;
            // Position subtitle below the main title (adjust 14 based on font size/scaling if needed)
            int subtitleYPos = (int)(14 / textSize); // Adjust Y based on main title's scale if they share the same translation point

            drawContext.drawText(
                    fontRenderer,
                    displayedSubTitle,
                    subtitleXPos,
                    subtitleYPos, // Position below the title
                    0xFFFFFF | alpha, // White subtitle, maybe make configurable?
                    renderShadow);

            drawContext.getMatrices().pop(); // Pop subtitle scaling
        }

        // Clean up render state
        RenderSystem.disableBlend();
        drawContext.getMatrices().pop(); // Pop main translation
    }


    private void setTextColor(String textColor) {
        try {
            // Ensure the color string is treated as hexadecimal
            this.titleTextColor = Integer.parseInt(textColor.replace("#", ""), 16);
        } catch (Exception e) {
            System.err.println("Text color '" + textColor + "' is not a valid hex color (e.g., 'ffffff'). Defaulting to white...");
            this.titleTextColor = 0xFFFFFF; // Default to white
        }
    }

    private void addRecentEntry(TerritoryProfile entry) {
        // Avoid adding duplicates if it's already the last entry
        if (!recentEntries.isEmpty() && recentEntries.getLast().equals(entry)) {
            return;
        }

        // Remove oldest if cache size is exceeded
        while (recentEntries.size() >= recentTerritoryCacheSize && !recentEntries.isEmpty()) {
            recentEntries.removeFirst();
        }
        // Add new entry if cache size allows
        if (recentTerritoryCacheSize > 0) {
            recentEntries.addLast(entry);
        }
    }


    private boolean matchesAnyRecentEntry(Predicate<TerritoryProfile> entryMatchPredicate) {
        // Check if the predicate matches any entry in the current list
        return recentEntries.stream().anyMatch(entryMatchPredicate);
    }
}