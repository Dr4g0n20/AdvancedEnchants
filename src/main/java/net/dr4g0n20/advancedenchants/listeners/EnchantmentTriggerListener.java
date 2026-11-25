package net.dr4g0n20.advancedenchants.listeners;

import net.dr4g0n20.advancedenchants.enchantments.ActivationTrigger;
import net.dr4g0n20.advancedenchants.enchantments.EnchantmentActivationService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

/** Routes safe gameplay events into the custom enchantment activation service. */
public final class EnchantmentTriggerListener implements Listener {

    private final EnchantmentActivationService activations;

    public EnchantmentTriggerListener(EnchantmentActivationService activations) {
        if (activations == null) {
            throw new IllegalArgumentException("Activation service cannot be null.");
        }
        this.activations = activations;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getItemInHand();
        activations.tryActivate(player, tool, ActivationTrigger.BLOCK_BREAK, event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFishingCatch(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }
        ItemStack rod = event.getPlayer().getItemInHand();
        activations.tryActivate(event.getPlayer(), rod, ActivationTrigger.FISHING_CATCH, event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        ActivationTrigger trigger = event.getCause() == EntityDamageEvent.DamageCause.FALL
                ? ActivationTrigger.FALL_DAMAGE : ActivationTrigger.DEFENSE;

        for (ItemStack armourPiece : player.getInventory().getArmorContents()) {
            if (armourPiece != null) {
                activations.tryActivate(player, armourPiece, trigger, event);
            }
        }
    }
}
