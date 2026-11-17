package me.jamino.wynnWanderer.pack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.Assumptions;

/**
 * The Wynncraft resource pack, to check the mod against what the server sends to players right now.
 * <p>
 * Wynncraft only hands out its pack to the Minecraft client: the server sends its URL and SHA-1 on join, and
 * rp-cdn.wynncraft.com blocks other downloads. So instead of downloading it, the tests use the newest Wynncraft
 * pack that a Minecraft launcher on this machine downloaded, which Minecraft records with its URL and SHA-1 in
 * {@code <game dir>/downloads/log.json}. Joining Wynncraft once is enough to get the latest pack.
 * <p>
 * The SHA-1 of the file is always checked against the one the server sent, so the tests never run against a
 * partial or modified download.
 * <p>
 * Gradle properties (passed to the tests as system properties):
 * <ul>
 *     <li>{@code -PwynncraftPack=<zip>}: use this pack instead. Its SHA-1 is read from its name
 *     ({@code PRODUCTION_<sha1>.zip} or {@code <sha1>}) or given with {@code -PwynncraftPackSha1}.</li>
 *     <li>{@code -PwynncraftPackSha1=<sha1>}: use this version of the pack from the launcher downloads, instead of
 *     the newest one.</li>
 *     <li>{@code -PwynncraftPackDirs=<dir>[;<dir>...]}: also look for downloads in these game directories, or in
 *     the instances inside them.</li>
 *     <li>{@code -PrequireWynncraftPack}: fail instead of skipping the tests when there is no pack.</li>
 * </ul>
 */
public final class WynncraftPack implements AutoCloseable {
    public static final String PROPERTY_PREFIX = "wynnWanderer.";
    /** The resource pack format of Minecraft 1.21.11, which picks the overlays of the pack that apply. */
    public static final int PACK_FORMAT = 75;

    private static final Pattern WYNNCRAFT_URL = Pattern.compile("https://rp-cdn\\.wynncraft\\.com/[A-Za-z]+_([0-9a-f]{40})\\.zip");
    private static final Pattern SHA1 = Pattern.compile("(?<![0-9a-fA-F])([0-9a-fA-F]{40})(?![0-9a-fA-F])");

    private static WynncraftPack instance;

    private final Path file;
    private final String sha1;
    private final String origin;
    private final ZipFile zip;
    private final JsonObject metadata;
    /** The directories of the pack that resources are read from, the first one wins. */
    private final List<String> layers = new ArrayList<>();
    private final Map<String, PackFont> fonts = new LinkedHashMap<>();

    private WynncraftPack(Path file, String sha1, String origin) throws IOException {
        this.file = file;
        this.sha1 = sha1;
        this.origin = origin;

        String actual = sha1(file);
        assertEquals(sha1, actual, "SHA-1 of the Wynncraft resource pack " + file + " (" + origin
                + ") does not match the one it was published with, the file is broken or was changed. "
                + "Rejoin Wynncraft to download it again.");

        this.zip = new ZipFile(file.toFile());
        byte[] mcmeta = readRoot("pack.mcmeta");
        if (mcmeta == null) fail("The Wynncraft resource pack " + file + " has no pack.mcmeta");
        this.metadata = JsonParser.parseString(new String(mcmeta, StandardCharsets.UTF_8)).getAsJsonObject();

        // Overlays that are listed later are applied on top of the earlier ones
        if (metadata.has("overlays")) {
            for (JsonElement entry : metadata.getAsJsonObject("overlays").getAsJsonArray("entries")) {
                JsonObject overlay = entry.getAsJsonObject();
                if (supports(overlay, PACK_FORMAT)) {
                    layers.add(0, overlay.get("directory").getAsString() + "/");
                }
            }
        }
        layers.add("");
    }

    /**
     * The Wynncraft resource pack, or skips the test when there is none and it is not required.
     */
    public static synchronized WynncraftPack get() {
        if (instance == null) {
            instance = find().orElse(null);
            if (instance == null) {
                String message = "No Wynncraft resource pack found. Join Wynncraft once with any launcher, or pass "
                        + "-PwynncraftPack=<zip> or -PwynncraftPackDirs=<game dir>. Searched for downloads/log.json in: "
                        + searchDirectories();
                if (Boolean.parseBoolean(property("requireWynncraftPack", "false"))) fail(message);
                Assumptions.abort(message);
            }
            System.out.println("Testing against the Wynncraft resource pack " + instance.describe());
        }
        return instance;
    }

    public String sha1() {
        return sha1;
    }

    public JsonObject metadata() {
        return metadata;
    }

    /** The overlays that Minecraft applies on top of the pack, from the top one. */
    public List<String> overlays() {
        return layers.subList(0, layers.size() - 1);
    }

