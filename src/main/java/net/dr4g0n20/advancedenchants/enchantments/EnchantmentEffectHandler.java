package net.dr4g0n20.advancedenchants.enchantments;

/** Performs one registered, non-offensive enchantment effect. */
public interface EnchantmentEffectHandler {
    void activate(ActivationContext context);
}
