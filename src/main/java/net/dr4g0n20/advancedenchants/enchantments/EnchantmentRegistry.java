package net.dr4g0n20.advancedenchants.enchantments;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Stores custom enchantment definitions in registration order. */
public final class EnchantmentRegistry {

    private final Map<String, EnchantmentDefinition> enchantments =
            new LinkedHashMap<String, EnchantmentDefinition>();

    public boolean register(EnchantmentDefinition definition) {
        if (definition == null || enchantments.containsKey(definition.getId())) {
            return false;
        }
        enchantments.put(definition.getId(), definition);
        return true;
    }

    public EnchantmentDefinition get(String id) {
        return id == null ? null : enchantments.get(id.toLowerCase(Locale.ENGLISH));
    }

    public Collection<EnchantmentDefinition> getAll() {
        return Collections.unmodifiableCollection(enchantments.values());
    }

    public int size() {
        return enchantments.size();
    }
}