    /**
     * Reads a file of the pack the way Minecraft sees it, with the overlays applied, or returns null.
     */
    public byte[] read(String path) {
        for (String layer : layers) {
            byte[] data = readRoot(layer + path);
            if (data != null) return data;
        }
        return null;
    }

    public JsonObject readJson(String path) {
        byte[] data = read(path);
        return data == null ? null : JsonParser.parseString(new String(data, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    /**
     * The font with this id, such as {@code minecraft:banner/pill}, or null when the pack does not have it.
     */
    public synchronized PackFont font(String id) {
        if (!fonts.containsKey(id)) {
            fonts.put(id, PackFont.load(this, id));
        }
        return fonts.get(id);
    }

    @Override
    public void close() throws IOException {
        zip.close();
    }

    private byte[] readRoot(String path) {
        ZipEntry entry = zip.getEntry(path);
        if (entry == null || entry.isDirectory()) return null;
        try (InputStream in = zip.getInputStream(entry)) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + path + " from " + file, e);
        }
    }

    private String describe() {
        StringBuilder description = new StringBuilder(sha1);
        JsonElement text = metadata.getAsJsonObject("pack").get("description");
        if (text != null) {
            description.append(" (").append(plainText(text).replace('\n', ' ').trim()).append(")");
        }
        return description.append(", ").append(origin).append(": ").append(file).toString();
    }

    private static String plainText(JsonElement component) {
        if (component.isJsonPrimitive()) return component.getAsString();
        StringBuilder text = new StringBuilder();
        if (component.isJsonArray()) {
            for (JsonElement part : component.getAsJsonArray()) text.append(plainText(part));
        } else if (component.isJsonObject()) {
            JsonObject object = component.getAsJsonObject();
            if (object.has("text")) text.append(object.get("text").getAsString());
            if (object.has("extra")) text.append(plainText(object.get("extra")));
        }
        return text.toString();
    }

    // Finding the pack

    private record Download(Path file, String sha1, Instant time, String url) {}

    private static Optional<WynncraftPack> find() {
        String sha1 = property("wynncraftPackSha1", "").trim().toLowerCase(Locale.ROOT);
        String explicit = property("wynncraftPack", "").trim();

        try {
            if (!explicit.isEmpty()) {
                Path path = Paths.get(explicit);
                assertTrue(Files.isRegularFile(path), "-PwynncraftPack " + path + " is not a file");
                if (sha1.isEmpty()) {
                    Matcher matcher = SHA1.matcher(path.getFileName().toString());
                    assertTrue(matcher.find(), "Pass the SHA-1 that Wynncraft sent for " + path + " with -PwynncraftPackSha1");
                    sha1 = matcher.group(1).toLowerCase(Locale.ROOT);
                }
                return Optional.of(new WynncraftPack(path, sha1, "given with -PwynncraftPack"));
            }

            List<Download> downloads = findDownloads();
            if (!sha1.isEmpty()) {
                String wanted = sha1;
                Download download = downloads.stream()
                        .filter(candidate -> candidate.sha1().equals(wanted))
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("No download of the Wynncraft resource pack " + wanted
                                + " in " + searchDirectories()));
                return Optional.of(new WynncraftPack(download.file(), download.sha1(), "downloaded " + download.time()));
            }

            Optional<Download> newest = downloads.stream().max(Comparator.comparing(Download::time));
            if (newest.isEmpty()) return Optional.empty();
            Download download = newest.get();
            return Optional.of(new WynncraftPack(download.file(), download.sha1(), "newest download, " + download.time()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Every Wynncraft pack that Minecraft downloaded and still has, read from the download logs.
     */
    private static List<Download> findDownloads() {
        List<Download> downloads = new ArrayList<>();
        for (Path directory : searchDirectories()) {
            for (Path log : downloadLogs(directory)) {
                downloads.addAll(readLog(log));
            }
        }
        return downloads;
    }

    private static List<Download> readLog(Path log) {
        List<Download> downloads = new ArrayList<>();
        List<String> lines;
        try {
            lines = Files.readAllLines(log, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return downloads;
        }

        for (String line : lines) {
            if (line.isBlank()) continue;
            try {
                JsonObject entry = JsonParser.parseString(line).getAsJsonObject();
                if (!entry.has("url") || !entry.has("hash") || !entry.has("file") || !entry.has("time")) continue;

                String url = entry.get("url").getAsString();
                Matcher matcher = WYNNCRAFT_URL.matcher(url);
                if (!matcher.matches()) continue;

                String hash = entry.get("hash").getAsString().toLowerCase(Locale.ROOT);
                // Wynncraft names the pack after its SHA-1, so both have to agree
                if (!hash.equals(matcher.group(1))) continue;

                JsonObject fileInfo = entry.getAsJsonObject("file");
                String name = fileInfo.get("name").getAsString().replace('\\', '/');
                Path file = log.resolveSibling(name);
                if (!Files.isRegularFile(file)) continue;
                if (fileInfo.has("size") && Files.size(file) != fileInfo.get("size").getAsLong()) continue;

                downloads.add(new Download(file, hash, Instant.parse(entry.get("time").getAsString()), url));
            } catch (RuntimeException | IOException e) {
                // Lines of other Minecraft versions or broken lines, Minecraft skips them too
            }
        }
        return downloads;
    }

    /** The download logs of this game directory and of the instances inside it. */
    private static List<Path> downloadLogs(Path directory) {
        List<Path> logs = new ArrayList<>();
        addLog(logs, directory);
        if (Files.isDirectory(directory)) {
            try (DirectoryStream<Path> children = Files.newDirectoryStream(directory, Files::isDirectory)) {
                for (Path child : children) {
                    addLog(logs, child);
                    addLog(logs, child.resolve("minecraft"));
                    addLog(logs, child.resolve(".minecraft"));
                }
            } catch (IOException e) {
                // Not readable, there is nothing to find there
            }
        }
        return logs;
    }

    private static void addLog(List<Path> logs, Path gameDirectory) {
        Path log = gameDirectory.resolve("downloads").resolve("log.json");
        if (Files.isRegularFile(log)) logs.add(log);
    }

    /** Game directories of the common launchers, and the folders that hold their instances. */
    private static List<Path> searchDirectories() {
        Set<Path> directories = new LinkedHashSet<>();
        for (String extra : property("wynncraftPackDirs", "").split(Pattern.quote(File.pathSeparator) + "|;")) {
            if (!extra.isBlank()) directories.add(Paths.get(extra.trim()));
        }

        Path home = Paths.get(System.getProperty("user.home"));
        List<Path> dataHomes = new ArrayList<>();
        String appData = System.getenv("APPDATA");
        if (appData != null) dataHomes.add(Paths.get(appData));
        String xdgData = System.getenv("XDG_DATA_HOME");
        dataHomes.add(xdgData != null ? Paths.get(xdgData) : home.resolve(".local/share"));
        dataHomes.add(home.resolve("Library/Application Support"));

        for (Path data : dataHomes) {
            directories.add(data.resolve(".minecraft"));
            directories.add(data.resolve("minecraft"));
            directories.add(data.resolve("ModrinthApp/profiles"));
            directories.add(data.resolve("com.modrinth.theseus/profiles"));
            directories.add(data.resolve("PrismLauncher/instances"));
            directories.add(data.resolve("MultiMC/instances"));
            directories.add(data.resolve("gdlauncher_carbon/data/instances"));
        }
        directories.add(home.resolve(".minecraft"));
        directories.add(home.resolve("curseforge/minecraft/Instances"));
        directories.add(home.resolve(".var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/instances"));
        // The dev client of this project
        directories.add(Paths.get("run"));
        directories.add(Paths.get("build/run"));

        return directories.stream().filter(Files::isDirectory).map(Path::toAbsolutePath).toList();
    }

    private static String property(String name, String fallback) {
        return System.getProperty(PROPERTY_PREFIX + name, fallback);
    }

    private static String sha1(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            try (InputStream in = Files.newInputStream(file)) {
                byte[] buffer = new byte[1 << 16];
                for (int read; (read = in.read(buffer)) > 0; ) {
                    digest.update(buffer, 0, read);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // pack.mcmeta

    /**
     * Whether an overlay entry or the pack section of pack.mcmeta supports this pack format.
     */
    public static boolean supports(JsonObject entry, int format) {
        if (entry.has("min_format") || entry.has("max_format")) {
            int min = entry.has("min_format") ? majorFormat(entry.get("min_format")) : Integer.MIN_VALUE;
            int max = entry.has("max_format") ? majorFormat(entry.get("max_format")) : Integer.MAX_VALUE;
            return format >= min && format <= max;
        }
        JsonElement formats = entry.has("formats") ? entry.get("formats") : entry.get("supported_formats");
        if (formats != null) {
            if (formats.isJsonPrimitive()) return formats.getAsInt() == format;
            if (formats.isJsonArray()) {
                JsonArray range = formats.getAsJsonArray();
                return format >= range.get(0).getAsInt() && format <= range.get(range.size() - 1).getAsInt();
            }
            JsonObject range = formats.getAsJsonObject();
            return format >= range.get("min_inclusive").getAsInt() && format <= range.get("max_inclusive").getAsInt();
        }
        return entry.has("pack_format") && entry.get("pack_format").getAsInt() == format;
    }

    /** Formats are written as 75 or [75, 0] for 75.0. */
    private static int majorFormat(JsonElement format) {
        return format.isJsonArray() ? format.getAsJsonArray().get(0).getAsInt() : format.getAsInt();
    }
}
