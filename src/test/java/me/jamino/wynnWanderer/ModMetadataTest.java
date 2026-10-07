package me.jamino.wynnWanderer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Checks that the metadata packaged into the jar points at things that exist.
 */
class ModMetadataTest {
    private final JsonObject modJson = TestResources.readJson("/fabric.mod.json");

    @Test
    void buildPropertiesAreExpanded() {
        assertEquals("wynn-wanderer", modJson.get("id").getAsString());
        assertFalse(modJson.toString().contains("${"), "Unexpanded property in fabric.mod.json");
        assertTrue(modJson.get("version").getAsString().matches("\\d+\\.\\d+\\.\\d+.*"));
    }

    @Test
    void dependsOnEverythingTheModUses() {
        JsonObject depends = modJson.getAsJsonObject("depends");

        for (String dependency : List.of("fabricloader", "fabric", "minecraft", "cloth-config", "wynntils")) {
            assertTrue(depends.has(dependency), "Missing dependency on " + dependency);
        }
        assertTrue(depends.get("minecraft").getAsString().startsWith("1.21.11"));
    }

    @Test
    void entrypointsExist() {
        List<String> entrypoints = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry :
                modJson.getAsJsonObject("entrypoints").entrySet()) {
            entry.getValue().getAsJsonArray().forEach(element -> entrypoints.add(element.getAsString()));
        }

        assertTrue(entrypoints.contains(WynnWanderer.class.getName()));
        for (String entrypoint : entrypoints) {
            assertClassExists(entrypoint);
        }
    }

    @Test
    void mixinsExist() {
        for (JsonElement mixinConfig : modJson.getAsJsonArray("mixins")) {
            JsonObject mixinJson = TestResources.readJson("/" + mixinConfig.getAsString());
            String mixinPackage = mixinJson.get("package").getAsString();

            List<String> mixins = new ArrayList<>();
            for (String side : List.of("mixins", "client", "server")) {
                if (!mixinJson.has(side)) continue;
                mixinJson.getAsJsonArray(side).forEach(element -> mixins.add(element.getAsString()));
            }

            for (String mixin : mixins) {
                assertClassExists(mixinPackage + "." + mixin);
            }
        }
    }

    // Mixins and entrypoints can not be loaded outside of the game, so only check that they were compiled
    private static void assertClassExists(String className) {
        String path = "/" + className.replace('.', '/') + ".class";
        assertNotNull(ModMetadataTest.class.getResource(path), "Missing class " + className);
    }
}
