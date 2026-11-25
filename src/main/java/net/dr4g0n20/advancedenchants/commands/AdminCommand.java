package net.dr4g0n20.advancedenchants.commands;

import net.dr4g0n20.advancedenchants.AdvancedEnchantsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class AdminCommand implements CommandExecutor {

    private final AdvancedEnchantsPlugin plugin;

    public AdminCommand(AdvancedEnchantsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("guidebook")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only players can receive the guidebook.");
                return true;
            }
            plugin.giveGuidebook((Player) sender);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("advancedenchants.admin")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }
            plugin.reloadPluginConfiguration();
            sender.sendMessage(ChatColor.GREEN + "AdvancedEnchants configuration reloaded.");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "AdvancedEnchants commands:");
        sender.sendMessage(ChatColor.GRAY + "/ae guidebook");
        if (sender.hasPermission("advancedenchants.admin")) {
            sender.sendMessage(ChatColor.GRAY + "/ae reload");
        }
        return true;
    }
}
