package net.dr4g0n20.advancedenchants.listeners;

import net.dr4g0n20.advancedenchants.enchantments.ItemEnchantmentStorage;
import net.dr4g0n20.advancedenchants.enchantments.StoredEnchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

/** Shares a small defensive aura with nearby players wearing Aura armour. */
public final class AuraListener implements Listener {

    private static final double RADIUS_SQUARED = 64.0D;
    private static final double REDUCTION_PER_LEVEL = 0.02D;
    private static final double MAX_REDUCTION = 0.20D;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onNearbyPlayerDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player target = (Player) event.getEntity();
        int strongestAura = 0;
        for (Entity nearby : target.getNearbyEntities(8.0D, 8.0D, 8.0D)) {
            if (!(nearby instanceof Player)) {
                continue;
            }
            Player wearer = (Player) nearby;
            if (wearer.getUniqueId().equals(target.getUniqueId())) {
                continue;
            }
            strongestAura = Math.max(strongestAura, getAuraLevel(wearer));
        }

        if (strongestAura > 0) {
            double reduction = Math.min(MAX_REDUCTION, strongestAura * REDUCTION_PER_LEVEL);
            event.setDamage(event.getDamage() * (1.0D - reduction));
        }
    }

    private int getAuraLevel(Player player) {
        int level = 0;
        for (ItemStack piece : player.getInventory().getArmorContents()) {
            for (StoredEnchantment enchantment : ItemEnchantmentStorage.getEnchantments(piece)) {
                if ("aura".equals(enchantment.getId())) {
                    level = Math.max(level, enchantment.getLevel());
                }
            }
        }
        return level;
    }
}
