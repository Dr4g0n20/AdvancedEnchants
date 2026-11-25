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

The design is modular. Enchantment definitions, activation logic, item storage, anvil handling, acquisition, and the guidebook are separate systems.

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
- A trigger, chance, and per-player cooldown activation pipeline for non-offensive enchantments.
- A player-accessible written guidebook via `/ae guidebook`; every enabled enchantment is visible immediately, and existing guidebooks refresh when opened.
- Initial runtime handlers for Feather Step (fall protection), Experience (bonus ore XP), and Allurement (bonus fishing catch).
- Preservation of pre-existing localized-name metadata.

The catalogue currently contains a partial set of defensive armour and non-combat tool, fishing, and farming definitions, adapted for Minecraft 1.12.2. It is not yet the complete catalogue discussed during planning. Weapon-focused definitions and combat effect handlers are not part of this implementation. Rarity assignments and activation balancing remain pending. The first three non-offensive runtime effects are implemented; other YAML entries remain definitions until their handlers are added. The guidebook shows every enabled enchantment to every player from the first opening, without a discovery or hidden-entry system. Anvil replacement and acquisition systems are not implemented yet.

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
