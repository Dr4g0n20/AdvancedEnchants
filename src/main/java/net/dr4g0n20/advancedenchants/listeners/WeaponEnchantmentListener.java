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
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Implements the Vanilla+ melee enchantments that can be represented safely
 * with the Bukkit 1.12.2 combat API.
 */
public final class WeaponEnchantmentListener implements Listener {

    private boolean applyingAreaDamage;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (applyingAreaDamage || !(event.getDamager() instanceof Player)
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

        level = ItemEnchantmentStorage.getLevel(weapon, "curse_of_harmlessness");
        if (level > 0 && roll(Math.min(0.60D, 0.10D * level))) {
            event.setDamage(0.0D);
            return;
        }

        level = ItemEnchantmentStorage.getLevel(weapon, "enderbane");
        if (level > 0 && (victim.getType() == EntityType.ENDERMAN
                || victim.getType() == EntityType.ENDER_DRAGON)) {
            damage += damage * (0.15D * level);
        }

        level = ItemEnchantmentStorage.getLevel(weapon, "ninja");
        if (level > 0 && attacker.isSneaking()) {
            damage += damage * (0.15D * level);
        }

        level = ItemEnchantmentStorage.getLevel(weapon, "shura");
        if (level > 0 && attacker.getHealth() <= attacker.getMaxHealth() * 0.50D
                && isCriticalHit(attacker)) {
            damage += damage * (0.15D * level);
        }

        level = ItemEnchantmentStorage.getLevel(weapon, "arctic_freeze");
        if (level > 0 && roll(Math.min(0.60D, 0.10D * level))) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,
                    40 + 20 * level, Math.min(2, level - 1)), true);
            victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON,
                    40 + 10 * level, 0), true);
        }

        level = ItemEnchantmentStorage.getLevel(weapon, "repel");
        if (level > 0 && roll(Math.min(0.60D, 0.10D * level))) {
            Vector knockback = victim.getLocation().toVector()
                    .subtract(attacker.getLocation().toVector()).normalize()
                    .multiply(0.45D + (0.10D * level));
            knockback.setY(0.25D);
            victim.setVelocity(knockback);
        }

        level = ItemEnchantmentStorage.getLevel(weapon, "thor");
        if (level > 0 && roll(Math.min(0.45D, 0.05D * level))) {
            victim.getWorld().strikeLightningEffect(victim.getLocation());
            damage += 1.0D * level;
        }

        level = ItemEnchantmentStorage.getLevel(weapon, "drain");
        if (level > 0) {
            double healing = Math.min(2.0D, damage * 0.03D * level);
            attacker.setHealth(Math.min(attacker.getMaxHealth(),
                    attacker.getHealth() + healing));
        }

        level = ItemEnchantmentStorage.getLevel(weapon, "caffeinated");
        if (level > 0 && attacker.isSprinting()
                && roll(Math.min(0.60D, 0.10D * level))) {
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
                    40, Math.min(1, level - 1)), true);
        }

        level = ItemEnchantmentStorage.getLevel(weapon, "carve");
        if (level > 0) {
            damageNearby(attacker, victim, 3.0D, Math.max(1.0D, level));
        }

        level = ItemEnchantmentStorage.getLevel(weapon, "fuddle");
        if (level > 0 && roll(Math.min(0.50D, 0.08D * level))
                && victim instanceof Player) {
            shuffleHotbar((Player) victim);
        }
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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSwordAbility(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack weapon = player.getItemInHand();
        int ascend = ItemEnchantmentStorage.getLevel(weapon, "ascend");
        int charge = ItemEnchantmentStorage.getLevel(weapon, "charge");
        if (ascend > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION,
                    20 + (20 * ascend), Math.min(2, ascend - 1)), true);
            event.setCancelled(true);
        } else if (charge > 0) {
            Vector velocity = player.getLocation().getDirection().normalize()
                    .multiply(0.8D + (0.35D * charge));
            velocity.setY(Math.max(0.25D, velocity.getY() + 0.20D));
            player.setVelocity(velocity);
            event.setCancelled(true);
        }
    }

    private void damageNearby(Player attacker, LivingEntity primary,
                              double radius, double damage) {
        applyingAreaDamage = true;
        try {
            for (org.bukkit.entity.Entity nearby : primary.getNearbyEntities(radius, radius, radius)) {
                if (nearby instanceof LivingEntity && nearby != attacker && nearby != primary) {
                    ((LivingEntity) nearby).damage(damage, attacker);
                }
            }
        } finally {
            applyingAreaDamage = false;
        }
    }

    private void shuffleHotbar(Player target) {
        org.bukkit.inventory.PlayerInventory inventory = target.getInventory();
        List<ItemStack> items = new ArrayList<ItemStack>();
        for (int slot = 0; slot < 9; slot++) {
            items.add(inventory.getItem(slot));
        }
        Collections.shuffle(items);
        for (int slot = 0; slot < 9; slot++) {
            inventory.setItem(slot, items.get(slot));
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
