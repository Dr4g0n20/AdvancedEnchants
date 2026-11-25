package net.dr4g0n20.advancedenchants.enchantments;

import org.bukkit.Material;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/** Data model for a configurable custom enchantment. */
public final class EnchantmentDefinition {

    private final String id;
    private final String displayName;
    private final int maximumLevel;
    private final String category;
    private final String description;
    private final Set<Material> applicableMaterials;
    private final Set<String> incompatibleIds;

    public EnchantmentDefinition(String id, String displayName, int maximumLevel,
                                 String category, String description) {
        this(id, displayName, maximumLevel, category, description,
                Collections.<Material>emptySet(), Collections.<String>emptySet());
    }

    public EnchantmentDefinition(String id, String displayName, int maximumLevel,
                                 String category, String description,
                                 Set<Material> applicableMaterials,
                                 Set<String> incompatibleIds) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Enchantment id cannot be empty.");
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Display name cannot be empty.");
        }
        if (maximumLevel < 1) {
            throw new IllegalArgumentException("Maximum level must be at least 1.");
        }

        this.id = id.toLowerCase(Locale.ENGLISH);
        this.displayName = displayName;
        this.maximumLevel = maximumLevel;
        this.category = category == null ? "uncategorised" : category;
        this.description = description == null ? "" : description;

        EnumSet<Material> materials = EnumSet.noneOf(Material.class);
        if (applicableMaterials != null) {
            materials.addAll(applicableMaterials);
        }
        this.applicableMaterials = Collections.unmodifiableSet(materials);

        java.util.Set<String> conflicts = new java.util.HashSet<String>();
        if (incompatibleIds != null) {
            for (String conflict : incompatibleIds) {
                if (conflict != null && !conflict.trim().isEmpty()) {
                    conflicts.add(conflict.toLowerCase(Locale.ENGLISH));
                }
            }
        }
        this.incompatibleIds = Collections.unmodifiableSet(conflicts);
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getMaximumLevel() { return maximumLevel; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }

    public Set<Material> getApplicableMaterials() { return applicableMaterials; }
    public Set<String> getIncompatibleIds() { return incompatibleIds; }

    public boolean canApplyTo(Material material) {
        return material != null && applicableMaterials.contains(material);
    }

    public boolean isIncompatibleWith(String otherId) {
        return otherId != null && incompatibleIds.contains(otherId.toLowerCase(Locale.ENGLISH));
    }
}
