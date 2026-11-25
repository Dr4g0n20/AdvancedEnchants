package net.dr4g0n20.advancedenchants.enchantments;

/**
 * One custom enchantment and its stored level.
 */
public final class StoredEnchantment {

    private final String id;
    private final int level;

    public StoredEnchantment(String id, int level) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Enchantment id cannot be empty.");
        }
        if (level < 1) {
            throw new IllegalArgumentException("Enchantment level must be positive.");
        }
        this.id = id.toLowerCase(java.util.Locale.ENGLISH);
        this.level = level;
    }

    public String getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }
}
