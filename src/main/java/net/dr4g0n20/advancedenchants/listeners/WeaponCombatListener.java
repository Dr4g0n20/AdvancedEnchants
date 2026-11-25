package net.dr4g0n20.advancedenchants.listeners;

import net.dr4g0n20.advancedenchants.enchantments.ItemEnchantmentStorage;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Spider;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Combat effects for the first 1.12.2-compatible Vanilla+ weapon enchantments.
 * Uses Bukkit 1.12 entity and potion APIs; no post-1.12 materials or attributes.
 */
public final class WeaponCombatListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMeleeDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)
                || !(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        Player attacker = (Player) event.getDamager();
        LivingEntity victim = (LivingEntity) event.getEntity();
        int firstStrike = level(attacker, "first_strike");
        int finishing = level(attacker, "finishing");
        int enderbane = level(attacker, "enderbane");
        int cubism = level(attacker, "cubism");
        int incinerate = level(attacker, "incinerate");
        int skullcrusher = level(attacker, "skullcrusher");
        int zombieCrusher = level(attacker, "zombie_crusher");
        int blazeReaper = level(attacker, "blaze_reaper");
        int ravenous = level(attacker, "ravenous");
        int starvation = level(attacker, "starvation");
        int blackout = level(attacker, "blackout");

        double damage = event.getDamage();
        if (firstStrike > 0 && victim.getHealth() >= victim.getMaxHealth() - 0.01D) {
            damage *= 1.0D + (0.10D * firstStrike);
        }
        if (finishing > 0 && victim.getHealth() <= victim.getMaxHealth() * 0.30D) {
            damage *= 1.0D + (0.08D * finishing);
        }
        if (enderbane > 0 && (victim instanceof Enderman || victim instanceof EnderDragon)) {
            damage *= 1.0D + (0.10D * enderbane);
        }
        if (cubism > 0 && (victim instanceof Slime || victim instanceof MagmaCube)) {
            damage *= 1.0D + (0.10D * cubism);
        }
        if (incinerate > 0 && (victim instanceof Spider || victim instanceof CaveSpider)) {
            damage *= 1.0D + (0.10D * incinerate);
        }
        if (skullcrusher > 0 && (victim instanceof Skeleton || victim instanceof WitherSkeleton)) {
            damage *= 1.0D + (0.10D * skullcrusher);
        }
        if (zombieCrusher > 0 && (victim instanceof Zombie || victim instanceof PigZombie)) {
            damage *= 1.0D + (0.10D * zombieCrusher);
        }
        if (blazeReaper > 0 && isNetherMob(victim)) {
            damage *= 1.0D + (0.08D * blazeReaper);
        }
        event.setDamage(damage);

        if (blackout > 0 && roll(Math.min(0.60D, 0.10D * blackout))) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,
                    30 + (10 * blackout), 0), true);
        }
        if (starvation > 0 && victim instanceof Player
                && roll(Math.min(0.60D, 0.10D * starvation))) {
            ((Player) victim).addPotionEffect(new PotionEffect(PotionEffectType.HUNGER,
                    60 + (20 * starvation), Math.min(2, starvation - 1)), true);
        }
        if (ravenous > 0 && roll(Math.min(0.70D, 0.12D * ravenous))) {
            attacker.setFoodLevel(Math.min(20, attacker.getFoodLevel() + 1));
            attacker.setSaturation(Math.min(20.0F, attacker.getSaturation() + 1.0F));
        }
    }

    private int level(Player player, String id) {
        return ItemEnchantmentStorage.getLevel(player.getItemInHand(), id);
    }

    private boolean roll(double chance) {
        return ThreadLocalRandom.current().nextDouble() < chance;
    }

    private boolean isNetherMob(Entity entity) {
        return entity instanceof Blaze || entity instanceof Ghast
                || entity instanceof PigZombie || entity instanceof Wither;
    }
}
