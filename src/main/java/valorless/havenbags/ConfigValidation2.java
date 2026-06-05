package valorless.havenbags;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.logging.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles configuration validation.
 * <p>
 * Rather than hardcoding default values, this class reads the bundled
 * {@link Config} from the JAR at runtime and registers every leaf
 * key it finds as a {@link Config#addValidationEntry} call, comparing it
 * against the user's live config so any missing keys are written with their
 * defaults.
 * </p>
 * <p>
 * Comments are preserved: block comments attached to a YAML section header are
 * forwarded to the first leaf discovered inside that section; block and inline
 * comments on leaf nodes are forwarded directly to their own entry.
 * </p>
 */
public final class ConfigValidation2 {

    private ConfigValidation2() {}

    /**
     * Reads the bundled default {@link Config} file from the JAR, then
     * registers every leaf key as a validation entry — with its default value
     * and any associated comments — before calling {@link Config#validate()}.
     *
     * <p>Block comments that sit above a YAML section header are collected and
     * prepended to the first leaf key encountered in that section so they are
     * not silently discarded.</p>
     *
     * @param conf the path to the config file to validate (e.g. {@code "abyss/config.yml"})
     * @return a Config instance with all validation entries registered and validated
     */
    public static Config validateAndGetConfig(String conf) {
        return validateAndGetConfig(conf, new ArrayList<>());
    }

    /**
     * Reads the bundled default {@link Config} file from the JAR, then
     * registers every leaf key as a validation entry — with its default value
     * and any associated comments — before calling {@link Config#validate()}.
     *
     * <p>Block comments that sit above a YAML section header are collected and
     * prepended to the first leaf key encountered in that section so they are
     * not silently discarded.</p>
     *
     * @param conf        the path to the config file to validate (e.g. {@code "abyss/config.yml"})
     * @param ignoredKeys keys (or key prefixes) to skip during validation —
     *                    useful for template/example entries that should not be
     *                    re-added to the user's config
     * @return a Config instance with all validation entries registered and validated
     */
    public static Config validateAndGetConfig(String conf, List<String> ignoredKeys) {
        JavaPlugin plugin = Main.plugin;
        Config config = new Config(plugin, conf);
        InputStream resource = plugin.getResource(conf);
        if (resource == null) {
            Log.warning(plugin, "Default " + conf +" not found in JAR – skipping validation.");
            config.validate();
            return config;
        }

        YamlConfiguration defaults = new YamlConfiguration();
        try {
            defaults.options().parseComments(true);
            defaults.load(new InputStreamReader(resource, StandardCharsets.UTF_8));
        } catch (IOException | InvalidConfigurationException e) {
            Log.warning(plugin, "Failed to parse default \"" + conf + "\": " + e.getMessage());
            config.validate();
            return config;
        }

        // Track which section paths have already had their block comments forwarded,
        // so we only attach them to the *first* leaf encountered in each section.
        Set<String> usedSectionComments = new HashSet<>();

        for (String key : defaults.getKeys(true)) {
            Object value = defaults.get(key);
            if (value instanceof ConfigurationSection) continue;

            // Skip any key that matches or starts with an ignored prefix.
            boolean skip = false;
            for (String ignored : ignoredKeys) {
                if (key.equals(ignored) || key.startsWith(ignored + ".")) {
                    skip = true;
                    break;
                }
            }
            if (skip) continue;

            List<String> allComments = new ArrayList<>();

            // Walk every ancestor section (outermost → innermost) and collect
            // block comments from any section whose comments haven't been used yet.
            // This ensures file-level / section-header comments aren't lost.
            String[] parts = key.split("\\.");
            StringBuilder sectionPath = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {
                if (i > 0) sectionPath.append('.');
                sectionPath.append(parts[i]);
                String sp = sectionPath.toString();
                if (!usedSectionComments.contains(sp)) {
                    List<String> sc = defaults.getComments(sp);
                    if (!sc.isEmpty()) allComments.addAll(sc);
                    usedSectionComments.add(sp);
                }
            }

            // Append the leaf's own block comments, then its inline comment.
            allComments.addAll(defaults.getComments(key));
            allComments.addAll(defaults.getInlineComments(key));

            // Bukkit can include null or blank entries (e.g. blank comment lines);
            // filter them out so they don't produce "# null" or empty comment lines.
            allComments.removeIf(c -> c == null || c.isBlank());

            if (allComments.isEmpty()) {
                config.addValidationEntry(key, value);
            } else {
                // Use the List<String> overload so each element becomes its own
                // comment line with a proper # prefix, rather than joining into a
                // single multi-line string where only the first line gets prefixed.
                config.addValidationEntry(key, value, allComments);
            }
        }

        config.validate();
        return config;
    }
}
