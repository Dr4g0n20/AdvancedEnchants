package net.dr4g0n20.advancedenchants.enchantments;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;

/**
 * Validates and applies custom enchantments to items and enchanted books.
 *
 * Books intentionally accept incompatible enchantments: conflicts are
 * checked when a book is applied to an actual piece of equipment.
 */
public final class EnchantmentApplicator {

    private final EnchantmentRegistry registry;
    private final int maximumEnchantments;

    public EnchantmentApplicator(EnchantmentRegistry registry, int maximumEnchantments) {
        if (registry == null) {
            throw new IllegalArgumentException("Enchantment registry cannot be null.");
        }
        if (maximumEnchantments < 1) {
            throw new IllegalArgumentException("Maximum enchantment count must be positive.");
        }
        this.registry = registry;
        this.maximumEnchantments = maximumEnchantments;
    }

    public EnchantmentApplyResult apply(ItemStack item, String enchantmentId, int level) {
        if (item == null || item.getType() == Material.AIR) {
            return EnchantmentApplyResult.INVALID_ITEM;
        }
        if (enchantmentId == null || enchantmentId.trim().isEmpty()) {
            return EnchantmentApplyResult.UNKNOWN_ENCHANTMENT;
        }

        EnchantmentDefinition definition = registry.get(enchantmentId);
        if (definition == null) {
            return EnchantmentApplyResult.UNKNOWN_ENCHANTMENT;
        }
        if (level < 1 || level > definition.getMaximumLevel()) {
            return EnchantmentApplyResult.INVALID_LEVEL;
        }

        boolean book = isEnchantedBook(item.getType());
        if (!book && !definition.canApplyTo(item.getType())) {
            return EnchantmentApplyResult.ITEM_NOT_COMPATIBLE;
        }

        List<StoredEnchantment> existing = ItemEnchantmentStorage.getEnchantments(item);
        int currentLevel = ItemEnchantmentStorage.getLevel(item, definition.getId());

        if (!book) {
            for (StoredEnchantment stored : existing) {
                if (stored.getId().equals(definition.getId())) {
                    continue;
                }
                EnchantmentDefinition other = registry.get(stored.getId());
                if (other != null && (definition.isIncompatibleWith(other.getId())
                        || other.isIncompatibleWith(definition.getId()))) {
                    return EnchantmentApplyResult.INCOMPATIBLE_ENCHANTMENT;
                }
            }
        }

        if (currentLevel >= level) {
            return EnchantmentApplyResult.ALREADY_HAS_EQUAL_OR_HIGHER_LEVEL;
        }

        if (currentLevel == 0 && existing.size() >= maximumEnchantments) {
            return EnchantmentApplyResult.ENCHANTMENT_LIMIT_REACHED;
        }

        boolean saved = ItemEnchantmentStorage.setEnchantment(item, definition.getId(), level);
        if (!saved) {
            return EnchantmentApplyResult.STORAGE_ERROR;
        }

        return currentLevel == 0
                ? EnchantmentApplyResult.APPLIED
                : EnchantmentApplyResult.UPGRADED;
    }

    public int getMaximumEnchantments() {
        return maximumEnchantments;
    }

    public static boolean isEnchantedBook(Material material) {
        return material == Material.BOOK || material == Material.ENCHANTED_BOOK;
    }
}
