package net.dr4g0n20.advancedenchants.listeners;

import net.dr4g0n20.advancedenchants.AdvancedEnchantsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Punishes combat logging: players who disconnect shortly after player-caused
 * damage are killed and their carried equipment is dropped at their logout spot.
 */
public final class CombatLoggingListener implements Listener {

    private final AdvancedEnchantsPlugin plugin;
    private final Map<UUID, Long> lastPlayerHit = new HashMap<UUID, Long>();
    private final Set<UUID> combatLogDeaths = new HashSet<UUID>();

    public CombatLoggingListener(AdvancedEnchantsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamagedByPlayer(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        Player attacker = getPlayerAttacker(event.getDamager());
        if (attacker == null || attacker.equals(victim)) {
            return;
        }

        lastPlayerHit.put(victim.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Long lastHit = lastPlayerHit.remove(playerId);
        if (lastHit == null) {
            return;
        }

        long tagSeconds = Math.max(0L,
                plugin.getConfig().getLong("combat-logging.tag-duration-seconds", 15L));
        long elapsed = System.currentTimeMillis() - lastHit;
        if (!plugin.getConfig().getBoolean("combat-logging.enabled", true)
                || elapsed > tagSeconds * 1000L || player.isDead()) {
            return;
        }

        Location dropLocation = player.getLocation();
        dropCarriedItems(player, dropLocation);

        String message = ChatColor.RED + player.getName()
                + " was killed for combat logging.";
        combatLogDeaths.add(playerId);
        player.setHealth(0.0D);

        // setHealth(0) normally fires PlayerDeathEvent immediately. Keep a
        // fallback announcement for server implementations that do not.
        if (combatLogDeaths.remove(playerId)) {
            Bukkit.broadcastMessage(message);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        UUID playerId = event.getEntity().getUniqueId();
        if (combatLogDeaths.remove(playerId)) {
            event.setDeathMessage(ChatColor.RED + event.getEntity().getName()
                    + " was killed for combat logging.");
        }
        lastPlayerHit.remove(playerId);
    }

    private void dropCarriedItems(Player player, Location location) {
        for (ItemStack item : player.getInventory().getContents()) {
            drop(item, location);
        }
        for (ItemStack item : player.getInventory().getArmorContents()) {
            drop(item, location);
        }
        drop(player.getInventory().getItemInOffHand(), location);

        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().setItemInOffHand(null);
    }

    private void drop(ItemStack item, Location location) {
        if (item != null && item.getType() != org.bukkit.Material.AIR && item.getAmount() > 0) {
            location.getWorld().dropItemNaturally(location, item.clone());
        }
    }

    private Player getPlayerAttacker(Entity damager) {
        if (damager instanceof Player) {
            return (Player) damager;
        }
        if (damager instanceof Projectile) {
            Object shooter = ((Projectile) damager).getShooter();
            if (shooter instanceof Player) {
                return (Player) shooter;
            }
        }
        return null;
    }
}
