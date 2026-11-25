package net.dr4g0n20.advancedenchants.enchantments;

import org.bukkit.Material;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Immutable data model for a configurable custom enchantment. */
public final class EnchantmentDefinition {

    private final String id;
    private final String displayName;
    private final int maximumLevel;
    private final String category;
    private final String description;
    private final String rarity;
    private final String activation;
    private final double chance;
    private final int cooldownSeconds;
    private final Set<Material> applicableMaterials;
    private final Set<String> incompatibleIds;
    private final Map<String, Object> effectSettings;

    public EnchantmentDefinition(String id, String displayName, int maximumLevel,
                                 String category, String description) {
        this(id, displayName, maximumLevel, category, description, "COMMON",
                "EVENT", 100.0D, 0, Collections.<Material>emptySet(),
                Collections.<String>emptySet(), Collections.<String, Object>emptyMap());
    }

    public EnchantmentDefinition(String id, String displayName, int maximumLevel,
                                 String category, String description,
                                 Set<Material> applicableMaterials,
                                 Set<String> incompatibleIds) {
        this(id, displayName, maximumLevel, category, description, "COMMON",
                "EVENT", 100.0D, 0, applicableMaterials, incompatibleIds,
                Collections.<String, Object>emptyMap());
    }

    public EnchantmentDefinition(String id, String displayName, int maximumLevel,
                                 String category, String description, String rarity,
                                 String activation, double chance, int cooldownSeconds,
                                 Set<Material> applicableMaterials,
                                 Set<String> incompatibleIds,
                                 Map<String, Object> effectSettings) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Enchantment id cannot be empty.");
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Display name cannot be empty.");
        }
        if (maximumLevel < 1) {
            throw new IllegalArgumentException("Maximum level must be at least 1.");
        }
        if (chance < 0.0D || chance > 100.0D) {
            throw new IllegalArgumentException("Chance must be between 0 and 100.");
        }
        if (cooldownSeconds < 0) {
            throw new IllegalArgumentException("Cooldown cannot be negative.");
        }

        this.id = id.toLowerCase(Locale.ENGLISH);
        this.displayName = displayName;
        this.maximumLevel = maximumLevel;
        this.category = category == null ? "uncategorised" : category;
        this.description = description == null ? "" : description;
        this.rarity = rarity == null ? "COMMON" : rarity.toUpperCase(Locale.ENGLISH);
        this.activation = activation == null ? "EVENT" : activation.toUpperCase(Locale.ENGLISH);
        this.chance = chance;
        this.cooldownSeconds = cooldownSeconds;

        EnumSet<Material> materials = EnumSet.noneOf(Material.class);
        if (applicableMaterials != null) {
            materials.addAll(applicableMaterials);
        }
        this.applicableMaterials = Collections.unmodifiableSet(materials);

        Set<String> conflicts = new HashSet<String>();
        if (incompatibleIds != null) {
            for (String conflict : incompatibleIds) {
                if (conflict != null && !conflict.trim().isEmpty()) {
                    conflicts.add(conflict.toLowerCase(Locale.ENGLISH));
                }
            }
        }
        this.incompatibleIds = Collections.unmodifiableSet(conflicts);

        Map<String, Object> settings = new HashMap<String, Object>();
        if (effectSettings != null) {
            settings.putAll(effectSettings);
        }
        this.effectSettings = Collections.unmodifiableMap(settings);
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getMaximumLevel() { return maximumLevel; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getRarity() { return rarity; }
    public String getActivation() { return activation; }
    public double getChance() { return chance; }
    public int getCooldownSeconds() { return cooldownSeconds; }
    public Set<Material> getApplicableMaterials() { return applicableMaterials; }
    public Set<String> getIncompatibleIds() { return incompatibleIds; }
    public Map<String, Object> getEffectSettings() { return effectSettings; }

    public boolean canApplyTo(Material material) {
        return material != null && applicableMaterials.contains(material);
    }

    public boolean isIncompatibleWith(String otherId) {
        return otherId != null && incompatibleIds.contains(otherId.toLowerCase(Locale.ENGLISH));
    }
}
