package net.dr4g0n20.advancedenchants.listeners;

import net.dr4g0n20.advancedenchants.enchantments.ItemEnchantmentStorage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.concurrent.ThreadLocalRandom;

/** Handles the non-destructive chance to preserve a block with Curse of Breaklessness. */
public final class BlockBreakEnchantmentListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        int level = ItemEnchantmentStorage.getLevel(player.getItemInHand(), "curse_of_breaklessness");
        if (level < 1) {
            return;
        }

        double chance = Math.min(0.45D, 0.05D * level);
        if (ThreadLocalRandom.current().nextDouble() < chance) {
            event.setCancelled(true);
        }
    }
}
