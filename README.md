# AdvancedEnchants

A configurable custom-enchantment plugin for Minecraft 1.12.2.

## Target platform

- Minecraft: 1.12.2
- API: Spigot/Bukkit 1.12.2
- Java: 8
- Build system: Maven

## Current implementation

- Category-based YAML catalogue for armour, tools, fishing, farming, and utility enchantments.
- Custom enchantment storage, compatibility checks, configured conflicts, and a configurable item enchantment limit.
- Activation pipeline with per-enchantment chance and cooldown handling.
- Written guidebook available through `/ae guidebook`; all enabled enchantments are visible immediately.
- Combat-logging tag: disconnecting while tagged drops carried inventory, armour, and offhand items, then kills the player.
- Runtime effects currently implemented:
  - Feather Step, Experience, Allurement
  - Escape, Getaway, End Affinity, Nether Affinity
  - Aura, Vanish, Waterborne, Haste
  - Curse of Breaklessness, Foraging, Infernal Touch, Nether Prospector
  - Blacksmith, Carrot Planter, Replenish
  - Alacrity, Blast Mining, Auto Reel, Bait
- Passive effects are refreshed while the relevant enchanted equipment is worn or held.
- Mining effects preserve silk-touch behaviour for Infernal Touch and only add drops; Curse of Breaklessness cancels the individual block break without consuming durability.
- Crop replanting requires the appropriate seed item in the player's inventory; Blacksmith can prevent a durability loss.
- Blast Mining checks each additional block through a break event, skips protected or container blocks, and applies durability and eligible mining XP.
- Auto Reel uses reflective access to the 1.12.2 fishing-hook retraction method; server-fork compatibility must be verified in a live test.
- Weapon-focused definitions and offensive combat effects are not part of this implementation.

## Catalogue status

All 22 non-weapon Vanilla+ definitions in the current armour and tools catalogue now have runtime behaviour. The weapon catalogue remains intentionally empty. Rarity assignments and final balancing are still pending.

The plugin targets the Spigot/Bukkit 1.12.2 API. Builds verify compilation; live behaviour still needs testing on the target server software.

## Building

Run:

    mvn clean package

The plugin JAR will be generated in `target/`.

## Installation

1. Build the project.
2. Place the resulting JAR in the server's `plugins` folder.
3. Restart the server.
4. Configure `plugins/AdvancedEnchants/config.yml`.

## License

Not selected yet.
