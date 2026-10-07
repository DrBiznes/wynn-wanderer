package me.jamino.wynnWanderer.gametest;

import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.screens.overlays.placement.OverlayManagementScreen;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import me.jamino.wynnWanderer.WynnWanderer;
import me.jamino.wynnWanderer.config.WynnWandererConfig;
import me.jamino.wynnWanderer.features.TerritoryTitleCore;
import me.jamino.wynnWanderer.wynntils.TerritoryTitleOverlay;
import me.jamino.wynnWanderer.wynntils.WynnWandererFeature;
import me.shedaniel.autoconfig.AutoConfigClient;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Starts the real client with Wynntils installed and checks that the mod loads, that the territory
 * title overlay is registered with the Wynntils overlay manager, and that it renders without crashing.
 * <p>
 * Run with {@code gradlew runClientGameTest}. Screenshots are saved to build/run/clientGameTest/screenshots.
 */
public class WynnWandererSmokeTest implements FabricClientGameTest {
    // Wynntils downloads its data before it initializes features, which can take a while
    private static final int WYNNTILS_INIT_TIMEOUT_TICKS = 20 * 120;

    @Override
    public void runTest(ClientGameTestContext context) {
        modIsInitialized(context);
        overlayIsRegisteredWithWynntils(context);
        overlayCanBeMovedWithTheOverlayManager(context);
        configScreenOpens(context);
        overlayRendersInWorld(context);
    }

    private void modIsInitialized(ClientGameTestContext context) {
        context.runOnClient(client -> {
            check(WynnWanderer.getTerritoryTitleCore() != null, "The territory title core was not initialized");
            check(WynnWanderer.getConfig() != null, "The config was not loaded");
        });
    }

    private void overlayIsRegisteredWithWynntils(ClientGameTestContext context) {
        // Wynntils initializes its features once resources have finished loading
        context.waitFor(client -> WynnWandererFeature.getInstance() != null, WYNNTILS_INIT_TIMEOUT_TICKS);

        context.runOnClient(client -> {
            WynnWandererFeature feature = WynnWandererFeature.getInstance();
            TerritoryTitleOverlay overlay = feature.getTerritoryTitleOverlay();

            check(
                    Managers.Feature.getFeatureInstance(WynnWandererFeature.class) == feature,
                    "The feature is not registered with the Wynntils feature manager");
            check(feature.isEnabled(), "The feature is not enabled");

            check(Managers.Overlay.getOverlays().contains(overlay), "The overlay is not registered with Wynntils");
            check(Managers.Overlay.getOverlayParent(overlay) == feature, "The overlay has the wrong parent feature");
            check(Managers.Overlay.isEnabled(overlay), "The overlay is not enabled");
            check(
                    Managers.Overlay.getRenderMap().values().stream().anyMatch(overlays -> overlays.contains(overlay)),
                    "The overlay is not in the Wynntils render order");

            // These are what the Wynntils config manager saves and loads, so without them the position is lost
            for (String option : new String[] {"position", "size", "userEnabled"}) {
                check(
                        overlay.getConfigOptionFromString(option).isPresent(),
                        "Wynntils did not register the overlay config option " + option);
            }

            check(
                    "Wynn Wanderer".equals(feature.getTranslatedName()),
                    "The feature name is not translated: " + feature.getTranslatedName());
            check(
                    !feature.getTranslatedDescription().startsWith("feature.wynntils."),
                    "The feature description is not translated");
            check(
                    "Territory Title".equals(overlay.getTranslatedName()),
                    "The overlay name is not translated: " + overlay.getTranslatedName());
        });
    }

    private void overlayCanBeMovedWithTheOverlayManager(ClientGameTestContext context) {
        context.runOnClient(client -> {
            TerritoryTitleOverlay overlay = WynnWandererFeature.getInstance().getTerritoryTitleOverlay();
            OverlayPosition original = overlay.getPosition();
            check(original != null, "The overlay has no position");

            float originalX = overlay.getRenderX();
            float originalY = overlay.getRenderY();

            // This is what dragging the overlay in the Wynntils overlay manager does
            overlay.setPosition(new OverlayPosition(
                    original.getVerticalOffset() + 25,
                    original.getHorizontalOffset() + 40,
                    original.getVerticalAlignment(),
                    original.getHorizontalAlignment(),
                    original.getAnchorSection()));

            check(overlay.getRenderX() == originalX + 40, "Moving the overlay did not change where it renders");
            check(overlay.getRenderY() == originalY + 25, "Moving the overlay did not change where it renders");

            overlay.setPosition(original);
        });
    }

    private void configScreenOpens(ClientGameTestContext context) {
        context.setScreen(() -> AutoConfigClient.getConfigScreen(WynnWandererConfig.class, null)
                .get());
        context.waitTicks(5);
        context.takeScreenshot("wynn-wanderer-config");
        context.setScreen(() -> null);
    }

