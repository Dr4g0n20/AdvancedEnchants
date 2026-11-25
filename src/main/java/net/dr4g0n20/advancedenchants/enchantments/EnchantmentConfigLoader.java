package net.dr4g0n20.advancedenchants.enchantments;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;

/** Loads enchantment definitions from category-based YAML files. */
public final class EnchantmentConfigLoader {

    private final JavaPlugin plugin;

    public EnchantmentConfigLoader(JavaPlugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null.");
        }
        this.plugin = plugin;
    }

    public int loadInto(EnchantmentRegistry registry) {
        File root = new File(plugin.getDataFolder(), "enchantments");
        if (!root.exists() && !root.mkdirs()) {
            plugin.getLogger().severe("Could not create enchantments configuration directory.");
            return 0;
        }

        List<File> files = new ArrayList<File>();
        collectYamlFiles(root, files);
        int loaded = 0;

        for (File file : files) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection section = yaml.getConfigurationSection("enchantments");
            if (section == null) {
                continue;
            }

            String category = yaml.getString("category", "uncategorised");
            for (String id : section.getKeys(false)) {
                ConfigurationSection data = section.getConfigurationSection(id);
                if (data == null) {
                    plugin.getLogger().warning("Ignoring malformed enchantment '" + id
                            + "' in " + file.getName() + ".");
                    continue;
                }

                try {
                    EnchantmentDefinition definition = parseDefinition(id, category, data);
                    if (registry.register(definition)) {
                        loaded++;
                    } else {
                        plugin.getLogger().warning("Duplicate enchantment id '" + id
                                + "' in " + file.getName() + "; later definition ignored.");
                    }
                } catch (IllegalArgumentException ex) {
                    plugin.getLogger().log(Level.WARNING, "Could not load enchantment '"
                            + id + "' from " + file.getName() + ": " + ex.getMessage());
                }
            }
        }

        return loaded;
    }

    private EnchantmentDefinition parseDefinition(String id, String category,
                                                   ConfigurationSection data) {
        String displayName = data.getString("display-name", id);
        int maximumLevel = data.getInt("maximum-level", 1);
        String description = data.getString("description", "");
        String rarity = data.getString("rarity", "COMMON");
        String activation = data.getString("activation.type", "EVENT");
        double chance = data.getDouble("activation.chance", 100.0D);
        int cooldown = data.getInt("activation.cooldown-seconds", 0);

        Set<Material> materials = EnumSet.noneOf(Material.class);
        for (String materialName : data.getStringList("applicable-items")) {
            try {
                materials.add(Material.valueOf(materialName.toUpperCase(Locale.ENGLISH)));
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Unknown 1.12.2 material '" + materialName
                        + "' on enchantment '" + id + "'.");
            }
        }

        List<String> conflicts = data.getStringList("incompatible-with");
        ConfigurationSection effects = data.getConfigurationSection("effects");
        java.util.Map<String, Object> effectSettings = effects == null
                ? java.util.Collections.<String, Object>emptyMap()
                : effects.getValues(true);

        if (materials.isEmpty()) {
            throw new IllegalArgumentException("At least one applicable item is required.");
        }

        return new EnchantmentDefinition(id, displayName, maximumLevel, category,
                description, rarity, activation, chance, cooldown, materials,
                new java.util.HashSet<String>(conflicts), effectSettings);
    }

    private void collectYamlFiles(File directory, List<File> files) {
        File[] children = directory.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            if (child.isDirectory()) {
                collectYamlFiles(child, files);
            } else if (child.isFile() && child.getName().toLowerCase(Locale.ENGLISH).endsWith(".yml")) {
                files.add(child);
            }
        }
    }
}
