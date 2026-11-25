package net.dr4g0n20.advancedenchants.listeners;

import net.dr4g0n20.advancedenchants.enchantments.ItemEnchantmentStorage;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

/** Defensive armour combat effects supported by the Bukkit 1.12.2 API. */
public final class ArmorCombatListener implements Listener {

    // Reflection and area damage create nested damage events. Do not recursively
    // trigger armour combat effects from those secondary hits.
    private boolean applyingSecondaryDamage;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDefend(EntityDamageByEntityEvent event) {
        if (applyingSecondaryDamage || !(event.getEntity() instanceof Player)
                || !(event.getDamager() instanceof LivingEntity)) {
            return;
        }

        Player defender = (Player) event.getEntity();
        LivingEntity attacker = (LivingEntity) event.getDamager();
        int adrenaline = totalLevel(defender, "adrenaline");
        int rebounding = totalLevel(defender, "rebounding");
        int resonate = totalLevel(defender, "resonate");
        int rumble = totalLevel(defender, "rumble");
        int scorching = totalLevel(defender, "scorching");

        if (resonate > 0) {
            event.setDamage(event.getDamage() * Math.max(0.0D, 1.0D - (0.04D * resonate)));
        }

        if (adrenaline > 0 && !(attacker instanceof Player)) {
            defender.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,
                    40 + (20 * adrenaline), Math.min(2, adrenaline - 1)), true);
        }

        if (scorching > 0 && roll(Math.min(0.60D, 0.12D * scorching))) {
            attacker.setFireTicks(Math.max(attacker.getFireTicks(), 40 + (20 * scorching)));
        }

        if (rebounding > 0 && roll(Math.min(0.60D, 0.10D * rebounding))) {
            applySecondaryDamage(attacker, defender, Math.max(1.0D, event.getDamage() * 0.10D * rebounding));
        }

        if (rumble > 0 && roll(Math.min(0.50D, 0.08D * rumble))) {
            for (org.bukkit.entity.Entity nearby : defender.getNearbyEntities(3.0D, 3.0D, 3.0D)) {
                if (!(nearby instanceof LivingEntity) || nearby == attacker || nearby == defender) {
                    continue;
                }
                applySecondaryDamage((LivingEntity) nearby, defender, 1.0D + rumble);
            }
        }
    }

    private int totalLevel(Player player, String id) {
        int total = 0;
        for (ItemStack piece : player.getInventory().getArmorContents()) {
            total += ItemEnchantmentStorage.getLevel(piece, id);
        }
        return total;
    }

    private void applySecondaryDamage(LivingEntity target, Player source, double damage) {
        if (target.isDead() || damage <= 0.0D) {
            return;
        }
        applyingSecondaryDamage = true;
        try {
            target.damage(damage, source);
        } finally {
            applyingSecondaryDamage = false;
        }
    }

    private boolean roll(double chance) {
        return ThreadLocalRandom.current().nextDouble() < chance;
    }
}