    private void overlayRendersInWorld(ClientGameTestContext context) {
        // Wynntils only renders overlays while in a world
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getClientWorld().waitForChunksRender();

            overlayRendersInTheOverlayManager(context);
            overlayRendersTerritoryTitles(context);
        } finally {
            RenderHook.setMode(RenderHook.Mode.NONE);
        }
    }

    private void overlayRendersInTheOverlayManager(ClientGameTestContext context) {
        TerritoryTitleOverlay overlay = WynnWandererFeature.getInstance().getTerritoryTitleOverlay();
        OverlayPosition original = context.computeOnClient(client -> overlay.getPosition());

        // The Wynntils overlay manager renders a preview of the selected overlay
        context.setScreen(() -> OverlayManagementScreen.create(null, overlay));
        RenderHook.setMode(RenderHook.Mode.WYNNTILS_RENDER_EVENT);
        context.waitTicks(20);
        context.takeScreenshot("wynn-wanderer-overlay-manager");

        context.runOnClient(client -> {
            check(client.screen instanceof OverlayManagementScreen, "The Wynntils overlay manager did not open");
            checkRendered(overlay);
            check(overlay.getPreviewRenderCount() > 0, "Wynntils never rendered the overlay preview");
        });

        // Top left of the screen instead of the default, to check that position and alignment changes are picked up
        int previewRenderCount = context.computeOnClient(client -> {
            overlay.setPosition(new OverlayPosition(
                    10, 10, VerticalAlignment.TOP, HorizontalAlignment.LEFT, OverlayPosition.AnchorSection.TOP_LEFT));
            return overlay.getPreviewRenderCount();
        });
        context.waitTicks(5);
        context.takeScreenshot("wynn-wanderer-overlay-manager-moved");

        context.runOnClient(client -> {
            checkRendered(overlay);
            check(
                    overlay.getPreviewRenderCount() > previewRenderCount,
                    "Wynntils stopped rendering the overlay preview after it was moved");
            overlay.setPosition(original);
        });

        context.setScreen(() -> null);
    }

    private void overlayRendersTerritoryTitles(ClientGameTestContext context) {
        TerritoryTitleOverlay overlay = WynnWandererFeature.getInstance().getTerritoryTitleOverlay();
        WynnWandererConfig.TerritoryTitlesConfig config = WynnWanderer.getConfig().territoryTitles;
        TerritoryTitleCore core = WynnWanderer.getTerritoryTitleCore();

        // The title timers only run on Wynncraft, so skip the fade-in to get a fully opaque title
        int fadeInTime = config.animation.textFadeInTime;
        boolean onlySignificant = config.showOnlySignificantTerritories;

        try {
            context.runOnClient(client -> {
                config.animation.textFadeInTime = 0;
                core.displayTerritoryTitle("Detlas");

                TerritoryTitleCore.DisplayedTitle title = core.getDisplayedTitle();
                check(title != null && title.significant(), "Detlas was not displayed as a significant territory");
                check("Detlas".equals(title.title().getString()), "Wrong title: " + title.title().getString());
                check(
                        "Heart of the Province".equals(title.subtitle().getString()),
                        "Wrong subtitle: " + title.subtitle().getString());
                check(title.color() == 0x669933, "Wrong color: " + Integer.toHexString(title.color()));
            });

            RenderHook.setMode(RenderHook.Mode.OVERLAY);
            context.waitTicks(10);
            context.takeScreenshot("wynn-wanderer-title-significant");
            context.runOnClient(client -> checkRendered(overlay));

            context.runOnClient(client -> {
                core.displayTerritoryTitle("Nivla Woods");

                TerritoryTitleCore.DisplayedTitle title = core.getDisplayedTitle();
                check(title != null && !title.significant(), "Nivla Woods was displayed as a significant territory");
                check(
                        "Entering Nivla Woods".equals(title.title().getString()),
                        "Wrong title: " + title.title().getString());
                check(title.subtitle() == null, "Regular territories should not have a subtitle");
            });

            RenderHook.setMode(RenderHook.Mode.OVERLAY);
            context.waitTicks(10);
            context.takeScreenshot("wynn-wanderer-title-regular");
            context.runOnClient(client -> checkRendered(overlay));
        } finally {
            context.runOnClient(client -> {
                config.animation.textFadeInTime = fadeInTime;
                config.showOnlySignificantTerritories = onlySignificant;
                core.clearTimer();
            });
        }
    }

    private static void checkRendered(TerritoryTitleOverlay overlay) {
        if (RenderHook.getFailure() != null) {
            throw new AssertionError("Rendering the overlay threw an exception", RenderHook.getFailure());
        }
        check(RenderHook.getRenderCount() > 0, "The HUD was never rendered");
        // Wynntils disables overlays that throw while rendering
        check(Managers.Overlay.isEnabled(overlay), "The overlay crashed while rendering and was disabled by Wynntils");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
