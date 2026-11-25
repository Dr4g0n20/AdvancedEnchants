package net.dr4g0n20.advancedenchants.guidebook;

import net.dr4g0n20.advancedenchants.AdvancedEnchantsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class GuidebookListener implements Listener {

    private final AdvancedEnchantsPlugin plugin;

    public GuidebookListener(AdvancedEnchantsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMenuClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof GuidebookMenuHolder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getClickedInventory() == null || event.getClickedInventory() != top) return;

        GuidebookMenuHolder holder = (GuidebookMenuHolder) top.getHolder();
        plugin.getGuidebookService().handleClick((Player) event.getWhoClicked(),
                holder, event.getSlot());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMenuDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof GuidebookMenuHolder) {
            event.setCancelled(true);
        }
    }
}
