package net.dr4g0n20.advancedenchants.enchantments.effects;

import net.dr4g0n20.advancedenchants.enchantments.ActivationContext;
import net.dr4g0n20.advancedenchants.enchantments.EnchantmentEffectHandler;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Briefly conceals the wearer after taking damage. */
public final class VanishHandler implements EnchantmentEffectHandler {

    @Override
    public void activate(ActivationContext context) {
        Player player = context.getPlayer();
        int duration = 40 + (context.getLevel() - 1) * 20;
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY, duration, 0, true, false), true);
    }
}
