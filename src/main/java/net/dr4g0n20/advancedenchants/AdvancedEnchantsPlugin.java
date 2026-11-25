package net.dr4g0n20.advancedenchants;

import net.dr4g0n20.advancedenchants.commands.AdminCommand;
import net.dr4g0n20.advancedenchants.guidebook.GuidebookService;
import net.dr4g0n20.advancedenchants.guidebook.GuidebookListener;
import org.bukkit.entity.Player;
import net.dr4g0n20.advancedenchants.enchantments.EnchantmentApplicator;
import net.dr4g0n20.advancedenchants.enchantments.EnchantmentActivationService;
import net.dr4g0n20.advancedenchants.enchantments.EnchantmentCooldownManager;
import net.dr4g0n20.advancedenchants.listeners.EnchantmentTriggerListener;
import net.dr4g0n20.advancedenchants.listeners.CombatLoggingListener;
import net.dr4g0n20.advancedenchants.enchantments.effects.FeatherStepHandler;
import net.dr4g0n20.advancedenchants.enchantments.effects.ExperienceHandler;
import net.dr4g0n20.advancedenchants.enchantments.effects.AllurementHandler;
import net.dr4g0n20.advancedenchants.enchantments.EnchantmentConfigLoader;
import net.dr4g0n20.advancedenchants.enchantments.EnchantmentRegistry;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

/**
 * Main entry point for AdvancedEnchants.
 * Targets the Spigot/Bukkit 1.12.2 API and Java 8.
 */
public final class AdvancedEnchantsPlugin extends JavaPlugin {

    private static final String[] BUNDLED_ENCHANTMENT_FILES = {
            "enchantments/vanilla-plus/weapons.yml",
            "enchantments/vanilla-plus/armour.yml",
            "enchantments/vanilla-plus/tools.yml",
            "enchantments/vanilla-plus/utility.yml"
    };

    private EnchantmentRegistry enchantmentRegistry;
    private EnchantmentApplicator enchantmentApplicator;
    private EnchantmentActivationService activationService;
    private GuidebookService guidebookService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        initialiseEnchantmentServices();
        registerCommands();
        Bukkit.getPluginManager().registerEvents(
                new EnchantmentTriggerListener(activationService), this);
        Bukkit.getPluginManager().registerEvents(new GuidebookListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CombatLoggingListener(this), this);
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
        this.guidebookService = new GuidebookService(enchantmentRegistry);

        if (getConfig().getBoolean("enchantments.load-vanilla-plus", true)) {
            saveBundledEnchantmentFiles();
            int loaded = new EnchantmentConfigLoader(this).loadInto(enchantmentRegistry);
            getLogger().info("Loaded " + loaded + " Vanilla+ enchantment definitions.");
        }

        int maximum = Math.max(1, getConfig().getInt("general.max-enchantments", 10));
        this.enchantmentApplicator = new EnchantmentApplicator(enchantmentRegistry, maximum);
        this.activationService = new EnchantmentActivationService(
                enchantmentRegistry, new EnchantmentCooldownManager());
        this.activationService.registerHandler("feather_step", new FeatherStepHandler());
        this.activationService.registerHandler("experience", new ExperienceHandler());
        this.activationService.registerHandler("allurement", new AllurementHandler());
    }

    private void saveBundledEnchantmentFiles() {
        for (String resource : BUNDLED_ENCHANTMENT_FILES) {
            if (!new java.io.File(getDataFolder(), resource).exists()) {
                saveResource(resource, false);
            }
        }
    }

    public EnchantmentRegistry getEnchantmentRegistry() {
        return enchantmentRegistry;
    }

    public void giveGuidebook(Player player) {
        guidebookService.giveTo(player);
    }

    public EnchantmentApplicator getEnchantmentApplicator() {
        return enchantmentApplicator;
    }

    public void reloadPluginConfiguration() {
        reloadConfig();
        initialiseEnchantmentServices();
    }
}
