# AdvancedEnchants

A configurable custom-enchantment plugin for Minecraft 1.12.2.

## Target platform

- Minecraft: 1.12.2
- API: Spigot/Bukkit 1.12.2
- Java: 8
- Build system: Maven

## Current implementation

- Category-based YAML catalogue for armour, weapons, tools, fishing, farming, and utility enchantments.
- Custom enchantment storage, compatibility checks, configured conflicts, and a configurable item enchantment limit.
- Activation pipeline with per-enchantment chance and cooldown handling.
- Written guidebook available through `/ae guidebook`.
- Combat-logging tag: disconnecting while tagged drops carried inventory, armour, and offhand items, then kills the player.
- Runtime effects implemented:
  - **Melee weapons:** Criticals, First Strike, Finishing, Blackout, Blaze Reaper, Cubism, Incinerate, Skullcrusher, Zombie Crusher, Starvation, Ravenous, Abrasion, Arctic Freeze, Ascend, Caffeinated, Carve, Charge, Curse of Harmlessness, Drain, Enderbane, Fuddle, Ninja, Postpone, Repel, Shura, and Thor.
  - **Bows:** Contagion, Explosive, Flashbang, Frost, Multi-Shot, plus compatible Cubism and Thor effects.
  - **Armour:** Aura, Adrenaline, Rebounding, Resonate, Rumble, Scorching, Escape, Getaway, End Affinity, Nether Affinity, Vanish, Waterborne, and Feather Step.
  - **Tools and mining:** Alacrity, Blast Mining, Curse of Breaklessness, Experience, Foraging, Haste, Infernal Touch, Nether Prospector, and Blacksmith.
  - **Farming:** Carrot Planter and Replenish.
  - **Fishing:** Allurement, Auto Reel, Bait, Fire Hook, Poisoned Hook, and Sharpness Hook.
- Existing server YAML files are migrated by appending missing bundled definitions without replacing custom enchantments or comments.
- Explosive arrows use a small explosion with fire and block damage disabled. Contagion creates a lingering poison cloud at the arrow impact location.
- Mining effects preserve silk-touch behaviour for Infernal Touch. Blast Mining checks each additional block through a break event, skips protected/container blocks, and applies durability and eligible mining XP.
- Crop replanting requires the appropriate seed item in the player's inventory; Blacksmith can prevent a durability loss.
- Auto Reel uses reflective access to the 1.12.2 fishing-hook retraction method; compatibility with server forks must be verified in a live test.
- Bait grants bonus fishing experience rather than changing bite probability.
- Passive effects are refreshed while the relevant enchanted equipment is worn or held.

## Vanilla+ compatibility notes

The Vanilla+ catalogue includes enchantments that require content introduced after Minecraft 1.12.2. They cannot be faithfully implemented on this target:
- **Brightness** targets the Warden, which does not exist in 1.12.2.
- **Double Blow** is trident-only, and tridents do not exist in 1.12.2.
- Crossbow-specific variants of Thor and Multi-Shot are unavailable because crossbows do not exist in 1.12.2.
- Ancient Debris is unavailable; Nether Prospector uses Nether Quartz Ore as the closest legacy mining alternative.

Some effects use legacy approximations where the 1.12.2 API lacks the later mechanic. Arctic Freeze uses Slowness and Poison to represent freezing and bleeding. Frost applies Slowness rather than a true frozen state.

Rarity assignments and final gameplay balancing remain configurable and are intentionally unassigned in the bundled catalogue.

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
