package net.dr4g0n20.advancedenchants.listeners;

import net.dr4g0n20.advancedenchants.enchantments.ItemEnchantmentStorage;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Implements the Vanilla+ melee enchantments that can be represented safely
 * with the Bukkit 1.12.2 combat API.
 */
public final class WeaponEnchantmentListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)
                || !(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        Player attacker = (Player) event.getDamager();
        LivingEntity victim = (LivingEntity) event.getEntity();
        ItemStack weapon = attacker.getItemInHand();
        if (weapon == null || weapon.getType() == Material.AIR) {
            return;
        }

        double damage = event.getDamage();
        int level = ItemEnchantmentStorage.getLevel(weapon, "criticals");
        if (level > 0 && isCriticalHit(attacker)) {
            damage += damage * (0.10D * level);
        }

        level = ItemEnchantmentStorage.getLevel(weapon, "first_strike");
        if (level > 0 && victim.getHealth() >= victim.getMaxHealth() - 0.01D) {
            damage += damage * (0.15D * level);
        }

        level = ItemEnchantmentStorage.getLevel(weapon, "finishing");
        if (level > 0 && victim.getHealth() <= victim.getMaxHealth() * 0.30D) {
            damage += damage * (0.15D * level);
        }

        EntityType type = victim.getType();
        level = ItemEnchantmentStorage.getLevel(weapon, "zombie_crusher");
        if (level > 0 && type == EntityType.ZOMBIE) {
            damage += damage * (0.10D * level);
        }

        level = ItemEnchantmentStorage.getLevel(weapon, "skullcrusher");
        if (level > 0 && (type == EntityType.SKELETON
                || type == EntityType.WITHER_SKELETON)) {
            damage += damage * (0.10D * level);
        }

        level = ItemEnchantmentStorage.getLevel(weapon, "blaze_reaper");
        if (level > 0 && (type == EntityType.BLAZE || type == EntityType.GHAST
                || type == EntityType.MAGMA_CUBE || type == EntityType.PIG_ZOMBIE)) {
            damage += damage * (0.10D * level);
        }

        level = ItemEnchantmentStorage.getLevel(weapon, "cubism");
        if (level > 0 && (type == EntityType.SLIME || type == EntityType.MAGMA_CUBE)) {
            damage += damage * (0.10D * level);
        }

        level = ItemEnchantmentStorage.getLevel(weapon, "incinerate");
        if (level > 0 && (type == EntityType.SPIDER || type == EntityType.CAVE_SPIDER)) {
            damage += damage * (0.10D * level);
        }

        event.setDamage(damage);

        level = ItemEnchantmentStorage.getLevel(weapon, "blackout");
        if (level > 0 && roll(Math.min(0.50D, 0.10D * level))) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,
                    30 + (20 * level), 0), true);
        }

        level = ItemEnchantmentStorage.getLevel(weapon, "starvation");
        if (level > 0 && roll(Math.min(0.60D, 0.15D * level))) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER,
                    60 + (20 * level), Math.min(1, level / 2)), true);
        }

        level = ItemEnchantmentStorage.getLevel(weapon, "ravenous");
        if (level > 0 && roll(Math.min(0.75D, 0.15D * level))
                && attacker.getFoodLevel() < 20) {
            attacker.setFoodLevel(Math.min(20, attacker.getFoodLevel() + level));
        }
    }

    private boolean isCriticalHit(Player player) {
        return !player.isOnGround() && player.getFallDistance() > 0.0F
                && !player.isSprinting() && !player.isInsideVehicle()
                && !player.hasPotionEffect(PotionEffectType.BLINDNESS);
    }

    private boolean roll(double chance) {
        return ThreadLocalRandom.current().nextDouble() < chance;
    }
}
