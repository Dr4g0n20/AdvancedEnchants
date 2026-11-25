package net.dr4g0n20.advancedenchants.enchantments.effects;

import net.dr4g0n20.advancedenchants.enchantments.ActivationContext;
import net.dr4g0n20.advancedenchants.enchantments.EnchantmentEffectHandler;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

/** Reduces incoming damage while the wearer is in the Nether. */
public final class NetherAffinityHandler implements EnchantmentEffectHandler {

    @Override
    public void activate(ActivationContext context) {
        if (!(context.getEvent() instanceof EntityDamageEvent)) {
            return;
        }
        Player player = context.getPlayer();
        if (player.getWorld().getEnvironment() != World.Environment.NETHER) {
            return;
        }

        EntityDamageEvent event = (EntityDamageEvent) context.getEvent();
        double reduction = numberSetting(context, "damage-reduction-percent-per-level", 3.0D)
                * context.getLevel() / 100.0D;
        reduction = Math.max(0.0D, Math.min(0.5D, reduction));
        event.setDamage(event.getDamage() * (1.0D - reduction));
    }

    private double numberSetting(ActivationContext context, String key, double fallback) {
        Object value = context.getDefinition().getEffectSettings().get(key);
        return value instanceof Number ? ((Number) value).doubleValue() : fallback;
    }
}
