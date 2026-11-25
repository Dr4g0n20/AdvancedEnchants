package net.dr4g0n20.advancedenchants.enchantments;

/** Result codes returned by the custom enchantment application service. */
public enum EnchantmentApplyResult {
    APPLIED,
    UPGRADED,
    ALREADY_HAS_EQUAL_OR_HIGHER_LEVEL,
    UNKNOWN_ENCHANTMENT,
    INVALID_ITEM,
    INVALID_LEVEL,
    ITEM_NOT_COMPATIBLE,
    INCOMPATIBLE_ENCHANTMENT,
    ENCHANTMENT_LIMIT_REACHED,
    STORAGE_ERROR
}
