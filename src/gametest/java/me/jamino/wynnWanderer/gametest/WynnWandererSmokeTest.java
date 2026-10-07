package me.jamino.wynnWanderer.gametest;

import com.wynntils.core.components.Models;
import me.jamino.wynnWanderer.WynnWanderer;
import me.jamino.wynnWanderer.config.WynnWandererConfig;
import me.jamino.wynnWanderer.features.TerritoryTitleCore;
import me.shedaniel.autoconfig.AutoConfigClient;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Starts the real client with Wynntils installed and checks that the mod loads, that the config
 * screen opens, and that territory titles render without crashing.
 * <p>
 * Run with {@code gradlew runClientGameTest}. Screenshots are saved to build/run/clientGameTest/screenshots.
 */
public class WynnWandererSmokeTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        modIsInitialized(context);
        configScreenOpens(context);
        titlesRenderInWorld(context);
    }

    private void modIsInitialized(ClientGameTestContext context) {
        context.runOnClient(client -> {
            check(WynnWanderer.getTerritoryTitleCore() != null, "The territory title core was not initialized");
            check(WynnWanderer.getConfig() != null, "The config was not loaded");
            // The territories come from Wynntils, make sure the parts of it we use are there
            check(Models.Territory != null && Models.WorldState != null, "Wynntils was not initialized");
        });
    }

    private void configScreenOpens(ClientGameTestContext context) {
        context.setScreen(() -> AutoConfigClient.getConfigScreen(WynnWandererConfig.class, null)
                .get());
        context.waitTicks(5);
        context.takeScreenshot("wynn-wanderer-config");
        context.setScreen(() -> null);
    }

    private void titlesRenderInWorld(ClientGameTestContext context) {
        WynnWandererConfig.TerritoryTitlesConfig config = WynnWanderer.getConfig().territoryTitles;
        WynnWandererConfig.TerritoryTitlesConfig.PositioningConfig positioning = config.positioning;
        TerritoryTitleCore core = WynnWanderer.getTerritoryTitleCore();

        int fadeInTime = config.animation.textFadeInTime;
        int textXOffset = positioning.textXOffset;
        int textYOffset = positioning.textYOffset;
        int subtitleXOffset = positioning.subtitleXOffset;
        int subtitleYOffset = positioning.subtitleYOffset;
        boolean centerText = positioning.centerText;

        // The HUD is only rendered while in a world
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getClientWorld().waitForChunksRender();

            context.runOnClient(client -> {
                // The title timers only run on Wynncraft, so skip the fade-in to get a fully opaque title
                config.animation.textFadeInTime = 0;
                // The default offsets are meant for larger windows than the one the tests run in
                positioning.centerText = true;
                positioning.textXOffset = 0;
                positioning.textYOffset = -80;
                positioning.subtitleXOffset = 0;
                positioning.subtitleYOffset = -50;

                core.displayTerritoryTitle("Detlas");

                TerritoryTitleCore.DisplayedTitle title = core.getDisplayedTitle();
                check(title != null && title.significant(), "Detlas was not displayed as a significant territory");
                check("Detlas".equals(title.title().getString()), "Wrong title: " + title.title().getString());
                check(
                        "Heart of the Province".equals(title.subtitle().getString()),
                        "Wrong subtitle: " + title.subtitle().getString());
                check(title.color() == 0x669933, "Wrong color: " + Integer.toHexString(title.color()));
            });
            checkRendersAndScreenshot(context, core, "wynn-wanderer-title-significant");

            // Top left of the screen, to check that position changes are picked up
            context.runOnClient(client -> {
                positioning.centerText = false;
                positioning.textXOffset = 10;
                positioning.textYOffset = 10;
                positioning.subtitleXOffset = 10;
                positioning.subtitleYOffset = 40;
            });
            checkRendersAndScreenshot(context, core, "wynn-wanderer-title-moved");

            context.runOnClient(client -> {
                positioning.centerText = true;
                positioning.textXOffset = 0;
                positioning.textYOffset = -80;

                core.displayTerritoryTitle("Nivla Woods");

                TerritoryTitleCore.DisplayedTitle title = core.getDisplayedTitle();
                check(title != null && !title.significant(), "Nivla Woods was displayed as a significant territory");
                check(
                        "Entering Nivla Woods".equals(title.title().getString()),
                        "Wrong title: " + title.title().getString());
                check(title.subtitle() == null, "Regular territories should not have a subtitle");
            });
            checkRendersAndScreenshot(context, core, "wynn-wanderer-title-regular");

            // Nothing is rendered once the title is cleared
            context.runOnClient(client -> core.clearTimer());
            context.waitTicks(2);
            int renderCount = context.computeOnClient(
                    client -> core.getTerritoryRenderer().getRenderCount());
            context.waitTicks(5);
            context.runOnClient(client -> check(
                    core.getTerritoryRenderer().getRenderCount() == renderCount,
                    "A title was rendered after it was cleared"));
        } finally {
            context.runOnClient(client -> {
                config.animation.textFadeInTime = fadeInTime;
                positioning.textXOffset = textXOffset;
                positioning.textYOffset = textYOffset;
                positioning.subtitleXOffset = subtitleXOffset;
                positioning.subtitleYOffset = subtitleYOffset;
                positioning.centerText = centerText;
                core.clearTimer();
            });
        }
    }

    private void checkRendersAndScreenshot(ClientGameTestContext context, TerritoryTitleCore core, String name) {
        int renderCount = context.computeOnClient(
                client -> core.getTerritoryRenderer().getRenderCount());
        context.waitTicks(10);
        context.takeScreenshot(name);
        context.runOnClient(client -> check(
                core.getTerritoryRenderer().getRenderCount() > renderCount, "The title was not rendered: " + name));
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
