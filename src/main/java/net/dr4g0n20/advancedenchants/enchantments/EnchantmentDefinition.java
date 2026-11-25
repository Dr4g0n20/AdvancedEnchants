package net.dr4g0n20.advancedenchants.enchantments;

/** Data model for a configurable custom enchantment. */
public final class EnchantmentDefinition {

    private final String id;
    private final String displayName;
    private final int maximumLevel;
    private final String category;
    private final String description;

    public EnchantmentDefinition(String id, String displayName, int maximumLevel,
                                 String category, String description) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Enchantment id cannot be empty.");
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Display name cannot be empty.");
        }
        if (maximumLevel < 1) {
            throw new IllegalArgumentException("Maximum level must be at least 1.");
        }
        this.id = id.toLowerCase();
        this.displayName = displayName;
        this.maximumLevel = maximumLevel;
        this.category = category == null ? "uncategorised" : category;
        this.description = description == null ? "" : description;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getMaximumLevel() { return maximumLevel; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
}
