package net.dr4g0n20.advancedenchants.enchantments.effects;

import net.dr4g0n20.advancedenchants.enchantments.ActivationContext;
import net.dr4g0n20.advancedenchants.enchantments.EnchantmentEffectHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Grants a stronger speed boost when damage would leave the wearer critically low. */
public final class GetawayHandler implements EnchantmentEffectHandler {

    @Override
    public void activate(ActivationContext context) {
        if (!(context.getEvent() instanceof EntityDamageEvent)) {
            return;
        }

        EntityDamageEvent event = (EntityDamageEvent) context.getEvent();
        Player player = context.getPlayer();
        double thresholdPercent = numberSetting(
                context, "health-threshold-percent", 20.0D);
        double projectedHealth = player.getHealth() - event.getFinalDamage();
        double threshold = player.getMaxHealth() * thresholdPercent / 100.0D;
        if (projectedHealth > threshold) {
            return;
        }

        int amplifier = Math.min(2, context.getLevel());
        int duration = 80 + (context.getLevel() - 1) * 20;
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED, duration, amplifier, true, false), true);
    }

    private double numberSetting(ActivationContext context, String key, double fallback) {
        Object value = context.getDefinition().getEffectSettings().get(key);
        return value instanceof Number ? ((Number) value).doubleValue() : fallback;
    }
}
