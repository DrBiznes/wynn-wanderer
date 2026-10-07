package me.jamino.wynnWanderer.gametest;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.utils.type.RenderElementType;
import me.jamino.wynnWanderer.wynntils.WynnWandererFeature;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

/**
 * Wynntils only posts its render events while connected to Wynncraft, so overlays are never rendered
 * in the singleplayer world the smoke tests run in. This HUD element stands in for Wynncraft and lets
 * the tests choose what to render each frame.
 */
public class RenderHook implements ClientModInitializer {
    public enum Mode {
        NONE,
        // Post the render event Wynntils posts when the vanilla title is rendered, which makes the
        // Wynntils overlay manager render the overlays in that layer
        WYNNTILS_RENDER_EVENT,
        // Render the overlay directly, the same way the Wynntils overlay manager does on Wynncraft
        OVERLAY
    }

    private static volatile Mode mode = Mode.NONE;
    private static volatile int renderCount = 0;
    private static volatile Throwable failure = null;

    @Override
    public void onInitializeClient() {
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("wynn-wanderer-gametest", "render_hook"), RenderHook::render);
    }

    public static void setMode(Mode newMode) {
        mode = newMode;
        renderCount = 0;
    }

    public static int getRenderCount() {
        return renderCount;
    }

    /**
     * @return The first exception thrown while rendering, or null if there were none
     */
    public static Throwable getFailure() {
        return failure;
    }

    private static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Mode currentMode = mode;
        if (currentMode == Mode.NONE) return;

        Minecraft client = Minecraft.getInstance();
        try {
            if (currentMode == Mode.WYNNTILS_RENDER_EVENT) {
                WynntilsMod.postEvent(
                        new RenderEvent.Pre(guiGraphics, deltaTracker, client.getWindow(), RenderElementType.TITLE));
            } else {
                WynnWandererFeature.getInstance()
                        .getTerritoryTitleOverlay()
                        .render(guiGraphics, deltaTracker, client.getWindow());
            }
            renderCount++;
        } catch (Throwable t) {
            if (failure == null) {
                failure = t;
            }
        }
    }
}
