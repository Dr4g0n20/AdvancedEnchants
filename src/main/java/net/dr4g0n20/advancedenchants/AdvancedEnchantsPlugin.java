package net.dr4g0n20.advancedenchants;

import net.dr4g0n20.advancedenchants.commands.AdminCommand;
import net.dr4g0n20.advancedenchants.enchantments.EnchantmentApplicator;
import net.dr4g0n20.advancedenchants.enchantments.EnchantmentRegistry;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main entry point for AdvancedEnchants.
 * Targets the Spigot/Bukkit 1.12.2 API and Java 8.
 */
public final class AdvancedEnchantsPlugin extends JavaPlugin {

    private EnchantmentRegistry enchantmentRegistry;
    private EnchantmentApplicator enchantmentApplicator;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        initialiseEnchantmentServices();
        registerCommands();
        getLogger().info("AdvancedEnchants enabled. Registered "
                + enchantmentRegistry.size() + " custom enchantments.");
    }

    @Override
    public void onDisable() {
        getLogger().info("AdvancedEnchants disabled.");
    }

    private void registerCommands() {
        PluginCommand command = getCommand("ae");
        if (command == null) {
            getLogger().severe("The /ae command is missing from plugin.yml.");
            return;
        }
        command.setExecutor(new AdminCommand(this));
    }

    private void initialiseEnchantmentServices() {
        this.enchantmentRegistry = new EnchantmentRegistry();
        int maximum = Math.max(1, getConfig().getInt("general.max-enchantments", 10));
        this.enchantmentApplicator = new EnchantmentApplicator(enchantmentRegistry, maximum);
    }

    public EnchantmentRegistry getEnchantmentRegistry() {
        return enchantmentRegistry;
    }

    public EnchantmentApplicator getEnchantmentApplicator() {
        return enchantmentApplicator;
    }

    public void reloadPluginConfiguration() {
        reloadConfig();
        initialiseEnchantmentServices();
    }
}
