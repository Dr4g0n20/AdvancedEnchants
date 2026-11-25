package net.dr4g0n20.advancedenchants;

import net.dr4g0n20.advancedenchants.commands.AdminCommand;
import net.dr4g0n20.advancedenchants.guidebook.GuidebookService;
import net.dr4g0n20.advancedenchants.guidebook.GuidebookListener;
import org.bukkit.entity.Player;
import net.dr4g0n20.advancedenchants.enchantments.EnchantmentApplicator;
import net.dr4g0n20.advancedenchants.enchantments.EnchantmentActivationService;
import net.dr4g0n20.advancedenchants.enchantments.EnchantmentCooldownManager;
import net.dr4g0n20.advancedenchants.enchantments.PassiveEnchantmentTask;
import net.dr4g0n20.advancedenchants.listeners.EnchantmentTriggerListener;
import net.dr4g0n20.advancedenchants.listeners.CombatLoggingListener;
import net.dr4g0n20.advancedenchants.listeners.AuraListener;
import net.dr4g0n20.advancedenchants.listeners.BlockDropEnchantmentListener;
import net.dr4g0n20.advancedenchants.listeners.BlockBreakEnchantmentListener;
import net.dr4g0n20.advancedenchants.listeners.ToolDurabilityListener;
import net.dr4g0n20.advancedenchants.listeners.CropReplantListener;
import net.dr4g0n20.advancedenchants.listeners.FishingUtilityListener;
import net.dr4g0n20.advancedenchants.listeners.BlastMiningListener;
import net.dr4g0n20.advancedenchants.listeners.WeaponEnchantmentListener;
import net.dr4g0n20.advancedenchants.listeners.ArmorCombatListener;
import net.dr4g0n20.advancedenchants.listeners.BowEnchantmentListener;
import net.dr4g0n20.advancedenchants.enchantments.effects.FeatherStepHandler;
import net.dr4g0n20.advancedenchants.enchantments.effects.ExperienceHandler;
import net.dr4g0n20.advancedenchants.enchantments.effects.AllurementHandler;
import net.dr4g0n20.advancedenchants.enchantments.effects.EscapeHandler;
import net.dr4g0n20.advancedenchants.enchantments.effects.GetawayHandler;
import net.dr4g0n20.advancedenchants.enchantments.effects.EndAffinityHandler;
import net.dr4g0n20.advancedenchants.enchantments.effects.NetherAffinityHandler;
import net.dr4g0n20.advancedenchants.enchantments.effects.VanishHandler;
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
        Bukkit.getPluginManager().registerEvents(new AuraListener(), this);
        Bukkit.getPluginManager().registerEvents(new BlockDropEnchantmentListener(), this);
        Bukkit.getPluginManager().registerEvents(new BlockBreakEnchantmentListener(), this);
        Bukkit.getPluginManager().registerEvents(new ToolDurabilityListener(), this);
        Bukkit.getPluginManager().registerEvents(new CropReplantListener(this), this);
        Bukkit.getPluginManager().registerEvents(new FishingUtilityListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlastMiningListener(this), this);
        Bukkit.getPluginManager().registerEvents(new WeaponEnchantmentListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ArmorCombatListener(), this);
        Bukkit.getPluginManager().registerEvents(new BowEnchantmentListener(this), this);
        new PassiveEnchantmentTask(this).runTaskTimer(this, 20L, 20L);
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
        this.activationService.registerHandler("escape", new EscapeHandler());
        this.activationService.registerHandler("getaway", new GetawayHandler());
        this.activationService.registerHandler("end_affinity", new EndAffinityHandler());
        this.activationService.registerHandler("nether_affinity", new NetherAffinityHandler());
        this.activationService.registerHandler("vanish", new VanishHandler());
    }

    private void saveBundledEnchantmentFiles() {
        for (String resource : BUNDLED_ENCHANTMENT_FILES) {
            java.io.File file = new java.io.File(getDataFolder(), resource);
            if (!file.exists()) {
                saveResource(resource, false);
                continue;
            }

            // The original release shipped an intentionally empty weapons.yml.
            // Upgrade that exact empty catalogue, but never overwrite a server's
            // existing custom weapon definitions.
            if (resource.endsWith("/weapons.yml")) {
                org.bukkit.configuration.file.YamlConfiguration existing =
                        org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
                org.bukkit.configuration.ConfigurationSection entries =
                        existing.getConfigurationSection("enchantments");
                if (entries != null && entries.getKeys(false).isEmpty()) {
                    saveResource(resource, true);
                }
            }

            if (resource.endsWith("/weapons.yml")) {
                appendMissingDefaults(file, resource, new String[] {
                        "contagion", "explosive", "flashbang", "frost", "multi_shot",
                        "arctic_freeze", "ascend", "caffeinated", "carve", "charge",
                        "curse_of_harmlessness", "drain", "enderbane", "fuddle",
                        "ninja", "postpone", "repel", "shura", "thor"
                });
            }
            if (resource.endsWith("/armour.yml")) {
                appendMissingDefaults(file, resource, new String[] {
                        "adrenaline", "rebounding", "resonate", "rumble", "scorching"
                });
            }
            if (resource.endsWith("/tools.yml")) {
                appendMissingDefaults(file, resource, new String[] {
                        "fire_hook", "poisoned_hook", "sharpness_hook"
                });
            }
        }
    }

    private void appendMissingDefaults(java.io.File file, String resource, String[] addedIds) {
        org.bukkit.configuration.file.YamlConfiguration existing =
                org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
        org.bukkit.configuration.ConfigurationSection entries =
                existing.getConfigurationSection("enchantments");
        if (entries == null) {
            return;
        }

        java.io.InputStream stream = getResource(resource);
        if (stream == null) {
            return;
        }

        java.util.Scanner scanner = new java.util.Scanner(stream, "UTF-8").useDelimiter("\\A");
        String bundled;
        try {
            bundled = scanner.hasNext() ? scanner.next() : "";
        } finally {
            scanner.close();
        }

        StringBuilder additions = new StringBuilder();
        for (String id : addedIds) {
            if (entries.contains(id)) {
                continue;
            }
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                    "(?ms)^  " + id + ":\\s*\\n.*?(?=^  [a-z0-9_]+:\\s*$|\\z)");
            java.util.regex.Matcher matcher = pattern.matcher(bundled);
            if (matcher.find()) {
                additions.append(matcher.group().trim()).append("\n\n");
            }
        }

        if (additions.length() == 0) {
            return;
        }

        try {
            String current = new String(java.nio.file.Files.readAllBytes(file.toPath()),
                    java.nio.charset.StandardCharsets.UTF_8);
            String updated = current + (current.endsWith("\n") ? "" : "\n")
                    + additions.toString();
            java.nio.file.Files.write(file.toPath(),
                    updated.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            getLogger().info("Added missing Vanilla+ definitions to " + file.getName() + ".");
        } catch (java.io.IOException ex) {
            getLogger().warning("Could not migrate Vanilla+ definitions: "
                    + ex.getMessage());
        }
    }

    public EnchantmentRegistry getEnchantmentRegistry() {
        return enchantmentRegistry;
    }

    public void openGuidebook(Player player) {
        guidebookService.open(player);
    }

    public GuidebookService getGuidebookService() {
        return guidebookService;
    }

    public EnchantmentApplicator getEnchantmentApplicator() {
        return enchantmentApplicator;
    }

    public void reloadPluginConfiguration() {
        reloadConfig();
        initialiseEnchantmentServices();
    }
}
