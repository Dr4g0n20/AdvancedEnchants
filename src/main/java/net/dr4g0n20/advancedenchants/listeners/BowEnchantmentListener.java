package net.dr4g0n20.advancedenchants.listeners;

import net.dr4g0n20.advancedenchants.enchantments.ItemEnchantmentStorage;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/** Bow enchantments implemented using the legacy 1.12.2 projectile API. */
public final class BowEnchantmentListener implements Listener {

    private static final String BOW_DATA = "advancedenchants_bow";
    private static boolean spawningExtraArrows;
    private final JavaPlugin plugin;

    public BowEnchantmentListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArrowLaunch(ProjectileLaunchEvent event) {
        if (spawningExtraArrows || !(event.getEntity() instanceof Arrow)
                || !(event.getEntity().getShooter() instanceof Player)) {
            return;
        }

        Player shooter = (Player) event.getEntity().getShooter();
        ItemStack bow = shooter.getItemInHand();
        if (bow == null || bow.getType() != org.bukkit.Material.BOW) {
            return;
        }

        Arrow arrow = (Arrow) event.getEntity();
        arrow.setMetadata(BOW_DATA, new FixedMetadataValue(plugin, bow.clone()));

        int multiShot = ItemEnchantmentStorage.getLevel(bow, "multi_shot");
        if (multiShot < 1) {
            return;
        }

        // Two additional arrows are spread slightly around the original shot.
        Vector direction = arrow.getVelocity().clone();
        Location origin = arrow.getLocation();
        spawningExtraArrows = true;
        try {
            for (double angle : new double[] {-0.12D, 0.12D}) {
                Vector spread = rotateY(direction, angle);
                Arrow extra = shooter.getWorld().spawnArrow(origin, spread,
                        (float) Math.max(0.1D, direction.length()), 0.0F);
                extra.setShooter(shooter);
                extra.setMetadata(BOW_DATA, new FixedMetadataValue(plugin, bow.clone()));
            }
        } finally {
            spawningExtraArrows = false;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onArrowDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow)
                || !(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        ItemStack bow = storedBow((Arrow) event.getDamager());
        if (bow == null) {
            return;
        }

        LivingEntity target = (LivingEntity) event.getEntity();
        int flashbang = ItemEnchantmentStorage.getLevel(bow, "flashbang");
        if (flashbang > 0) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,
                    20 * flashbang, 0), true);
        }

        int frost = ItemEnchantmentStorage.getLevel(bow, "frost");
        if (frost > 0) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,
                    40 + (20 * frost), Math.min(3, frost - 1)), true);
        }

        int contagion = ItemEnchantmentStorage.getLevel(bow, "contagion");
        if (contagion > 0) {
            int duration = 40 + (20 * contagion);
            for (Entity nearby : target.getNearbyEntities(3.0D + contagion,
                    2.0D, 3.0D + contagion)) {
                if (nearby instanceof LivingEntity && nearby != target) {
                    ((LivingEntity) nearby).addPotionEffect(new PotionEffect(
                            PotionEffectType.POISON, duration, 0), true);
                }
            }
            target.addPotionEffect(new PotionEffect(PotionEffectType.POISON,
                    duration, 0), true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArrowLand(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow)) {
            return;
        }
        ItemStack bow = storedBow((Arrow) event.getEntity());
        if (bow == null || ItemEnchantmentStorage.getLevel(bow, "explosive") < 1) {
            return;
        }
        Location hit = event.getEntity().getLocation();
        // No fire and no block damage: the enchantment cannot grief terrain.
        hit.getWorld().createExplosion(hit.getX(), hit.getY(), hit.getZ(),
                1.0F, false, false);
    }

    private ItemStack storedBow(Arrow arrow) {
        if (!arrow.hasMetadata(BOW_DATA)) {
            return null;
        }
        List<MetadataValue> values = arrow.getMetadata(BOW_DATA);
        if (values.isEmpty() || !(values.get(0).value() instanceof ItemStack)) {
            return null;
        }
        return (ItemStack) values.get(0).value();
    }

    private Vector rotateY(Vector vector, double angle) {
        double cosine = Math.cos(angle);
        double sine = Math.sin(angle);
        return new Vector(vector.getX() * cosine - vector.getZ() * sine,
                vector.getY(),
                vector.getX() * sine + vector.getZ() * cosine);
    }
}
