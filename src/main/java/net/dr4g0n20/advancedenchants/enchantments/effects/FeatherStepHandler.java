package net.dr4g0n20.advancedenchants.enchantments.effects;

import net.dr4g0n20.advancedenchants.enchantments.ActivationContext;
import net.dr4g0n20.advancedenchants.enchantments.EnchantmentEffectHandler;
import org.bukkit.event.entity.EntityDamageEvent;

/** Cancels eligible fall damage when Feather Step activates. */
public final class FeatherStepHandler implements EnchantmentEffectHandler {

    @Override
    public void activate(ActivationContext context) {
        if (!(context.getEvent() instanceof EntityDamageEvent)) {
            return;
        }
        EntityDamageEvent event = (EntityDamageEvent) context.getEvent();
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }
}
