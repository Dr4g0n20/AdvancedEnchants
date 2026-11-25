# AdvancedEnchants

A configurable custom-enchantment plugin for Minecraft 1.12.2.

## Target platform

- Minecraft: 1.12.2
- API: Spigot/Bukkit 1.12.2
- Java: 8
- Build system: Maven

The Spigot API is the compatibility baseline. The exact server software used by the host can be confirmed later; most Bukkit-compatible 1.12.2 servers support this API.

## Project direction

AdvancedEnchants is being built around the AdvancedEnchantments Vanilla+ catalogue, with additional genuinely unique enchantments considered from other sources.

The design is modular. Enchantment definitions, activation logic, item storage, anvil handling, acquisition, and the guidebook will be separate systems.

## Agreed design foundations

- Keep the Vanilla+ catalogue; adapt individual effects only where Minecraft 1.12.2 requires it.
- Maximum of 10 enchantments on an item or enchanted book by default (configurable).
- Custom enchantments will use plugin-managed metadata and application order.
- Enchantment rarity is cosmetic and does not alter normal vanilla enchanting probabilities.
- Anvil rules will be implemented separately from vanilla prior-work penalties.
- Configuration will be split into manageable files as the catalogue grows.

## Current implementation status

This is the initial project scaffold. It currently includes:

- Maven build targeting Java 8 and Spigot API 1.12.2.
- Plugin entry point and metadata.
- Main configuration defaults.
- Initial /ae reload administration command.
- Custom enchantment definition and registry foundations.
- Persistent item/book enchantment storage, including levels and application order.
- Application validation for item compatibility, maximum levels, configured conflicts, and the global enchantment limit.
- Books can store conflicting enchantments; equipment application checks conflicts.
- An application service exposed by the main plugin class.
- A category-based YAML loader for enchantment definitions.
- Initial Vanilla+ configuration files split into weapons, armour, tools, and utility.
- Definition metadata for rarity, activation type, chance, cooldown, item compatibility, conflicts, and effect settings.
- Preservation of pre-existing localized-name metadata.

The catalogue now includes an initial set of defensive armour and non-combat tool/fishing definitions, adapted for Minecraft 1.12.2. Weapon-focused definitions and combat effect handlers are not part of this implementation. Rarity assignments and activation balancing remain pending. The YAML files currently describe definitions only; runtime effect handlers, anvil replacement, acquisition systems, and the guidebook are not implemented yet.

## Building

Run:

    mvn clean package

The plugin JAR will be generated in target/.

## Installation

1. Build the project.
2. Place the resulting JAR in the server's plugins folder.
3. Restart the server.
4. Configure plugins/AdvancedEnchants/config.yml.

## License

Not selected yet.
