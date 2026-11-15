package me.jamino.wynnWanderer.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import me.jamino.wynnWanderer.WynnWanderer;
import me.jamino.wynnWanderer.config.gui.ClothConfigData;
import org.junit.jupiter.api.Test;

/**
 * Cloth Config and Mod Menu are optional, so the mod must not load any of their classes unless they are installed.
 */
class OptionalConfigScreenTest {
    private static final String CONFIG_SCREEN_PACKAGE = "me/jamino/wynnWanderer/config/gui/";

    @Test
    void onlyTheConfigScreenUsesClothConfigOrModMenu() throws IOException {
        List<String> offenders = new ArrayList<>();
        for (Path classFile : mainClassFiles()) {
            String name = classesDir().relativize(classFile).toString().replace('\\', '/');
            if (name.startsWith(CONFIG_SCREEN_PACKAGE)) continue;

            // Class names in the constant pool are stored as plain text
            String constants = new String(Files.readAllBytes(classFile), StandardCharsets.ISO_8859_1)
                    // Annotations are skipped by Java when their class is missing, so the config can keep them
                    .replace("me/shedaniel/autoconfig/annotation/", "");
            if (constants.contains("me/shedaniel") || constants.contains("com/terraformersmc")) {
                offenders.add(name);
            }
        }

        assertEquals(List.of(), offenders);
    }

    @Test
    void clothConfigSeesTheSameOptionsAsTheConfig() {
        assertEquals(optionFields(WynnWandererConfig.class), optionFields(ClothConfigData.class));
    }

    private static List<String> optionFields(Class<?> type) {
        List<String> fields = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                fields.add(field.getType().getName() + " " + field.getName());
            }
        }
        return fields;
    }

    private static List<Path> mainClassFiles() throws IOException {
        try (Stream<Path> files = Files.walk(classesDir())) {
            return files.filter(path -> path.toString().endsWith(".class")).toList();
        }
    }

    private static Path classesDir() {
        try {
            // Walk up from me/jamino/wynnWanderer/WynnWanderer.class to the root of the compiled classes
            Path dir = Path.of(WynnWanderer.class.getResource("WynnWanderer.class").toURI());
            for (int i = 0; i < WynnWanderer.class.getName().split("\\.").length; i++) {
                dir = dir.getParent();
            }
            return dir;
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
