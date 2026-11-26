package net.dr4g0n20.advancedenchants.listeners;

import net.dr4g0n20.advancedenchants.AdvancedEnchantsPlugin;
import net.dr4g0n20.advancedenchants.enchantments.EnchantmentApplyResult;
import net.dr4g0n20.advancedenchants.enchantments.EnchantmentDefinition;
import net.dr4g0n20.advancedenchants.enchantments.ItemEnchantmentStorage;
import net.dr4g0n20.advancedenchants.enchantments.StoredEnchantment;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Supplements vanilla enchanting-table results so every result has at least
 * two enchantments in total. The highest enchanting-table option can
 * occasionally produce four total enchantments.
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

        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR
                || item.getType() == Material.BOOK
                || item.getType() == Material.ENCHANTED_BOOK) {
            return;
        }

        int vanillaCount = event.getEnchantsToAdd().size();
        int customCount = ItemEnchantmentStorage.getEnchantments(item).size();
        int targetCount = Math.max(2, vanillaCount + customCount);

        int fourEnchantments = plugin.getConfig().getInt(
                "acquisition.enchanting-table.four-enchantment-target", 4);
        fourEnchantments = Math.max(2, Math.min(4, fourEnchantments));

        int maximumOptionCost = plugin.getConfig().getInt(
                "acquisition.enchanting-table.maximum-option-cost", 3);
        boolean isMaximumOption = event.getExpLevelCost() >= maximumOptionCost;
        double fourChance = plugin.getConfig().getDouble(
                "acquisition.enchanting-table.four-enchantment-chance", 35.0D);
        fourChance = Math.max(0.0D, Math.min(100.0D, fourChance));

        if (isMaximumOption
                && ThreadLocalRandom.current().nextDouble(100.0D) < fourChance) {
            targetCount = Math.max(targetCount, fourEnchantments);
        }

        int maximum = plugin.getEnchantmentApplicator().getMaximumEnchantments();
        targetCount = Math.min(targetCount, vanillaCount + maximum);

        int attempts = 0;
        while (vanillaCount + ItemEnchantmentStorage.getEnchantments(item).size()
                < targetCount && attempts++ < maximum) {
            List<EnchantmentDefinition> candidates = getEligibleEnchantments(item);
            if (candidates.isEmpty()) {
                break;
            }

            EnchantmentDefinition selected = chooseByRarity(candidates);
            if (selected == null) {
                break;
            }

            int level = getLevelForEnchantingCost(
                    event.getExpLevelCost(), selected.getMaximumLevel());
            EnchantmentApplyResult result = plugin.getEnchantmentApplicator()
                    .apply(item, selected.getId(), level);

            if (result != EnchantmentApplyResult.APPLIED
                    && result != EnchantmentApplyResult.UPGRADED) {
                break;
            }

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

            if (!conflict && conflictsWithVanilla(item, definition)) {
                conflict = true;
            }

            if (!conflict) {
                candidates.add(definition);
            }
        }
        return candidates;
    }

    private boolean conflictsWithVanilla(
            ItemStack item, EnchantmentDefinition definition) {
        // Custom definitions currently express incompatibilities by custom ID.
        // Keep vanilla enchantments intact; the applicator remains responsible
        // for validating custom-enchantment compatibility.
        for (Enchantment enchantment : item.getEnchantments().keySet()) {
            if (enchantment == null) {
                continue;
            }
        }
        return false;
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
        // Bukkit 1.12 reports the selected table option as a cost of 1, 2, or 3.
        // Use the option tier rather than treating this value as the player's
        // full XP level.
        int availableLevel = cost >= 3 ? maximumLevel : (cost == 2
                ? Math.max(1, (maximumLevel + 1) / 2) : 1);
        return Math.max(1, Math.min(maximumLevel, availableLevel));
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
