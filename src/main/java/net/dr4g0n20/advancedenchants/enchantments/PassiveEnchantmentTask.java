package net.dr4g0n20.advancedenchants.enchantments;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Refreshes short passive potion effects while the relevant enchanted item is
 * actively equipped. Effects naturally expire shortly after the item is changed.
 */
public final class PassiveEnchantmentTask extends BukkitRunnable {

    private final JavaPlugin plugin;

    public PassiveEnchantmentTask(JavaPlugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null.");
        }
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ItemStack helmet = player.getInventory().getHelmet();
            if (has(helmet, "waterborne")) {
                refresh(player, PotionEffectType.WATER_BREATHING, 0);
            }

            ItemStack hand = player.getItemInHand();
            int hasteLevel = ItemEnchantmentStorage.getLevel(hand, "haste");
            if (hasteLevel > 0 && hasteLevel <= 3) {
                refresh(player, PotionEffectType.FAST_DIGGING, hasteLevel - 1);
            }

            int alacrityLevel = ItemEnchantmentStorage.getLevel(hand, "alacrity");
            if (alacrityLevel > 0 && alacrityLevel <= 3) {
                refresh(player, PotionEffectType.SPEED, alacrityLevel - 1);
            }
        }
    }

    private boolean has(ItemStack item, String id) {
        return ItemEnchantmentStorage.hasEnchantment(item, id);
    }

    private void refresh(Player player, PotionEffectType type, int amplifier) {
        PotionEffect current = player.getPotionEffect(type);
        if (current == null || current.getAmplifier() < amplifier
                || (current.getAmplifier() == amplifier && current.getDuration() < 45)) {
            player.addPotionEffect(new PotionEffect(type, 65, amplifier, true, false), true);
        }
    }
}
