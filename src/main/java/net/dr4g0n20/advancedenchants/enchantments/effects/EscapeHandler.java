package net.dr4g0n20.advancedenchants.enchantments.effects;

import net.dr4g0n20.advancedenchants.enchantments.ActivationContext;
import net.dr4g0n20.advancedenchants.enchantments.EnchantmentEffectHandler;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Gives the wearer a brief speed boost after taking damage. */
public final class EscapeHandler implements EnchantmentEffectHandler {

    @Override
    public void activate(ActivationContext context) {
        Player player = context.getPlayer();
        int amplifier = Math.max(0, Math.min(1, context.getLevel() - 1));
        int duration = 60 + (context.getLevel() - 1) * 20;
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED, duration, amplifier, true, false), true);
    }
}
