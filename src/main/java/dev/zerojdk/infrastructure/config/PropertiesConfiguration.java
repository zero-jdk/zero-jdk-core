package dev.zerojdk.infrastructure.config;

import lombok.NonNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A minimal, insertion-order–preserving key-value configuration.
 *
 * <h2>File format</h2>
 * <ul>
 *   <li><strong>Delimiter</strong> - Each non-blank line must contain a single key/value pair
 *       separated by the <code>=</code> character. Other delimiters such as <code>:</code>,
 *       whitespace, or escape sequences (<code>\\uXXXX</code>, <code>\:</code>, ...) are
 *       <em>not</em> recognised.</li>
 *   <li><strong>Comments</strong> – Lines whose first non-blank character is <code>#</code> are
 *       treated as comments and skipped.</li>
 *   <li><strong>Duplicate keys</strong> - If a key appears more than once, the <em>last</em>
 *       occurrence silently overwrites any earlier value.</li>
 *   <li><strong>Encoding</strong> – Files are always read from and written to disk using UTF-8.</li>
 * </ul>
 *
 * <h2>Thread-safety</h2>
 * <p>
 * Instances of this class are <strong>not thread-safe</strong>.
 * </p>
 *
 * <p>This class is deliberately lightweight and does <em>not</em> attempt to be a drop-in
 * replacement for {@link java.util.Properties}.
 */
public final class PropertiesConfiguration {
    private final Map<String, String> values;

    public PropertiesConfiguration() {
        this(new LinkedHashMap<>());
    }

    private PropertiesConfiguration(Map<String, String> values) {
        this.values = values;
    }

    public static PropertiesConfiguration from(Path file) throws IOException {
        LinkedHashMap<String, String> values = new LinkedHashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(file, UTF_8)) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.strip();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int idx = line.indexOf('=');

                if (idx <= 0) {
                    continue;
                }

                String key = line.substring(0, idx).strip();
                String value = line.substring(idx + 1).strip();

                values.put(key, value);
            }
        }

        return new PropertiesConfiguration(values);
    }

    public String getString(@NonNull String key) {
        return values.get(key);
    }

    public void addProperty(@NonNull String key, @NonNull Object value) {
        addProperty(key, value.toString());
    }

    public void addProperty(@NonNull String key, @NonNull String value) {
        values.put(key, value);
    }

    public void save(Path file) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, UTF_8)) {
            for (Map.Entry<String, String> entry : values.entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue());
                writer.newLine();
            }
        }
    }
}