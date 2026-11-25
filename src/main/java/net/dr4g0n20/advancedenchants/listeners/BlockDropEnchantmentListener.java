package net.dr4g0n20.advancedenchants.listeners;

import net.dr4g0n20.advancedenchants.enchantments.ItemEnchantmentStorage;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

/** Applies safe bonus-drop and automatic-smelting tool enchantments. */
public final class BlockDropEnchantmentListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDrops(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        ItemStack tool = player.getItemInHand();
        int infernalLevel = ItemEnchantmentStorage.getLevel(tool, "infernal_touch");
        int foragingLevel = ItemEnchantmentStorage.getLevel(tool, "foraging");
        int prospectorLevel = ItemEnchantmentStorage.getLevel(tool, "nether_prospector");
        Material blockType = event.getBlock().getType();

        if (infernalLevel > 0 && !tool.containsEnchantment(org.bukkit.enchantments.Enchantment.SILK_TOUCH)) {
            smeltEligibleDrops(event, blockType);
        }

        if (foragingLevel > 0
                && (blockType == Material.LEAVES || blockType == Material.LEAVES_2)) {
            double chance = Math.min(0.75D, 0.15D * foragingLevel);
            if (ThreadLocalRandom.current().nextDouble() < chance) {
                for (Item dropped : event.getItems()) {
                    ItemStack bonus = dropped.getItemStack().clone();
                    if (bonus.getType() != Material.AIR) {
                        player.getWorld().dropItemNaturally(event.getBlock().getLocation(), bonus);
                    }
                }
            }
        }

        if (prospectorLevel > 0 && blockType == Material.QUARTZ_ORE) {
            double chance = Math.min(0.75D, 0.15D * prospectorLevel);
            if (ThreadLocalRandom.current().nextDouble() < chance) {
                player.getWorld().dropItemNaturally(event.getBlock().getLocation(),
                        new ItemStack(Material.QUARTZ, 1));
            }
        }
    }

    private void smeltEligibleDrops(BlockDropItemEvent event, Material blockType) {
        Material result = smeltResult(blockType);
        if (result == null) {
            return;
        }
        for (Item dropped : event.getItems()) {
            ItemStack stack = dropped.getItemStack();
            if (stack.getType() == blockType || isExpectedInput(blockType, stack.getType())) {
                dropped.setItemStack(new ItemStack(result, stack.getAmount()));
            }
        }
    }

    private boolean isExpectedInput(Material blockType, Material dropType) {
        if (blockType == Material.CLAY) {
            return dropType == Material.CLAY_BALL;
        }
        return dropType == blockType;
    }

    private Material smeltResult(Material blockType) {
        switch (blockType) {
            case IRON_ORE:
                return Material.IRON_INGOT;
            case GOLD_ORE:
                return Material.GOLD_INGOT;
            case SAND:
                return Material.GLASS;
            case CLAY:
                return Material.BRICK;
            case NETHERRACK:
                return Material.NETHER_BRICK;
            default:
                return null;
        }
    }
}
