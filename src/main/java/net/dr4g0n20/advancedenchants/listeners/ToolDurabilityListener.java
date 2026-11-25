package net.dr4g0n20.advancedenchants.listeners;

import net.dr4g0n20.advancedenchants.enchantments.ItemEnchantmentStorage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

import java.util.concurrent.ThreadLocalRandom;

/** Gives Blacksmith a chance to prevent ordinary item durability loss. */
public final class ToolDurabilityListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        int level = ItemEnchantmentStorage.getLevel(event.getItem(), "blacksmith");
        if (level < 1) {
            return;
        }

        double preservationChance = Math.min(0.75D, 0.15D * level);
        if (ThreadLocalRandom.current().nextDouble() < preservationChance) {
            event.setCancelled(true);
        }
    }
}
