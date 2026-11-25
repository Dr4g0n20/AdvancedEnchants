package net.dr4g0n20.advancedenchants.listeners;

import net.dr4g0n20.advancedenchants.enchantments.ItemEnchantmentStorage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/** Replants mature crops when a suitable tool enchantment and seed are available. */
public final class CropReplantListener implements Listener {

    private final JavaPlugin plugin;

    public CropReplantListener(JavaPlugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null.");
        }
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCropHarvest(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material crop = block.getType();
        Material seed = seedFor(crop);
        if (seed == null || block.getData() < maximumAge(crop)) {
            return;
        }

        ItemStack tool = player.getItemInHand();
        boolean carrotPlanter = crop == Material.CARROT
                && ItemEnchantmentStorage.hasEnchantment(tool, "carrot_planter");
        boolean replenish = ItemEnchantmentStorage.hasEnchantment(tool, "replenish");
        if (!carrotPlanter && !replenish) {
            return;
        }

        if (!removeOne(player, seed)) {
            return;
        }

        final org.bukkit.Location location = block.getLocation();
        final Material cropType = crop;
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                Block harvested = location.getBlock();
                if (harvested.getType() == Material.AIR) {
                    harvested.setType(cropType);
                    harvested.setData((byte) 0, true);
                }
            }
        });
    }

    private boolean removeOne(Player player, Material material) {
        if (!player.getInventory().contains(material)) {
            return false;
        }
        Map<Integer, ItemStack> leftovers =
                player.getInventory().removeItem(new ItemStack(material, 1));
        return leftovers.isEmpty();
    }

    private Material seedFor(Material crop) {
        switch (crop) {
            case CROPS:
                return Material.SEEDS;
            case CARROT:
                return Material.CARROT_ITEM;
            case POTATO:
                return Material.POTATO_ITEM;
            case BEETROOT_BLOCK:
                return Material.BEETROOT_SEEDS;
            default:
                return null;
        }
    }

    private int maximumAge(Material crop) {
        return crop == Material.BEETROOT_BLOCK ? 3 : 7;
    }
}
