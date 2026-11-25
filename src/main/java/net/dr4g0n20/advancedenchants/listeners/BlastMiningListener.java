package net.dr4g0n20.advancedenchants.listeners;

import net.dr4g0n20.advancedenchants.enchantments.ItemEnchantmentStorage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

/** Mines a small facing-aligned 3x3 plane while firing protection events for each block. */
public final class BlastMiningListener implements Listener {

    private static final ThreadLocal<Boolean> EXTRA_BLOCK_EVENT =
            ThreadLocal.withInitial(() -> Boolean.FALSE);

    private final JavaPlugin plugin;

    public BlastMiningListener(JavaPlugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null.");
        }
        this.plugin = plugin;
    }

    public static boolean isProcessingExtraBlock() {
        return EXTRA_BLOCK_EVENT.get().booleanValue();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCentreBlockBreak(final BlockBreakEvent event) {
        final Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL
                || !ItemEnchantmentStorage.hasEnchantment(
                        player.getItemInHand(), "blast_mining")) {
            return;
        }

        final org.bukkit.Location centre = event.getBlock().getLocation();
        final boolean xPlane = facesNorthOrSouth(player.getLocation().getYaw());
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                breakAdjacentBlocks(player, centre, xPlane);
            }
        });
    }

    private void breakAdjacentBlocks(Player player, org.bukkit.Location centre, boolean xPlane) {
        for (int first = -1; first <= 1; first++) {
            for (int second = -1; second <= 1; second++) {
                if (first == 0 && second == 0) {
                    continue;
                }
                ItemStack tool = player.getItemInHand();
                if (!player.isOnline() || tool == null || tool.getType() == Material.AIR
                        || !ItemEnchantmentStorage.hasEnchantment(tool, "blast_mining")) {
                    return;
                }

                Block block = xPlane
                        ? centre.getWorld().getBlockAt(
                                centre.getBlockX() + first, centre.getBlockY() + second,
                                centre.getBlockZ())
                        : centre.getWorld().getBlockAt(
                                centre.getBlockX(), centre.getBlockY() + second,
                                centre.getBlockZ() + first);
                if (!isEligible(block)) {
                    continue;
                }

                Material originalType = block.getType();
                BlockBreakEvent extraEvent = new BlockBreakEvent(block, player);
                extraEvent.setExpToDrop(experienceFor(originalType, tool));
                EXTRA_BLOCK_EVENT.set(Boolean.TRUE);
                try {
                    Bukkit.getPluginManager().callEvent(extraEvent);
                } finally {
                    EXTRA_BLOCK_EVENT.set(Boolean.FALSE);
                }

                if (extraEvent.isCancelled() || block.getType() != originalType) {
                    continue;
                }

                Collection<ItemStack> drops = extraEvent.isDropItems()
                        ? block.getDrops(tool) : java.util.Collections.<ItemStack>emptyList();
                block.setType(Material.AIR, false);
                for (ItemStack drop : drops) {
                    if (drop != null && drop.getType() != Material.AIR) {
                        player.getWorld().dropItemNaturally(block.getLocation(), drop);
                    }
                }

                if (extraEvent.getExpToDrop() > 0) {
                    ExperienceOrb orb = player.getWorld().spawn(
                            block.getLocation(), ExperienceOrb.class);
                    orb.setExperience(extraEvent.getExpToDrop());
                }
                damageToolOnce(player, tool);
            }
        }
    }

    private boolean isEligible(Block block) {
        Material type = block.getType();
        if (type == Material.AIR || !type.isSolid()
                || type == Material.BEDROCK || type == Material.BARRIER
                || type == Material.END_PORTAL_FRAME || type == Material.MOB_SPAWNER) {
            return false;
        }
        return !(block.getState() instanceof InventoryHolder);
    }

    private boolean facesNorthOrSouth(float yaw) {
        float normalized = (yaw % 360.0F + 360.0F) % 360.0F;
        return normalized < 45.0F || normalized >= 135.0F && normalized < 225.0F
                || normalized >= 315.0F;
    }

    private int experienceFor(Material type, ItemStack tool) {
        if (tool.containsEnchantment(Enchantment.SILK_TOUCH)) {
            return 0;
        }
        switch (type) {
            case COAL_ORE:
                return randomBetween(0, 2);
            case DIAMOND_ORE:
            case EMERALD_ORE:
                return randomBetween(3, 7);
            case LAPIS_ORE:
                return randomBetween(2, 5);
            case REDSTONE_ORE:
            case GLOWING_REDSTONE_ORE:
                return randomBetween(1, 5);
            case QUARTZ_ORE:
                return randomBetween(2, 5);
            default:
                return 0;
        }
    }

    private int randomBetween(int minimum, int maximum) {
        return ThreadLocalRandom.current().nextInt(minimum, maximum + 1);
    }

    private void damageToolOnce(Player player, ItemStack tool) {
        if (tool.getType().getMaxDurability() <= 0) {
            return;
        }
        PlayerItemDamageEvent damageEvent = new PlayerItemDamageEvent(player, tool, 1);
        Bukkit.getPluginManager().callEvent(damageEvent);
        if (damageEvent.isCancelled()) {
            return;
        }

        int durability = tool.getDurability() + damageEvent.getDamage();
        if (durability >= tool.getType().getMaxDurability()) {
            player.setItemInHand(null);
        } else {
            tool.setDurability((short) durability);
        }
    }
}
