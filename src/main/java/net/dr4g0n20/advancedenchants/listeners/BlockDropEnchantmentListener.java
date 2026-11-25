package net.dr4g0n20.advancedenchants.listeners;

import net.dr4g0n20.advancedenchants.enchantments.ItemEnchantmentStorage;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Applies safe bonus-drop and automatic-smelting tool enchantments.
 * Uses BlockBreakEvent because BlockDropItemEvent is not present in Bukkit 1.12.2.
 */
public final class BlockDropEnchantmentListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        ItemStack tool = player.getItemInHand();
        int infernalLevel = ItemEnchantmentStorage.getLevel(tool, "infernal_touch");
        int foragingLevel = ItemEnchantmentStorage.getLevel(tool, "foraging");
        int prospectorLevel = ItemEnchantmentStorage.getLevel(tool, "nether_prospector");
        Block block = event.getBlock();
        Material blockType = block.getType();

        boolean infernalApplies = infernalLevel > 0
                && !tool.containsEnchantment(org.bukkit.enchantments.Enchantment.SILK_TOUCH)
                && smeltResult(blockType) != null;
        boolean foragingApplies = foragingLevel > 0
                && (blockType == Material.LEAVES || blockType == Material.LEAVES_2)
                && roll(0.15D * foragingLevel);
        boolean prospectorApplies = prospectorLevel > 0
                && blockType == Material.QUARTZ_ORE
                && roll(0.15D * prospectorLevel);

        if (!infernalApplies && !foragingApplies && !prospectorApplies) {
            return;
        }

        Collection<ItemStack> vanillaDrops = block.getDrops(tool);
        List<ItemStack> drops = new ArrayList<ItemStack>();
        for (ItemStack original : vanillaDrops) {
            if (original == null || original.getType() == Material.AIR) {
                continue;
            }
            ItemStack drop = original.clone();
            if (infernalApplies && isSmeltableInput(blockType, drop.getType())) {
                drop.setType(smeltResult(blockType));
            }
            drops.add(drop);
            if (foragingApplies) {
                drops.add(original.clone());
            }
        }

        if (prospectorApplies) {
            drops.add(new ItemStack(Material.QUARTZ, 1));
        }

        // Suppress the normal drop pass because the equivalent vanilla drops
        // (with the active tool's Fortune/Silk Touch rules) are emitted below.
        event.setDropItems(false);
        for (ItemStack drop : drops) {
            player.getWorld().dropItemNaturally(block.getLocation(), drop);
        }
    }

    private boolean roll(double chance) {
        return ThreadLocalRandom.current().nextDouble() < Math.min(0.75D, chance);
    }

    private boolean isSmeltableInput(Material blockType, Material dropType) {
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
