package net.dr4g0n20.advancedenchants.enchantments.effects;

import net.dr4g0n20.advancedenchants.enchantments.ActivationContext;
import net.dr4g0n20.advancedenchants.enchantments.EnchantmentEffectHandler;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Adds a small amount of bonus XP when mining eligible vanilla ores. */
public final class ExperienceHandler implements EnchantmentEffectHandler {

    private static final Set<Material> ORES = new HashSet<Material>(Arrays.asList(
            Material.COAL_ORE, Material.DIAMOND_ORE, Material.EMERALD_ORE,
            Material.LAPIS_ORE, Material.REDSTONE_ORE, Material.GLOWING_REDSTONE_ORE,
            Material.QUARTZ_ORE
    ));

    @Override
    public void activate(ActivationContext context) {
        if (!(context.getEvent() instanceof BlockBreakEvent)) {
            return;
        }
        BlockBreakEvent event = (BlockBreakEvent) context.getEvent();
        if (!ORES.contains(event.getBlock().getType()) || event.getExpToDrop() <= 0) {
            return;
        }

        // Bonus scales gently with level and never changes the block's drops.
        event.setExpToDrop(event.getExpToDrop() + context.getLevel());
    }
}
