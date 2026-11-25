package net.dr4g0n20.advancedenchants;

import net.dr4g0n20.advancedenchants.commands.AdminCommand;
import net.dr4g0n20.advancedenchants.enchantments.EnchantmentRegistry;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main entry point for AdvancedEnchants.
 * Targets the Spigot/Bukkit 1.12.2 API and Java 8.
 */
public final class AdvancedEnchantsPlugin extends JavaPlugin {

    private EnchantmentRegistry enchantmentRegistry;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.enchantmentRegistry = new EnchantmentRegistry();
        registerCommands();
        getLogger().info("AdvancedEnchants enabled. Enchantment engine is being initialised.");
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

    public EnchantmentRegistry getEnchantmentRegistry() {
        return enchantmentRegistry;
    }

    public void reloadPluginConfiguration() {
        reloadConfig();
    }
}
