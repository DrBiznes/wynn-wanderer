package me.jamino.wynnWanderer.pack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeSet;

/**
 * A font of a resource pack, read the way Minecraft reads it, to find which glyph a character gets and how far
 * it moves the text along.
 * <p>
 * Supports the space, bitmap and reference providers. Other providers (ttf, unihex) are treated as having no
 * glyphs.
 */
public final class PackFont {
    private final String id;
    /** Searched in order, the first provider that has a character wins. */
    private final List<Provider> providers;

    private PackFont(String id, List<Provider> providers) {
        this.id = id;
        this.providers = providers;
    }

    static PackFont load(WynncraftPack pack, String id) {
        List<Provider> providers = new ArrayList<>();
        if (!addProviders(pack, id, providers, new TreeSet<>())) return null;
        return new PackFont(id, providers);
    }

    public String id() {
        return id;
    }

    public boolean has(int codePoint) {
        return advance(codePoint).isPresent();
    }

    /**
     * How many pixels the character moves the text along, or empty when the font has no glyph for it.
     */
    public OptionalInt advance(int codePoint) {
        for (Provider provider : providers) {
            OptionalInt advance = provider.advance(codePoint);
            if (advance.isPresent()) return advance;
        }
        return OptionalInt.empty();
    }

    private static boolean addProviders(WynncraftPack pack, String id, List<Provider> providers, Set<String> loading) {
        ResourceId font = ResourceId.of(id);
        JsonObject definition = pack.readJson(font.path("font", ".json"));
        if (definition == null) return false;
        if (!loading.add(font.toString())) {
            throw new IllegalStateException("Font " + font + " references itself");
        }

        for (JsonElement element : definition.getAsJsonArray("providers")) {
            JsonObject provider = element.getAsJsonObject();
            if (!enabled(provider)) continue;

            switch (provider.get("type").getAsString()) {
                case "space" -> providers.add(SpaceProvider.of(provider));
                case "bitmap" -> providers.add(new BitmapProvider(pack, provider));
                case "reference" -> {
                    // Minecraft also ignores references to fonts that do not exist
                    addProviders(pack, provider.get("id").getAsString(), providers, loading);
                }
                default -> {
                    // ttf, unihex and legacy_unicode are not used for the text of the mod
                }
            }
        }

        loading.remove(font.toString());
        return true;
    }

    /**
     * Providers can be limited to some font options (uniform, jp), which are off by default.
     */
    private static boolean enabled(JsonObject provider) {
        if (!provider.has("filter")) return true;
        for (Map.Entry<String, JsonElement> option : provider.getAsJsonObject("filter").entrySet()) {
            if (option.getValue().getAsBoolean()) return false;
        }
        return true;
    }

    private interface Provider {
        OptionalInt advance(int codePoint);
    }

    private record SpaceProvider(Map<Integer, Integer> advances) implements Provider {
        static SpaceProvider of(JsonObject definition) {
            Map<Integer, Integer> advances = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : definition.getAsJsonObject("advances").entrySet()) {
                int[] codePoints = entry.getKey().codePoints().toArray();
                if (codePoints.length != 1) continue;
                advances.put(codePoints[0], Math.round(entry.getValue().getAsFloat()));
            }
            return new SpaceProvider(advances);
        }

        @Override
        public OptionalInt advance(int codePoint) {
            Integer advance = advances.get(codePoint);
            return advance == null ? OptionalInt.empty() : OptionalInt.of(advance);
        }
    }

    private static final class BitmapProvider implements Provider {
        private final WynncraftPack pack;
        private final String file;
        private final int height;
        /** Column and row of each character in the image. */
        private final Map<Integer, int[]> cells = new HashMap<>();
        private final int columns;
        private final int rows;
        private PngImage image;
        private final Map<Integer, Integer> advances = new HashMap<>();

        BitmapProvider(WynncraftPack pack, JsonObject definition) {
            this.pack = pack;
            this.file = definition.get("file").getAsString();
            this.height = definition.has("height") ? definition.get("height").getAsInt() : 8;

            JsonArray chars = definition.getAsJsonArray("chars");
            this.rows = chars.size();
            int width = 0;
            for (int row = 0; row < rows; row++) {
                int[] line = chars.get(row).getAsString().codePoints().toArray();
                width = Math.max(width, line.length);
                for (int column = 0; column < line.length; column++) {
                    // NUL and space leave a cell empty
                    if (line[column] != 0 && line[column] != ' ') {
                        cells.putIfAbsent(line[column], new int[] {column, row});
                    }
                }
            }
            this.columns = width;
        }

        @Override
        public OptionalInt advance(int codePoint) {
            int[] cell = cells.get(codePoint);
            if (cell == null) return OptionalInt.empty();
            return OptionalInt.of(advances.computeIfAbsent(codePoint, c -> computeAdvance(cell[0], cell[1])));
        }

        /**
         * Like Minecraft, the glyph is as wide as its rightmost column that has a pixel in it, plus a pixel of
         * spacing.
         */
        private int computeAdvance(int column, int row) {
            PngImage image = image();
            int cellWidth = image.width / columns;
            int cellHeight = image.height / rows;
            float scale = (float) height / cellHeight;

            int width = 0;
            columns:
            for (int x = cellWidth - 1; x >= 0; x--) {
                for (int y = 0; y < cellHeight; y++) {
                    if (image.coverage(column * cellWidth + x, row * cellHeight + y) != 0) {
                        width = x + 1;
                        break columns;
                    }
                }
            }
            return (int) (0.5 + width * scale) + 1;
        }

        private PngImage image() {
            if (image == null) {
                String path = ResourceId.of(file).path("textures", "");
                byte[] data = pack.read(path);
                if (data == null) throw new IllegalStateException("Missing font texture " + path);
                try {
                    image = PngImage.read(data);
                } catch (RuntimeException e) {
                    throw new IllegalStateException("Could not read the font texture " + path, e);
                }
            }
            return image;
        }
    }

    private record ResourceId(String namespace, String path) {
        static ResourceId of(String id) {
            int colon = id.indexOf(':');
            return colon < 0 ? new ResourceId("minecraft", id) : new ResourceId(id.substring(0, colon), id.substring(colon + 1));
        }

        String path(String folder, String extension) {
            return "assets/" + namespace + "/" + folder + "/" + path + extension;
        }

        @Override
        public String toString() {
            return namespace + ":" + path;
        }
    }
}
