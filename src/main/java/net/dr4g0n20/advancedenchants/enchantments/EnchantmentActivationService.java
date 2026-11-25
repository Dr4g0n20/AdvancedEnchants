package net.dr4g0n20.advancedenchants.enchantments;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** Checks trigger, chance and cooldown before invoking a registered effect. */
public final class EnchantmentActivationService {

    private final EnchantmentRegistry registry;
    private final EnchantmentCooldownManager cooldowns;
    private final Map<String, EnchantmentEffectHandler> handlers =
            new HashMap<String, EnchantmentEffectHandler>();

    public EnchantmentActivationService(EnchantmentRegistry registry,
                                        EnchantmentCooldownManager cooldowns) {
        if (registry == null || cooldowns == null) {
            throw new IllegalArgumentException("Registry and cooldown manager are required.");
        }
        this.registry = registry;
        this.cooldowns = cooldowns;
    }

    public void registerHandler(String enchantmentId, EnchantmentEffectHandler handler) {
        if (enchantmentId == null || enchantmentId.trim().isEmpty() || handler == null) {
            throw new IllegalArgumentException("An enchantment id and handler are required.");
        }
        handlers.put(enchantmentId.toLowerCase(java.util.Locale.ENGLISH), handler);
    }

    public boolean tryActivate(Player player, ItemStack item, ActivationTrigger trigger,
                               Object event) {
        if (player == null || item == null || trigger == null || event == null) {
            return false;
        }

        boolean activated = false;
        for (StoredEnchantment stored : ItemEnchantmentStorage.getEnchantments(item)) {
            EnchantmentDefinition definition = registry.get(stored.getId());
            if (definition == null || definition.getMaximumLevel() < stored.getLevel()
                    || !definition.canApplyTo(item.getType())) {
                continue;
            }

            ActivationTrigger configured = ActivationTrigger.fromConfig(definition.getActivation());
            if (configured != trigger) {
                continue;
            }

            EnchantmentEffectHandler handler = handlers.get(definition.getId());
            if (handler == null || cooldowns.isOnCooldown(player.getUniqueId(), definition.getId())) {
                continue;
            }

            double chance = definition.getChance();
            if (chance < 100.0D && ThreadLocalRandom.current().nextDouble(100.0D) >= chance) {
                continue;
            }

            ActivationContext context = new ActivationContext(player, item, definition,
                    stored.getLevel(), trigger, event);
            handler.activate(context);
            cooldowns.startCooldown(player.getUniqueId(), definition.getId(),
                    definition.getCooldownSeconds());
            activated = true;
        }
        return activated;
    }

    public EnchantmentCooldownManager getCooldownManager() {
        return cooldowns;
    }

    public int getRegisteredHandlerCount() {
        return handlers.size();
    }
}
