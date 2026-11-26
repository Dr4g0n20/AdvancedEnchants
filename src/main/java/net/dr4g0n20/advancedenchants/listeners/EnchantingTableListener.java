package net.dr4g0n20.advancedenchants.listeners;

import net.dr4g0n20.advancedenchants.AdvancedEnchantsPlugin;
import net.dr4g0n20.advancedenchants.enchantments.EnchantmentApplyResult;
import net.dr4g0n20.advancedenchants.enchantments.EnchantmentDefinition;
import net.dr4g0n20.advancedenchants.enchantments.ItemEnchantmentStorage;
import net.dr4g0n20.advancedenchants.enchantments.StoredEnchantment;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Adds one eligible custom enchantment to a successful enchanting-table result.
 * Uses the live registry so newly added YAML definitions are automatically
 * included without requiring individual acquisition handlers.
 */
public final class EnchantingTableListener implements Listener {

    private final AdvancedEnchantsPlugin plugin;

    public EnchantingTableListener(AdvancedEnchantsPlugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null.");
        }
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantItem(EnchantItemEvent event) {
        if (!plugin.getConfig().getBoolean("acquisition.enchanting-table.enabled", true)) {
            return;
        }

        double chance = plugin.getConfig().getDouble(
                "acquisition.enchanting-table.chance", 60.0D);
        chance = Math.max(0.0D, Math.min(100.0D, chance));
        if (ThreadLocalRandom.current().nextDouble(100.0D) >= chance) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR
                || item.getType() == Material.BOOK
                || item.getType() == Material.ENCHANTED_BOOK) {
            return;
        }

        List<EnchantmentDefinition> candidates = getEligibleEnchantments(item);
        if (candidates.isEmpty()) {
            return;
        }

        EnchantmentDefinition selected = chooseByRarity(candidates);
        if (selected == null) {
            return;
        }

        int level = getLevelForEnchantingCost(
                event.getExpLevelCost(), selected.getMaximumLevel());

        EnchantmentApplyResult result = plugin.getEnchantmentApplicator()
                .apply(item, selected.getId(), level);
        if (result == EnchantmentApplyResult.APPLIED
                || result == EnchantmentApplyResult.UPGRADED) {
            if (plugin.getConfig().getBoolean("general.debug", false)) {
                event.getEnchanter().sendMessage(
                        "§dCustom enchantment: §f" + selected.getDisplayName()
                                + " " + toRoman(level));
            }
        }
    }

    private List<EnchantmentDefinition> getEligibleEnchantments(ItemStack item) {
        List<EnchantmentDefinition> candidates = new ArrayList<EnchantmentDefinition>();
        List<StoredEnchantment> existing = ItemEnchantmentStorage.getEnchantments(item);
        int maximum = plugin.getEnchantmentApplicator().getMaximumEnchantments();
        if (existing.size() >= maximum) {
            return candidates;
        }

        for (EnchantmentDefinition definition
                : plugin.getEnchantmentRegistry().getAll()) {
            if (!definition.canApplyTo(item.getType())
                    || ItemEnchantmentStorage.hasEnchantment(item, definition.getId())) {
                continue;
            }

            boolean conflict = false;
            for (StoredEnchantment stored : existing) {
                EnchantmentDefinition other =
                        plugin.getEnchantmentRegistry().get(stored.getId());
                if (definition.isIncompatibleWith(stored.getId())
                        || (other != null
                        && other.isIncompatibleWith(definition.getId()))) {
                    conflict = true;
                    break;
                }
            }
            if (!conflict) {
                candidates.add(definition);
            }
        }
        return candidates;
    }

    private EnchantmentDefinition chooseByRarity(
            List<EnchantmentDefinition> candidates) {
        double totalWeight = 0.0D;
        for (EnchantmentDefinition candidate : candidates) {
            totalWeight += getRarityWeight(candidate.getRarity());
        }
        if (totalWeight <= 0.0D) {
            return null;
        }

        double roll = ThreadLocalRandom.current().nextDouble(totalWeight);
        for (EnchantmentDefinition candidate : candidates) {
            roll -= getRarityWeight(candidate.getRarity());
            if (roll < 0.0D) {
                return candidate;
            }
        }
        return candidates.get(candidates.size() - 1);
    }

    private double getRarityWeight(String rarity) {
        if (rarity == null) {
            return 1.0D;
        }
        switch (rarity.toUpperCase(java.util.Locale.ENGLISH)) {
            case "COMMON":
            case "UNASSIGNED":
                return 1.0D;
            case "UNCOMMON":
                return 0.75D;
            case "RARE":
                return 0.50D;
            case "EPIC":
                return 0.30D;
            case "LEGENDARY":
                return 0.15D;
            case "MYTHIC":
                return 0.08D;
            default:
                return 1.0D;
        }
    }

    private int getLevelForEnchantingCost(int cost, int maximumLevel) {
        int availableLevel = Math.max(1, Math.min(maximumLevel, 1 + Math.max(0, cost - 1) / 6));
        return availableLevel;
    }

    private String toRoman(int number) {
        String[] numerals = {"X", "IX", "V", "IV", "I"};
        int[] values = {10, 9, 5, 4, 1};
        StringBuilder result = new StringBuilder();
        int remaining = number;
        for (int i = 0; i < values.length; i++) {
            while (remaining >= values[i]) {
                result.append(numerals[i]);
                remaining -= values[i];
            }
        }
        return result.toString();
    }
}
