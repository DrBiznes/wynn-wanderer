package me.jamino.wynnWanderer.config;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes the config as a small TOML file, so the mod does not need Cloth Config to load its settings.
 * <p>
 * Every public field of the config object is an option. Booleans, numbers and strings are written as values,
 * any other object becomes a table. This is the same layout Cloth Config's TOML serializer used, so existing
 * config files keep working.
 */
public final class ConfigFile {
    private ConfigFile() {
    }

    /**
     * Reads the file into {@code config}, leaving the defaults in place for anything missing or invalid.
     *
     * @return a description of every line that could not be used, empty if the whole file was read
     */
    public static List<String> read(Path path, Object config) throws IOException {
        return parse(Files.readString(path, StandardCharsets.UTF_8), config);
    }

    public static void write(Path path, Object config) throws IOException {
        Files.createDirectories(path.toAbsolutePath().getParent());
        Path temp = path.resolveSibling(path.getFileName() + ".tmp");
        Files.writeString(temp, format(config), StandardCharsets.UTF_8);
        try {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static String format(Object config) {
        StringBuilder out = new StringBuilder();
        formatTable(config, "", out);
        return out.toString();
    }

    public static List<String> parse(String text, Object config) {
        List<String> problems = new ArrayList<>();
        Object table = config;
        String[] lines = text.split("\\R", -1);

        for (int i = 0; i < lines.length; i++) {
            String line = stripComment(lines[i]).trim();
            if (line.isEmpty()) continue;
            String where = "line " + (i + 1) + ": ";

            try {
                if (line.startsWith("[")) {
                    if (!line.endsWith("]") || line.startsWith("[[")) {
                        throw new IllegalArgumentException("not a table header");
                    }
                    table = findTable(config, line.substring(1, line.length() - 1).trim());
                    continue;
                }

                int equals = line.indexOf('=');
                if (equals < 0) throw new IllegalArgumentException("expected key = value");
                if (table == null) continue; // inside an unknown table, already reported

                String key = unquoteKey(line.substring(0, equals).trim());
                Field field = optionField(table.getClass(), key);
                if (field == null || !isValue(field.getType())) {
                    throw new IllegalArgumentException("unknown option " + key);
                }
                field.set(table, parseValue(line.substring(equals + 1).trim(), field.getType()));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                problems.add(where + e.getMessage());
                if (line.startsWith("[")) table = null;
            }
        }
        return problems;
    }

    private static void formatTable(Object table, String path, StringBuilder out) {
        List<Field> tables = new ArrayList<>();
        boolean headerWritten = path.isEmpty();

        for (Field field : optionFields(table.getClass())) {
            Object value = get(field, table);
            if (!isValue(field.getType())) {
                if (value != null) tables.add(field);
                continue;
            }
            if (!headerWritten) {
                if (!out.isEmpty()) out.append('\n');
                out.append('[').append(path).append("]\n");
                headerWritten = true;
            }
            out.append(field.getName()).append(" = ").append(formatValue(value)).append('\n');
        }

        for (Field field : tables) {
            String childPath = path.isEmpty() ? field.getName() : path + "." + field.getName();
            formatTable(get(field, table), childPath, out);
        }
    }

    private static Object findTable(Object config, String path) {
        Object table = config;
        for (String part : path.split("\\.", -1)) {
            String key = unquoteKey(part.trim());
            Field field = optionField(table.getClass(), key);
            if (field == null || isValue(field.getType())) {
                throw new IllegalArgumentException("unknown table " + path);
            }
            Object child = get(field, table);
            if (child == null) throw new IllegalArgumentException("unknown table " + path);
            table = child;
        }
        return table;
    }

    private static Object parseValue(String raw, Class<?> type) {
        if (type == String.class) return parseString(raw);
        if (type == boolean.class || type == Boolean.class) {
            if (raw.equals("true")) return true;
            if (raw.equals("false")) return false;
            throw new IllegalArgumentException("expected true or false but got " + raw);
        }

        String number = raw.replace("_", "");
        try {
            if (type == int.class || type == Integer.class) return Integer.parseInt(number);
            if (type == long.class || type == Long.class) return Long.parseLong(number);
            double value = switch (number) {
                case "inf", "+inf" -> Double.POSITIVE_INFINITY;
                case "-inf" -> Double.NEGATIVE_INFINITY;
                case "nan", "+nan", "-nan" -> Double.NaN;
                default -> Double.parseDouble(number);
            };
            if (type == float.class || type == Float.class) return (float) value;
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("expected a " + type.getSimpleName() + " but got " + raw);
        }
    }

    private static String formatValue(Object value) {
        if (value instanceof String string) return quote(string);
        if (value instanceof Double || value instanceof Float) {
            double number = ((Number) value).doubleValue();
            if (Double.isNaN(number)) return "nan";
            if (Double.isInfinite(number)) return number > 0 ? "inf" : "-inf";
        }
        return String.valueOf(value);
    }

    private static String quote(String string) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : string.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20 || c == 0x7f) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }

    private static String parseString(String raw) {
        if (raw.length() >= 2 && raw.startsWith("'") && raw.endsWith("'")) {
            return raw.substring(1, raw.length() - 1);
        }
        if (raw.length() < 2 || !raw.startsWith("\"") || !raw.endsWith("\"")) {
            throw new IllegalArgumentException("expected a quoted string but got " + raw);
        }

        StringBuilder out = new StringBuilder();
        for (int i = 1; i < raw.length() - 1; i++) {
            char c = raw.charAt(i);
            if (c != '\\') {
                out.append(c);
                continue;
            }
            if (++i >= raw.length() - 1) throw new IllegalArgumentException("unfinished escape in " + raw);
            char escaped = raw.charAt(i);
            switch (escaped) {
                case '"', '\\' -> out.append(escaped);
                case 'n' -> out.append('\n');
                case 'r' -> out.append('\r');
                case 't' -> out.append('\t');
                case 'b' -> out.append('\b');
                case 'f' -> out.append('\f');
                case 'u', 'U' -> {
                    int length = escaped == 'u' ? 4 : 8;
                    if (i + length >= raw.length()) throw new IllegalArgumentException("bad escape in " + raw);
                    out.appendCodePoint(Integer.parseInt(raw.substring(i + 1, i + 1 + length), 16));
                    i += length;
                }
                default -> throw new IllegalArgumentException("bad escape \\" + escaped + " in " + raw);
            }
        }
        return out.toString();
    }

    private static String unquoteKey(String key) {
        if (key.startsWith("\"") || key.startsWith("'")) return parseString(key);
        return key;
    }

    /** Removes a trailing # comment, ignoring any # inside a string. */
    private static String stripComment(String line) {
        char quote = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quote != 0) {
                if (c == '\\' && quote == '"') {
                    i++;
                } else if (c == quote) {
                    quote = 0;
                }
            } else if (c == '"' || c == '\'') {
                quote = c;
            } else if (c == '#') {
                return line.substring(0, i);
            }
        }
        return line;
    }

    private static boolean isValue(Class<?> type) {
        return type.isPrimitive() || type == String.class || Number.class.isAssignableFrom(type)
                || type == Boolean.class;
    }

    private static List<Field> optionFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Field field : type.getFields()) {
            int modifiers = field.getModifiers();
            if (!Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers) && !Modifier.isTransient(modifiers)) {
                fields.add(field);
            }
        }
        return fields;
    }

    private static Field optionField(Class<?> type, String name) {
        for (Field field : optionFields(type)) {
            if (field.getName().equals(name)) return field;
        }
        return null;
    }

    private static Object get(Field field, Object owner) {
        try {
            return field.get(owner);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
