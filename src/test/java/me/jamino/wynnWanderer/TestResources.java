package me.jamino.wynnWanderer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads the processed resources of the mod, as they are packaged into the jar.
 */
public final class TestResources {
    public static final String LANG_PATH = "/assets/wynn-wanderer/lang/en_us.json";

    private TestResources() {}

    public static JsonObject readJson(String path) {
        InputStream stream = TestResources.class.getResourceAsStream(path);
        assertNotNull(stream, "Missing resource " + path);

        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Map<String, String> readLang() {
        Map<String, String> translations = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : readJson(LANG_PATH).entrySet()) {
            translations.put(entry.getKey(), entry.getValue().getAsString());
        }
        return translations;
    }
}
