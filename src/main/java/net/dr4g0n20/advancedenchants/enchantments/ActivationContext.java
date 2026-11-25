package net.dr4g0n20.advancedenchants.enchantments;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Immutable information passed to an enchantment effect handler. */
public final class ActivationContext {

    private final Player player;
    private final ItemStack sourceItem;
    private final EnchantmentDefinition definition;
    private final int level;
    private final ActivationTrigger trigger;
    private final Object event;

    public ActivationContext(Player player, ItemStack sourceItem,
                             EnchantmentDefinition definition, int level,
                             ActivationTrigger trigger, Object event) {
        this.player = player;
        this.sourceItem = sourceItem == null ? null : sourceItem.clone();
        this.definition = definition;
        this.level = level;
        this.trigger = trigger;
        this.event = event;
    }

    public Player getPlayer() { return player; }
    public ItemStack getSourceItem() { return sourceItem == null ? null : sourceItem.clone(); }
    public EnchantmentDefinition getDefinition() { return definition; }
    public int getLevel() { return level; }
    public ActivationTrigger getTrigger() { return trigger; }
    public Object getEvent() { return event; }
}
