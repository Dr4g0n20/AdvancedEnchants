package net.dr4g0n20.advancedenchants.guidebook;

import net.dr4g0n20.advancedenchants.AdvancedEnchantsPlugin;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/** Refreshes an existing guidebook immediately before the player opens it. */
public final class GuidebookListener implements Listener {

    private final AdvancedEnchantsPlugin plugin;

    public GuidebookListener(AdvancedEnchantsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGuidebookOpen(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.WRITTEN_BOOK) {
            return;
        }
        new GuidebookService(plugin.getEnchantmentRegistry()).refresh(item);
    }
}
