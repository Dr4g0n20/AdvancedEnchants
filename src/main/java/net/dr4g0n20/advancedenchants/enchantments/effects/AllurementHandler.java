package net.dr4g0n20.advancedenchants.enchantments.effects;

import net.dr4g0n20.advancedenchants.enchantments.ActivationContext;
import net.dr4g0n20.advancedenchants.enchantments.EnchantmentEffectHandler;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/** Grants one additional copy of the caught item without changing the original catch. */
public final class AllurementHandler implements EnchantmentEffectHandler {

    @Override
    public void activate(ActivationContext context) {
        if (!(context.getEvent() instanceof PlayerFishEvent)) {
            return;
        }
        PlayerFishEvent event = (PlayerFishEvent) context.getEvent();
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }

        Entity caught = event.getCaught();
        if (!(caught instanceof Item)) {
            return;
        }

        ItemStack bonus = ((Item) caught).getItemStack().clone();
        Player player = context.getPlayer();
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(bonus);
        for (ItemStack leftover : leftovers.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }
    }
}
